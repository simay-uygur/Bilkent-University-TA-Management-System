import React, { useState, useEffect, useMemo, useRef } from 'react';
import axios from 'axios';
import { useParams, useNavigate } from 'react-router-dom';
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
  type?: 'Full-time' | 'Part-time';
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

// Response type from the API
interface ProctorRequestResponse {
  requestId: number;
  requestType: string;
  description: string;
  senderName: string | null;
  receiverName: string;
  sentTime: {
    day: number;
    month: number;
    year: number;
    hour: number;
    minute: number;
  };
  instrId: number;
  examName: string;
  examId: number;
  requiredTas: number;
  tasLeft: number;
  rejected: boolean;
  approved: boolean;
  pending: boolean;
  assignedTas?: TA[]; // This might not be included in the response
}

const CAPACITY = {
  fullCap: 20,
  partCap: 10,
};

const AssignProctorTA: React.FC = () => {
  const navigate = useNavigate();
  // Update to extract both examID and requestID from URL parameters
  const { examID, requestID } = useParams<{ examID: string; requestID: string }>();
  
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

  // Update the useEffect to use requestID from path params
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        if (!requestID) {
          throw new Error("Request ID is missing");
        }
        
        // Step 1: Fetch the request details
        const requestResponse = await axios.get<ProctorRequestResponse>(
          `/api/request/${requestID}`
        );
        
        const requestDetails = requestResponse.data;
        console.log('Request details:', requestDetails);
        
        // Step 2: Fetch course details based on receiverName (which is the department code)
        const courseResponse = await axios.get(
          `/api/department/${requestDetails.receiverName}/exam/${requestDetails.examId}`
        ).catch(() => {
          // Fallback if this endpoint doesn't exist
          return {
            data: {
              courseName: requestDetails.description || "Unknown Course",
              courseCode: requestDetails.receiverName + "-464"  // Default course code
            }
          };
        });
        
        // Step 3: Construct the exam data from both responses
        const examInfo: ProctorRequestInfo = {
          requestId: requestDetails.requestId,
          examId: requestDetails.examId,
          examName: requestDetails.examName,
          courseName: courseResponse.data.courseName || requestDetails.description,
          courseCode: courseResponse.data.courseCode || `${requestDetails.receiverName}-464`,
          requiredTas: requestDetails.requiredTas,
          tasLeft: requestDetails.tasLeft,
          assignedTas: [] // We'll populate this next
        };
        
        // Step 4: Fetch assigned TAs if they exist
        let assignedTAs: TA[] = [];
        try {
          const assignedResponse = await axios.get(
            `/api/request/${requestID}/assigned-tas`
          );
          
          if (assignedResponse.data && Array.isArray(assignedResponse.data)) {
            assignedTAs = assignedResponse.data.map((ta: any) => ({
              id: ta.taId,
              name: `${ta.name} ${ta.surname}`,
              level: ta.academicLevel,
              workload: ta.workload || 0,
              hasAdjacentExam: ta.hasAdjacentExam || false
            }));
          }
        } catch (err) {
          console.warn("Could not fetch assigned TAs:", err);
          // Continue even if we can't get assigned TAs
        }
        
        examInfo.assignedTas = assignedTAs;
        setExamData(examInfo);
        
        // Step 5: Fetch available TAs for this exam
        try {
          const taResponse = await axios.get<AvailableTA[]>(
            `/api/course/${examInfo.courseCode}/proctoring/exam/${examInfo.examId}`
          );
          
          // Transform TAs into the format our component uses
          const transformedTAs: TA[] = taResponse.data.map(ta => ({
            id: ta.taId,
            name: `${ta.name} ${ta.surname}`,
            level: ta.academicLevel,
            workload: ta.workload,
            hasAdjacentExam: ta.hasAdjacentExam
          }));
          
          // Set assigned TAs from what we fetched
          if (assignedTAs.length > 0) {
            setAssigned(assignedTAs);
            initialAssigned.current = assignedTAs.map(ta => ta.id);
            
            // Filter out already assigned TAs from potential TAs
            const assignedIds = new Set(assignedTAs.map(ta => ta.id));
            setPotential(transformedTAs.filter(ta => !assignedIds.has(ta.id)));
          } else {
            // No TAs assigned yet
            setPotential(transformedTAs);
          }
        } catch (err) {
          console.error("Failed to fetch available TAs:", err);
          setError("Failed to load available TAs. Please try again later.");
        }
        
      } catch (err) {
        console.error("Failed to fetch request data:", err);
        setError("Failed to load exam data. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [examID, requestID]); // Update dependencies

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
      
      // Make API call to update assigned TAs - use requestID from params
      await axios.post(`/api/request/${requestID}/assign-tas`, {
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