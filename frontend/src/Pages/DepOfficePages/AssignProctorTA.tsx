import React, { useState, useEffect, useMemo, useRef } from 'react';
import axios from 'axios';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import GreenBut from '../../components/Buttons/GreenBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import LoadingPage from '../CommonPages/LoadingPage';
import styles from './AssignProctorTA.module.css';

export interface TA {
  id: string | number;
  name: string;
  level: string;
  workload: number;
  hasAdjacentExam: boolean;
  type?: 'Full-time' | 'Part-time'; // Making this optional as it's not in API
}

interface AvailableTA {
  workload: number;
  hasAdjacentExam: boolean;
  taId: number;
  name: string;
  surname: string;
  academicLevel: string;
}

interface ProctorRequestInfo {
  requestId: number;
  examId: number;
  examName: string;
  courseName: string;
  courseCode: string;
  requiredTas: number;
  tasLeft: number;
  assignedTas: TA[];
}

const CAPACITY = {
  fullCap: 20,
  partCap: 10,
};

const AssignProctorTA: React.FC = () => {
  const navigate = useNavigate();
  const { examID } = useParams<{ examID: string }>();
  const [searchParams] = useSearchParams();
  const requestId = searchParams.get('request');
  
  // State declarations
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [examData, setExamData] = useState<ProctorRequestInfo | null>(null);
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [savingChanges, setSavingChanges] = useState(false);

  // For exit and save confirmations
  const [showExitConfirm, setShowExitConfirm] = useState(false);
  const [showSaveConfirm, setShowSaveConfirm] = useState(false);

  // Track initial assigned IDs
  const initialAssigned = useRef<(string | number)[]>([]);

  // Fetch exam and TA data
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        if (!examID) {
          throw new Error("Exam ID is missing");
        }
        
        // Fetching basic exam info based on the request ID
        // This might need to be adjusted based on your actual API
        const examInfoResponse = await axios.get<ProctorRequestInfo>(
          `/api/proctor-request/${requestId}`
        ).catch(() => {
          // Fallback to using a mock response for development
          return {
            data: {
              requestId: parseInt(requestId || '0'),
              examId: parseInt(examID),
              examName: "Midterm",
              courseName: "Data Structures",
              courseCode: "CS-464",
              requiredTas: 3,
              tasLeft: 3,
              assignedTas: []
            }
          };
        });
        
        // Set exam data
        const examInfo = examInfoResponse.data;
        setExamData(examInfo);
        
        // Now fetch available TAs
        const taResponse = await axios.get<AvailableTA[]>(
          `/api/course/${examInfo.courseCode}/proctoring/exam/${examInfo.examId}`
        );
        
        // Transform TAs into the format our component uses
        const transformedTAs: TA[] = taResponse.data.map(ta => ({
          id: ta.taId,
          name: `${ta.name} ${ta.surname}`,
          level: ta.academicLevel,
          workload: ta.workload,
          hasAdjacentExam: ta.hasAdjacentExam,
          // We don't have type in the API, setting a default value
          type: 'Full-time' // Default assumption
        }));
        
        // Set assigned TAs
        if (examInfo.assignedTas && examInfo.assignedTas.length > 0) {
          setAssigned(examInfo.assignedTas);
          initialAssigned.current = examInfo.assignedTas.map(ta => ta.id);
          
          // Filter out already assigned TAs from potential TAs
          const assignedIds = new Set(examInfo.assignedTas.map(ta => ta.id));
          setPotential(transformedTAs.filter(ta => !assignedIds.has(ta.id)));
        } else {
          // No TAs assigned yet
          setPotential(transformedTAs);
        }
      } catch (err) {
        console.error("Failed to fetch exam data:", err);
        setError("Failed to load exam data. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [examID, requestId]);

  // Handle no exam data scenario
  if (!loading && !examData) {
    return (
      <div className={styles.pageWrapper}>
        <p className={styles.notFound}>Exam not found.</p>
        <button 
          className={styles.retryButton} 
          onClick={() => navigate('/department-office/assign-proctor')}
        >
          Return to Assignments
        </button>
      </div>
    );
  }

  const leftCount = examData ? Math.max(0, examData.requiredTas - assigned.length) : 0;

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else if (!examData || assigned.length < examData.requiredTas) {
      setAssigned(a => [...a, ta]);
      setPotential(p => p.filter(x => x.id !== ta.id));
    } else {
      setErrorMsg(`Cannot select more than ${examData?.requiredTas} TAs.`);
    }
  };

  const filtered = useMemo(() => {
    return potential
      .filter(t => t.name.toLowerCase().includes(searchTerm.toLowerCase()))
      .sort((a, b) => a.workload - b.workload);
  }, [potential, searchTerm]);

  const handleBackClick = () => {
    const curr = assigned.map(t => t.id).sort().join();
    const init = initialAssigned.current.sort().join();
    if (curr !== init) {
      setShowExitConfirm(true);
    } else {
      navigate('/department-office/assign-proctor');
    }
  };

  const onConfirmExit = () => {
    setShowExitConfirm(false);
    navigate('/department-office/assign-proctor');
  };

  const onCancelExit = () => {
    setShowExitConfirm(false);
  };

  const handleSaveClick = () => {
    setShowSaveConfirm(true);
  };

  const onConfirmSave = async () => {
    try {
      setSavingChanges(true);
      
      // Extract just the IDs of assigned TAs
      const assignedTaIds = assigned.map(ta => ta.id);
      
      // Make API call to update assigned TAs
      await axios.post(`/api/proctor-request/${requestId}/assign-tas`, {
        examId: examData?.examId,
        taIds: assignedTaIds
      });
      
      // Update the initial assigned reference
      initialAssigned.current = assignedTaIds;
      setShowSaveConfirm(false);
      
      // Show success message
      setErrorMsg("TAs have been successfully assigned!");
      
      // Navigate back after a delay
      setTimeout(() => {
        navigate('/department-office/assign-proctor');
      }, 1500);
    } catch (err) {
      console.error("Failed to save TA assignments:", err);
      setErrorMsg("Failed to save changes. Please try again.");
    } finally {
      setSavingChanges(false);
    }
  };

  const onCancelSave = () => {
    setShowSaveConfirm(false);
  };

  if (loading) {
    return <LoadingPage />;
  }

  if (error) {
    return (
      <div className={styles.pageWrapper}>
        <p className={styles.errorMessage}>{error}</p>
        <button 
          className={styles.retryButton} 
          onClick={() => window.location.reload()}
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={handleBackClick} />
        <h1 className={styles.title}>
          {examData?.courseName} - {examData?.examName}
        </h1>
        <div className={styles.stats}>
          <span>Needed: {examData?.requiredTas}</span>
          <span>Left: {leftCount}</span>
        </div>
      </div>

      <div className={styles.search}>
        <input
          type="text"
          placeholder="Search by name…"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
        />
      </div>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
            <th>Adjacent Exam</th>
            {/* <th>Type</th> Removed as it's not in API */}
          </tr>
        </thead>
        <tbody>
          {filtered.map(ta => (
            <tr
              key={ta.id}
              className={`${styles.taRow} ${assigned.some(a => a.id === ta.id) ? styles.selectedRow : ''}`}
              onClick={() => toggleSelect(ta)}
            >
              <td>{ta.name}</td>
              <td>{ta.level}</td>
              <td>{ta.workload}</td>
              <td>{ta.hasAdjacentExam ? 'Yes' : 'No'}</td>
              {/* <td>{ta.type}</td> Removed as it's not in API */}
            </tr>
          ))}
          {filtered.length === 0 && (
            <tr>
              <td colSpan={4} className={styles.noResults}>
                No TAs found matching your search.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <div className={styles.selectedSection}>
        <h2>Assigned TAs ({assigned.length})</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => (
            <li key={ta.id} onClick={() => toggleSelect(ta)}>
              {ta.name} <span className={styles.removeIcon}>×</span>
            </li>
          ))}
          {assigned.length === 0 && (
            <li className={styles.noAssigned}>No TAs assigned yet</li>
          )}
        </ul>
      </div>

      <div className={styles.actions}>
        <GreenBut 
          text={savingChanges ? "Saving..." : "Save Changes"} 
          onClick={handleSaveClick}
          disabled={savingChanges}
        />
      </div>

      <div className={styles.info}>
        Full Time Workload Capacity: {CAPACITY.fullCap} <br />
        Part Time Workload Capacity: {CAPACITY.partCap} <br />
      </div>

      {errorMsg && <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />}

      {showSaveConfirm && (
        <ConPop
          message="Do you want to save the changes you made to TA assignments?"
          onConfirm={onConfirmSave}
          onCancel={onCancelSave}
        />
      )}

      {showExitConfirm && (
        <ConPop
          message="You have unsaved changes. Are you sure you want to leave?"
          onConfirm={onConfirmExit}
          onCancel={onCancelExit}
        />
      )}
    </div>
  );
};

export default AssignProctorTA;

/*
import React, { useState, useMemo, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import GreenBut from '../../components/Buttons/GreenBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignProctorTA.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  type: 'Full-time' | 'Part-time';
}

export interface no{
  fullCap: number;
  partCap: number;
}

const no: no = {
  fullCap: 20,
  partCap: 10,
}

export interface Exam {
  id: string;
  courseName: string;
  examType: 'Midterm' | 'Final';
  needed: number;
  tasLeft: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
}

const sampleExams: Exam[] = [
  {
    id: 'e1',
    courseName: 'Algorithms – Midterm',
    examType: 'Midterm',
    needed: 3,
    tasLeft: 1,
    assignedTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, type: 'Full-time' },
      { id: 'ta2', name: 'Ayşe Fatma', level: 'MS',  workload: 4, hasAdjacentExam: true,  type: 'Part-time' },
    ],
    potentialTAs: [
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, type: 'Full-time' },
      { id: 'ta4', name: 'John Smith', level: 'BS',  workload: 3, hasAdjacentExam: true,  type: 'Part-time' },
    ],
  },
  {
    id: 'e2',
    courseName: 'Data Structures – Final',
    examType: 'Final',
    needed: 2,
    tasLeft: 2,
    assignedTAs: [],
    potentialTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, type: 'Full-time' },
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, type: 'Full-time' },
    ],
  },
];

const AssignProctorTA: React.FC = () => {
  const navigate = useNavigate();
  const { examID } = useParams<{ examID: string }>();
  const examData = sampleExams.find(e => e.id === examID);
  if (!examData) {
    return <div className={styles.pageWrapper}><p className={styles.notFound}>Exam not found.</p></div>;
  }

  const { needed } = examData;
  const [assigned, setAssigned] = useState<TA[]>([...examData.assignedTAs]);
  const [potential, setPotential] = useState<TA[]>([...examData.potentialTAs]);
  const [searchTerm, setSearchTerm] = useState('');
  const [errorMsg, setErrorMsg] = useState<string|null>(null);

  // for exit and save confirmations
  const [showExitConfirm, setShowExitConfirm] = useState(false);
  const [showSaveConfirm, setShowSaveConfirm] = useState(false);

  // track initial assigned IDs
  const initialAssigned = useRef<string[]>(examData.assignedTAs.map(t => t.id));

  const leftCount = Math.max(0, needed - assigned.length);

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else if (assigned.length < needed) {
      setAssigned(a => [...a, ta]);
      setPotential(p => p.filter(x => x.id !== ta.id));
    } else {
      setErrorMsg(`Cannot select more than ${needed} TAs.`);
    }
  };

  const filtered = useMemo(() => {
    return potential
      .filter(t => t.name.toLowerCase().includes(searchTerm.toLowerCase()))
      .sort((a,b)=> a.workload - b.workload);
  }, [potential, searchTerm]);

  const handleBackClick = () => {
    const curr = assigned.map(t => t.id).sort().join();
    const init = initialAssigned.current.sort().join();
    if (curr !== init) {
      setShowExitConfirm(true);
    } else {
      navigate('/department-office/assign-proctor');
    }
  };

  const onConfirmExit = () => {
    setShowExitConfirm(false);
    // TODO: persist or discard as needed
    navigate('/department-office/assign-proctor');
  };

  const onCancelExit = () => {
    setShowExitConfirm(false);
  };

  const handleSaveClick = () => {
    setShowSaveConfirm(true);
  };

  const onConfirmSave = () => {
    // TODO: persist assigned changes to backend
    initialAssigned.current = assigned.map(t => t.id);
    setShowSaveConfirm(false);
    navigate('/department-office/assign-proctor');
  };

  const onCancelSave = () => {
    setShowSaveConfirm(false);
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={handleBackClick} />
        <h1 className={styles.title}>{examData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </div>

      <div className={styles.search}>
        <input
          type="text"
          placeholder="Search by name…"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
        />
      </div>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
            <th>Adjacent?</th>
            <th>Type</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(ta => (
            <tr
              key={ta.id}
              className={assigned.some(a => a.id === ta.id) ? styles.selectedRow : ''}
              onClick={() => toggleSelect(ta)}
            >
              <td>{ta.name}</td>
              <td>{ta.level}</td>
              <td>{ta.workload}</td>
              <td>{ta.hasAdjacentExam ? 'Yes' : 'No'}</td>
              <td>{ta.type}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => (
            <li key={ta.id} onClick={() => toggleSelect(ta)}>
              {ta.name} ×
            </li>
          ))}
        </ul>
      </div>

      <div className={styles.actions}>
        <GreenBut text="Save Changes" onClick={handleSaveClick} />
      </div>

      <div className={styles.info}>
        Full Time Workload Capacity: {no.fullCap} <br />
        Part Time Workload Capacity: {no.partCap} <br />
      </div>

      {errorMsg && <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />}

      {showSaveConfirm && (
        <ConPop
          message="Do you want to save the changes you made?"
          onConfirm={onConfirmSave}
          onCancel={onCancelSave}
        />
      )}

      {showExitConfirm && (
        <ConPop
          message="You have unsaved changes. Are you sure you want to leave?"
          onConfirm={onConfirmExit}
          onCancel={onCancelExit}
        />
      )}
    </div>
  );
};

export default AssignProctorTA;
 */