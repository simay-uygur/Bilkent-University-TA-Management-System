import React, { useState, useMemo, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectCourseTA.module.css';
import axios from 'axios';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

const SelectCourseTA: React.FC = () => {
  const navigate = useNavigate();
  // Change from courseId to sectionCode
  const { sectionCode } = useParams<{ sectionCode: string }>();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [courseData, setCourseData] = useState<Course>({
    id: sectionCode || 'c2',
    // Format the section code to look better in the UI
    courseName: sectionCode ? formatSectionName(sectionCode) : 'CS202 – Data Structures',
    needed: 3,
    assignedTAs: [],
    potentialTAs: [],
  });

  // Helper function to format section name for display
  function formatSectionName(code: string): string {
    const parts = code.split('-');
    if (parts.length >= 5) {
      return `${parts[0]}-${parts[1]} Section ${parts[2]} (${parts[4]} ${parts[3]})`;
    }
    return code;
  }
  
  // Fetch TAs from API
  useEffect(() => {
    const fetchTAs = async () => {
      try {
        setLoading(true);
        // Use relative URL to avoid CORS issues
        const response = await axios.get('/api/ta/department/CS');
        
        // Transform API response to match our component's expected format
        const fetchedTAs = response.data.map((ta: any) => ({
          id: ta.id.toString(),
          name: `${ta.name} ${ta.surname}`,
          level: ta.academicLevel === 'PHD' ? 'PhD' : 
                 ta.academicLevel === 'MSC' ? 'MS' : 'BS',
          workload: ta.totalWorkload || 0,
          hasAdjacentExam: false, // Default value
          preference: 'normal' // Default value
        }));
        
        setCourseData(prev => ({
          ...prev,
          potentialTAs: fetchedTAs
        }));
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching TAs:', err);
        setError('Failed to load TAs. Please try again.');
        setLoading(false);
      }
    };
    
    fetchTAs();
  }, [sectionCode]);

  const needed = courseData.needed;
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  
  // Update potential TAs when courseData changes
  useEffect(() => {
    setPotential(courseData.potentialTAs);
  }, [courseData.potentialTAs]);

  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);

  const leftCount = Math.max(0, needed - assigned.length);
  
  const assignTA = (ta: TA) => {
    const newAssigned = [...assigned, ta];
    setAssigned(newAssigned);
    setPotential(p => p.filter(a => a.id !== ta.id));
  };

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };

  const handleUnprefConfirm = () => {
    if (selectedUnpref) assignTA(selectedUnpref);
    setShowUnprefConfirm(false);
  };

  const sortedPotential = useMemo(
    () => [...potential].sort((a, b) => a.workload - b.workload),
    [potential]
  );

  const handleConfirm = () => setConfirmAssign(true);
  
  const doConfirm = async () => { 
    try {
      // Update the API endpoint to match the backend structure
      for (const ta of assigned) {
        await axios.post(`/api/sections/${sectionCode}/tas/${ta.id}`);
        console.log(`Assigned TA ${ta.id} to section ${sectionCode}`);
      }
      setConfirmAssign(false);
      navigate('/department-office/assign-course');
    } catch (err) {
      console.error('Error assigning TAs:', err);
      alert('Failed to assign TAs. Please try again.');
      setConfirmAssign(false);
    }
  };
  
  if (loading) {
    return <div className={styles.loading}>Loading TAs...</div>;
  }

  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.header}>
        <BackBut to="/department-office/assign-course" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
          </tr>
        </thead>
        <tbody>
          {sortedPotential.map(ta => {
            const isAssigned = assigned.some(a => a.id === ta.id);
            const rowClass = isAssigned
              ? styles.assignedRow
              : ta.preference === 'preferred'
              ? styles.preferredRow
              : ta.preference === 'unpreferred'
              ? styles.unpreferredRow
              : '';
            return (
              <tr
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={`${styles.rowBase} ${rowClass}`}
              >
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'
                ? styles.preferredItem
                : ta.preference === 'unpreferred'
                ? styles.unpreferredItem
                : styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button 
          className={styles.confirmButton} 
          onClick={handleConfirm}
          disabled={assigned.length === 0}
        >
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={doConfirm}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectCourseTA;
/* // src/pages/AssignTA/SelectTACourse.tsx
import React, { useState, useMemo, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import DepOfNavBar from '../../components/NavBars/DepOfNavBar';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectTACourse.module.css';
import axios from 'axios';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

const SelectTACourse: React.FC = () => {
  const navigate = useNavigate();
  const { courseId } = useParams<{ courseId: string }>();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [courseData, setCourseData] = useState<Course>({
    id: courseId || 'c2',
    courseName: courseId ? `Course ${courseId}` : 'CS202 – Data Structures',
    needed: 3,
    assignedTAs: [],
    potentialTAs: [],
  });
  
  // Fetch TAs from API
  useEffect(() => {
    const fetchTAs = async () => {
      try {
        setLoading(true);
        const response = await axios.get('http://localhost:5173/api/ta/department/CS');
        
        // Transform API response to match our component's expected format
        const fetchedTAs = response.data.map((ta: any) => ({
          id: ta.id.toString(),
          name: `${ta.name} ${ta.surname}`,
          level: ta.academicLevel === 'PHD' ? 'PhD' : 
                 ta.academicLevel === 'MSC' ? 'MS' : 'BS',
          workload: ta.totalWorkload || 0,
          hasAdjacentExam: false, // Default value
          preference: 'normal' // Default value
        }));
        
        setCourseData(prev => ({
          ...prev,
          potentialTAs: fetchedTAs
        }));
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching TAs:', err);
        setError('Failed to load TAs. Please try again.');
        setLoading(false);
      }
    };
    
    fetchTAs();
  }, [courseId]);

  const needed = courseData.needed;
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  
  // Update potential TAs when courseData changes
  useEffect(() => {
    setPotential(courseData.potentialTAs);
  }, [courseData.potentialTAs]);

  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);

  const leftCount = Math.max(0, needed - assigned.length);
  
  // Rest of your component functionality remains the same
  
  const assignTA = (ta: TA) => {
    const newAssigned = [...assigned, ta];
    setAssigned(newAssigned);
    setPotential(p => p.filter(a => a.id !== ta.id));
  };

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };

  const handleUnprefConfirm = () => {
    if (selectedUnpref) assignTA(selectedUnpref);
    setShowUnprefConfirm(false);
  };

  const sortedPotential = useMemo(
    () => [...potential].sort((a, b) => a.workload - b.workload),
    [potential]
  );

  const handleConfirm = () => setConfirmAssign(true);
  
  const doConfirm = async () => { 
    try {
      // Save assignments to backend
      for (const ta of assigned) {
        await axios.post(`http://localhost:5173/api/sections/${courseId}/tas/${ta.id}`);
      }
      setConfirmAssign(false);
      navigate('/department-office/assign-course');
    } catch (err) {
      console.error('Error assigning TAs:', err);
      alert('Failed to assign TAs. Please try again.' + err);
      setConfirmAssign(false);
    }
  };
  
  if (loading) {
    return <div className={styles.loading}>Loading TAs...</div>;
  }

  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.header}>
        <BackBut to="/department-office/assign-course" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

     // {Rest of your component rendering remains the same }
      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
          </tr>
        </thead>
        <tbody>
          {sortedPotential.map(ta => {
            const isAssigned = assigned.some(a => a.id === ta.id);
            const rowClass = isAssigned
              ? styles.assignedRow
              : ta.preference === 'preferred'
              ? styles.preferredRow
              : ta.preference === 'unpreferred'
              ? styles.unpreferredRow
              : '';
            return (
              <tr
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={`${styles.rowBase} ${rowClass}`}
              >
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'
                ? styles.preferredItem
                : ta.preference === 'unpreferred'
                ? styles.unpreferredItem
                : styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button 
          className={styles.confirmButton} 
          onClick={handleConfirm}
          disabled={assigned.length === 0}
        >
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={doConfirm}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectTACourse; */
/* // src/pages/AssignTA/SelectTACourse.tsx
import React, { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import DepOfNavBar from '../../components/NavBars/DepOfNavBar';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectTACourse.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

const sampleCourses: Course[] = [
  {
    id: 'c2',
    courseName: 'CS202 – Data Structures',
    needed: 3,
    assignedTAs: [],
    potentialTAs: [
      { id: 't4', name: 'David Kim', level: 'MS', workload: 4, hasAdjacentExam: false, preference: 'preferred' },
      { id: 't5', name: 'Eva Martinez', level: 'BS', workload: 2, hasAdjacentExam: false, preference: 'unpreferred' },
      { id: 't6', name: 'Frank Zhou', level: 'PhD', workload: 6, hasAdjacentExam: true, preference: 'normal' },
    ],
  },
];

const SelectTACourse: React.FC = () => {
  const navigate = useNavigate();
  const { courseId } = useParams<{ courseId: string }>();
  const courseData = sampleCourses.find(c => c.id === courseId) || sampleCourses[0];

  const needed = courseData.needed;
  const [assigned, setAssigned] = useState<TA[]>([...courseData.assignedTAs]);
  const [potential, setPotential] = useState<TA[]>([...courseData.potentialTAs]);

  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);

  const leftCount = Math.max(0, needed - assigned.length);
  courseData.tasLeft = leftCount;

  const assignTA = (ta: TA) => {
    const newAssigned = [...assigned, ta];
    setAssigned(newAssigned);
    setPotential(p => p.filter(a => a.id !== ta.id));
  };

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };

  const handleUnprefConfirm = () => {
    if (selectedUnpref) assignTA(selectedUnpref);
    setShowUnprefConfirm(false);
  };

  const sortedPotential = useMemo(
    () => [...potential].sort((a, b) => a.workload - b.workload),
    [potential]
  );

  const handleConfirm = () => setConfirmAssign(true);
  const doConfirm = () => { setConfirmAssign(false); navigate('/asgnTAC'); };

  return (
    <div className={styles.pageWrapper}>
      

      <header className={styles.header}>
        <BackBut to="/asgnTAC" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
          </tr>
        </thead>
        <tbody>
          {sortedPotential.map(ta => {
            const isAssigned = assigned.some(a => a.id === ta.id);
            const rowClass = isAssigned
              ? styles.assignedRow
              : ta.preference === 'preferred'
              ? styles.preferredRow
              : ta.preference === 'unpreferred'
              ? styles.unpreferredRow
              : '';
            return (
              <tr
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={`${styles.rowBase} ${rowClass}`}
              >
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'
                ? styles.preferredItem
                : ta.preference === 'unpreferred'
                ? styles.unpreferredItem
                : styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button className={styles.confirmButton} onClick={handleConfirm}>
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={doConfirm}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectTACourse;
 */