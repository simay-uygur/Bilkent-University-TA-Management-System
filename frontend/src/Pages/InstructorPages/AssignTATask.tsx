import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import BackBut from '../../components/Buttons/BackBut';
import SearchSelect from '../../components/SearchSelect';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignTATask.module.css';
import LoadingPage from '../CommonPages/LoadingPage';

export interface TA {
  id: string;
  name: string;
  surname?: string;
  academicLevel?: string;
  totalWorkload?: number;
}

const AssignTATask: React.FC = () => {
  // Updated to use sectionCode and taskId
  const { sectionCode, taskId } = useParams<{ sectionCode: string; taskId: string }>();
  const navigate = useNavigate();
  
  // Parse the section code for display
  const parts = sectionCode?.split('-') || [];
  const courseCode = parts.length >= 2 ? `${parts[0]}-${parts[1]}` : '';
  const sectionNumber = parts.length >= 3 ? parts[2] : '1';

  // States for the component
  const [loading, setLoading] = useState(true);
  const [taskDetails, setTaskDetails] = useState<any>(null);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [allTAs, setAllTAs] = useState<TA[]>([]);
  const [alreadyAssigned, setAlreadyAssigned] = useState<TA[]>([]);
  const [selected, setSelected] = useState<TA[]>([]);
  const [resetKey, setResetKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  // Fetch TAs and task details
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Get task details first to see what TAs are already assigned
        const taskResponse = await axios.get(`/api/task/${taskId}`);
        const taskData = taskResponse.data;
        setTaskDetails(taskData);
        console.log('Task details:', taskData);
        
        // Extract TAs that are already assigned to this task
        const alreadyAssignedTAs = taskData.tas || [];
        console.log('TAs from task:', alreadyAssignedTAs);
        
        // Fetch all section TAs
        const allTAsResponse = await axios.get(`/api/ta/sectionCode/${sectionCode}`);
        const allTAsData = allTAsResponse.data;
        
        // Format all TAs
        const formattedAllTAs = allTAsData.map((ta: any) => ({
          id: ta.id.toString(),
          name: ta.name,
          surname: ta.surname,
          academicLevel: ta.academicLevel,
          totalWorkload: ta.totalWorkload || 0
        }));
        
        setAllTAs(formattedAllTAs);
        
        // Format the already assigned TAs
        const formattedAssignedTAs = alreadyAssignedTAs.map((ta: any) => ({
          id: ta.id.toString(),
          name: ta.name,
          surname: ta.surname || '',
          academicLevel: ta.academicLevel,
          totalWorkload: ta.totalWorkload || 0
        }));
        
        setAlreadyAssigned(formattedAssignedTAs);
        console.log('Already assigned TAs:', formattedAssignedTAs);
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching data:', err);
        setFetchError('Failed to load data. Please try again.');
        setLoading(false);
      }
    };
    
    fetchData();
  }, [sectionCode, taskId]);

  // Filter available TAs (not already assigned and not currently selected)
  const available = allTAs.filter(
    ta =>
      !alreadyAssigned.some(a => a.id === ta.id) &&
      !selected.some(s => s.id === ta.id)
  );

  const handleSelect = (ta: TA) => {
    setSelected(prev => [...prev, ta]);
    setResetKey(k => k + 1);
  };

  const handleRemoveSelected = (ta: TA) => {
    setSelected(prev => prev.filter(s => s.id !== ta.id));
  };

 const handleRemoveAssigned = async (ta: TA) => {
  try {
    // First update UI optimistically
    setAlreadyAssigned(prev => prev.filter(a => a.id !== ta.id));
    
    // Then make the background API call
    await axios.put(
      `/api/task/sectionCode/${sectionCode}/task/${taskId}/unassign`,
      [parseInt(ta.id, 10)] // Send as array of numbers
    );
    
    console.log(`Successfully unassigned TA ${ta.id} from task ${taskId}`);
  } catch (err) {
    console.error('Error unassigning TA:', err);
    
    // If API call fails, restore the TA back to the list
    setAlreadyAssigned(prev => [...prev, ta]);
    
    // Show a subtle error notification (optional)
    setError(`Failed to unassign ${ta.name}. Please try again.`);
    setTimeout(() => setError(null), 3000); // Auto-dismiss after 3s
  }
};
  const handleSubmit = () => {
    if (selected.length === 0 && alreadyAssigned.length === 0) {
      setError('Please have at least one TA assigned.');
    } else {
      setConfirmOpen(true);
    }
  };

  const handleConfirm = async () => {
    try {
      // Merge newly selected TAs with already assigned TAs
      const merged = [...alreadyAssigned, ...selected];
      console.log('Final assigned TAs:', merged, 'for section:', sectionCode, 'task:', taskId);
      
      // Extract just the TA IDs as numbers (API expects List<Long>)
      const taIds = merged.map(ta => parseInt(ta.id, 10));
      
      // Make the API call 
      await axios.put(
        `/api/task/sectionCode/${sectionCode}/task/${taskId}/assign`,
        taIds // Send the array of IDs directly as the request body
      );
      
      console.log('Successfully assigned TAs');
      setConfirmOpen(false);
      navigate(`/instructor/workload/${sectionCode}`);
    } catch (err) {
      console.error('Error assigning TAs:', err);
      setError('Failed to assign TAs. Please try again.');
      setConfirmOpen(false);
    }
  };
  
  // Format duration for display
  const formatTime = (time: any) => {
    if (!time) return '';
    const hour = time.hour.toString().padStart(2, '0');
    const minute = time.minute.toString().padStart(2, '0');
    return `${hour}:${minute}`;
  };
  
  const formatDate = (date: any) => {
    if (!date) return '';
    return `${date.day}/${date.month}/${date.year}`;
  };

  // Show loading state
  if (loading) {
    return <LoadingPage/>;
  }

  // Show error state
  if (fetchError) {
    return <div className={styles.error}>{fetchError}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to={`/instructor/workload/${sectionCode}`} />
        <h1 className={styles.title}>
          {courseCode} Section {sectionNumber} - Assign Task To TAs
        </h1>
      </div>
      
      {taskDetails && (
        <div className={styles.taskInfoCard}>
          <h2>{taskDetails.type}</h2>
          {taskDetails.description && <p>{taskDetails.description}</p>}
          {taskDetails.duration && (
            <div className={styles.taskTime}>
              <span>
                {formatDate(taskDetails.duration.start)} {formatTime(taskDetails.duration.start)}
              </span>
              <span>to</span>
              <span>
                {formatDate(taskDetails.duration.finish)} {formatTime(taskDetails.duration.finish)}
              </span>
            </div>
          )}
          <div className={styles.taskStatus}>
            Status: <span className={styles.statusBadge}>{taskDetails.status}</span>
          </div>
        </div>
      )}

      <div className={styles.mainContainer}>
        <SearchSelect<TA>
          key={resetKey}
          options={available}
          filterOption={ta => `${ta.name} ${ta.surname || ''}`}
          renderOption={ta => (
            <div className={styles.taOption}>
              <span>{ta.name} {ta.surname || ''}</span>
              <span className={styles.taLevel}>{ta.academicLevel || 'TA'}</span>
              <span className={styles.taWorkload}>Workload: {ta.totalWorkload || 0}</span>
            </div>
          )}
          placeholder="Search TAs…"
          onSelect={handleSelect}
          className={styles.search}
        />

        <div className={styles.assignedContainer}>
          <h2>Assigned TAs</h2>
          <div className={styles.tags}>
            {/* Already assigned TAs with different styling */}
            {alreadyAssigned.map(ta => (
              <span key={ta.id} className={`${styles.tag} ${styles.existingTag}`}>
                {ta.name} {ta.surname || ''}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveAssigned(ta)}
                  title="Remove this TA"
                >
                  ×
                </button>
              </span>
            ))}
            
            {/* Newly selected TAs */}
            {selected.map(ta => (
              <span key={ta.id} className={`${styles.tag} ${styles.newTag}`}>
                {ta.name} {ta.surname || ''}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveSelected(ta)}
                  title="Remove this TA"
                >
                  ×
                </button>
              </span>
            ))}
          </div>
        </div>

        <button className={styles.submitBtn} onClick={handleSubmit}>
          Confirm Assignment
        </button>
      </div>

      {error && <ErrPopUp message={error} onConfirm={() => setError(null)} />}

      {confirmOpen && (
        <ConPop
          message={`Assign ${alreadyAssigned.length + selected.length} TA${alreadyAssigned.length + selected.length > 1 ? 's' : ''} to this task?`}
          onConfirm={handleConfirm}
          onCancel={() => setConfirmOpen(false)}
        />
      )}
    </div>
  );
};

export default AssignTATask;
/*  import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import BackBut from '../../components/Buttons/BackBut';
import SearchSelect from '../../components/SearchSelect';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignTATask.module.css';
import LoadingPage from '../CommonPages/LoadingPage';

export interface TA {
  id: string;
  name: string;
  surname?: string;
  academicLevel?: string;
  totalWorkload?: number;
}

const AssignTATask: React.FC = () => {
  // Updated to use sectionCode and taskId
  const { sectionCode, taskId } = useParams<{ sectionCode: string; taskId: string }>();
  const navigate = useNavigate();
  
  // Parse the section code for display
  const parts = sectionCode?.split('-') || [];
  const courseCode = parts.length >= 2 ? `${parts[0]}-${parts[1]}` : '';
  const sectionNumber = parts.length >= 3 ? parts[2] : '1';

  // States for the component
  const [loading, setLoading] = useState(true);
  const [taskDetails, setTaskDetails] = useState<any>(null);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [allTAs, setAllTAs] = useState<TA[]>([]);
  const [alreadyAssigned, setAlreadyAssigned] = useState<TA[]>([]);
  const [selected, setSelected] = useState<TA[]>([]);
  const [resetKey, setResetKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  // Fetch TAs and task details
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Get task details first to see what TAs are already assigned
        const taskResponse = await axios.get(`/api/task/${taskId}`);
        const taskData = taskResponse.data;
        setTaskDetails(taskData);
        console.log('Task details:', taskData);
        
        // Fetch all section TAs
        const allTAsResponse = await axios.get(`/api/ta/sectionCode/${sectionCode}`);
        const allTAsData = allTAsResponse.data;
        
        // Format all TAs
        const formattedAllTAs = allTAsData.map((ta: any) => ({
          id: ta.id.toString(),
          name: ta.name,
          surname: ta.surname,
          academicLevel: ta.academicLevel,
          totalWorkload: ta.totalWorkload || 0
        }));
        
        setAllTAs(formattedAllTAs);
        
        // Fetch currently assigned TAs for this task
        const assignedResponse = await axios.get(`api/sections/section/task/{task_id}`);
        
        const assignedTAsData = assignedResponse.data;
        
        // Format assigned TAs
        const formattedAssignedTAs = assignedTAsData.map((ta: any) => ({
          id: ta.id.toString(),
          name: ta.name,
          surname: ta.surname,
          academicLevel: ta.academicLevel,
          totalWorkload: ta.totalWorkload || 0
        }));
        
        setAlreadyAssigned(formattedAssignedTAs);
        console.log('Already assigned TAs:', formattedAssignedTAs);
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching data:', err);
        setFetchError('Failed to load data. Please try again.');
        setLoading(false);
      }
    };
    
    fetchData();
  }, [sectionCode, taskId]);

  // Filter available TAs (not already assigned and not currently selected)
  const available = allTAs.filter(
    ta =>
      !alreadyAssigned.some(a => a.id === ta.id) &&
      !selected.some(s => s.id === ta.id)
  );

  const handleSelect = (ta: TA) => {
    setSelected(prev => [...prev, ta]);
    setResetKey(k => k + 1);
  };

  const handleRemoveSelected = (ta: TA) => {
    setSelected(prev => prev.filter(s => s.id !== ta.id));
  };

  const handleRemoveAssigned = (ta: TA) => {
    setAlreadyAssigned(prev => prev.filter(a => a.id !== ta.id));
  };

  const handleSubmit = () => {
    if (selected.length === 0 && alreadyAssigned.length === 0) {
      setError('Please have at least one TA assigned.');
    } else {
      setConfirmOpen(true);
    }
  };

  const handleConfirm = async () => {
    try {
      // Merge newly selected TAs with already assigned TAs
      const merged = [...alreadyAssigned, ...selected];
      console.log('Final assigned TAs:', merged, 'for section:', sectionCode, 'task:', taskId);
      
      // Extract just the TA IDs as numbers (API expects List<Long>)
      const taIds = merged.map(ta => parseInt(ta.id, 10));
      
      // Make the API call 
      await axios.put(
        `/api/task/sectionCode/${sectionCode}/task/${taskId}/assign`,
        taIds // Send the array of IDs directly as the request body
      );
      
      console.log('Successfully assigned TAs');
      setConfirmOpen(false);
      navigate(`/instructor/workload/${sectionCode}`);
    } catch (err) {
      console.error('Error assigning TAs:', err);
      setError('Failed to assign TAs. Please try again.');
      setConfirmOpen(false);
    }
  };

  // Show loading state
  if (loading) {
    return <LoadingPage/>;
  }

  // Show error state
  if (fetchError) {
    return <div className={styles.error}>{fetchError}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to={`/instructor/workload/${sectionCode}`} />
        <h1 className={styles.title}>
          {courseCode} Section {sectionNumber} - Assign Task To TAs
        </h1>
        {taskDetails && (
          <div className={styles.taskDetails}>
            <span className={styles.taskType}>{taskDetails.type}</span>
            <span className={styles.taskDesc}>{taskDetails.description}</span>
          </div>
        )}
      </div>

      <div className={styles.mainContainer}>
        <SearchSelect<TA>
          key={resetKey}
          options={available}
          filterOption={ta => `${ta.name} ${ta.surname || ''}`}
          renderOption={ta => (
            <div className={styles.taOption}>
              <span>{ta.name} {ta.surname || ''}</span>
              <span className={styles.taLevel}>{ta.academicLevel || 'TA'}</span>
              <span className={styles.taWorkload}>Workload: {ta.totalWorkload || 0}</span>
            </div>
          )}
          placeholder="Search TAs…"
          onSelect={handleSelect}
          className={styles.search}
        />

        <div className={styles.assignedContainer}>
          <h2>Assigned TAs</h2>
          <div className={styles.tags}>
            
            {alreadyAssigned.map(ta => (
              <span key={ta.id} className={`${styles.tag} ${styles.existingTag}`}>
                {ta.name} {ta.surname || ''}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveAssigned(ta)}
                  title="Remove this TA"
                >
                  ×
                </button>
              </span>
            ))}
            
            
            {selected.map(ta => (
              <span key={ta.id} className={`${styles.tag} ${styles.newTag}`}>
                {ta.name} {ta.surname || ''}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveSelected(ta)}
                  title="Remove this TA"
                >
                  ×
                </button>
              </span>
            ))}
          </div>
        </div>

        <button className={styles.submitBtn} onClick={handleSubmit}>
          Confirm Assignment
        </button>
      </div>

      {error && <ErrPopUp message={error} onConfirm={() => setError(null)} />}

      {confirmOpen && (
        <ConPop
          message={`Assign ${alreadyAssigned.length + selected.length} TA${alreadyAssigned.length + selected.length > 1 ? 's' : ''} to this task?`}
          onConfirm={handleConfirm}
          onCancel={() => setConfirmOpen(false)}
        />
      )}
    </div>
  );
};

export default AssignTATask; */
 /* import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import BackBut from '../../components/Buttons/BackBut';
import SearchSelect from '../../components/SearchSelect';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignTATask.module.css';
import LoadingPage from '../CommonPages/LoadingPage';

export interface TA {
  id: string;
  name: string;
  surname?: string;
  academicLevel?: string;
  totalWorkload?: number;
}

const AssignTATask: React.FC = () => {
  // Updated to use sectionCode and taskId
  const { sectionCode, taskId } = useParams<{ sectionCode: string; taskId: string }>();
  const navigate = useNavigate();
  
  // Parse the section code for display
  const parts = sectionCode?.split('-') || [];
  const courseCode = parts.length >= 2 ? `${parts[0]}-${parts[1]}` : '';
  const sectionNumber = parts.length >= 3 ? parts[2] : '1';

  // Add loading and fetch states
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [allTAs, setAllTAs] = useState<TA[]>([]);
  
  // pre-existing assigned
  const [alreadyAssigned, setAlreadyAssigned] = useState<TA[]>([]);
  // newly selected
  const [selected, setSelected] = useState<TA[]>([]);
  const [resetKey, setResetKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  // Fetch TAs from API
  useEffect(() => {
    const fetchTAs = async () => {
      try {
        setLoading(true);
        
        // Fetch TAs for the section
        const response = await axios.get(`/api/ta/sectionCode/${sectionCode}`);
        console.log('Fetched TAs:', response.data);
        
        // Format TA data
        const formattedTAs = response.data.map((ta: any) => ({
          id: ta.id.toString(),
          name: ta.name,
          surname: ta.surname,
          academicLevel: ta.academicLevel,
          totalWorkload: ta.totalWorkload || 0
        }));
        
        setAllTAs(formattedTAs);
        
        // TODO: Fetch already assigned TAs for this specific task
        // For now, we'll leave this empty
        setAlreadyAssigned([]);
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching TAs:', err);
        setFetchError('Failed to load TAs. Please try again.');
        setLoading(false);
      }
    };
    
    fetchTAs();
  }, [sectionCode, taskId]);

  // Filter available TAs (not already assigned and not currently selected)
  const available = allTAs.filter(
    ta =>
      !alreadyAssigned.some(a => a.id === ta.id) &&
      !selected.some(s => s.id === ta.id)
  );

  const handleSelect = (ta: TA) => {
    setSelected(prev => [...prev, ta]);
    setResetKey(k => k + 1);
  };

  const handleRemoveSelected = (ta: TA) => {
    setSelected(prev => prev.filter(s => s.id !== ta.id));
  };

  const handleRemoveAssigned = (ta: TA) => {
    setAlreadyAssigned(prev => prev.filter(a => a.id !== ta.id));
  };

  const handleSubmit = () => {
    if (selected.length === 0 && alreadyAssigned.length === 0) {
      setError('Please have at least one TA assigned.');
    } else {
      setConfirmOpen(true);
    }
  };

  const handleConfirm = async () => {
    try {
      // merge new into existing
        // Merge newly selected TAs with already assigned TAs
    const merged = [...alreadyAssigned, ...selected];
    console.log('Final assigned TAs:', merged, 'for section:', sectionCode, 'task:', taskId);
    
    // Extract just the TA IDs as numbers (API expects List<Long>)
    const taIds = merged.map(ta => parseInt(ta.id, 10));
    
    // Make the API call based on the provided endpoint
    await axios.put(
      `/api/task/sectionCode/${sectionCode}/task/${taskId}/assign`,
      taIds // Send the array of IDs directly as the request body
    );
    
    console.log('Successfully assigned TAs');
      
      setConfirmOpen(false);
      navigate(`/instructor/workload/${sectionCode}`);
    } catch (err) {
      console.error('Error assigning TAs:', err);
      setError('Failed to assign TAs. Please try again.');
      setConfirmOpen(false);
    }
  };

  // Show loading state
  if (loading) {
    return <LoadingPage/>;
  }

  // Show error state
  if (fetchError) {
    return <div className={styles.error}>{fetchError}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to={`/instructor/workload/${sectionCode}`} />
        <h1 className={styles.title}>
          {courseCode} Section {sectionNumber} - Assign Task To TAs
        </h1>
      </div>

      <div className={styles.mainContainer}>
        <SearchSelect<TA>
          key={resetKey}
          options={available}
          filterOption={ta => `${ta.name} ${ta.surname || ''}`}
          renderOption={ta => <>{ta.name} {ta.surname || ''} ({ta.academicLevel || 'TA'})</>}
          placeholder="Search TAs…"
          onSelect={handleSelect}
          className={styles.search}
        />

        <div className={styles.assignedContainer}>
          <h2>Assigned TAs</h2>
          <div className={styles.tags}>
            {alreadyAssigned.map(ta => (
              <span key={ta.id} className={styles.tag}>
                {ta.name} {ta.surname || ''}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveAssigned(ta)}
                >
                  ×
                </button>
              </span>
            ))}
            {selected.map(ta => (
              <span key={ta.id} className={styles.tag}>
                {ta.name} {ta.surname || ''}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveSelected(ta)}
                >
                  ×
                </button>
              </span>
            ))}
          </div>
        </div>

        <button className={styles.submitBtn} onClick={handleSubmit}>
          Confirm Assignment
        </button>
      </div>

      {error && <ErrPopUp message={error} onConfirm={() => setError(null)} />}

      {confirmOpen && (
        <ConPop
          message={`Assign ${alreadyAssigned.length + selected.length} TA${alreadyAssigned.length + selected.length > 1 ? 's' : ''}?`}
          onConfirm={handleConfirm}
          onCancel={() => setConfirmOpen(false)}
        />
      )}
    </div>
  );
}; 

export default AssignTATask; */
 /* 
import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import SearchSelect from '../../components/SearchSelect';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignTATask.module.css';

export interface TA {
  id: string;
  name: string;
}

const sampleTAs: TA[] = [
  { id: 'ta1', name: 'Ali Veli' },
  { id: 'ta2', name: 'Ayşe Fatma' },
  { id: 'ta3', name: 'Mehmet Can' },
  { id: 'ta4', name: 'Elif Yilmaz' },
];

const AssignTATask: React.FC = () => {
  const { courseID } = useParams<{ courseID: string }>()
  const { courseSec } = useParams<{ courseSec: string }>()
  const navigate = useNavigate();

  // pre-existing assigned
  const [alreadyAssigned, setAlreadyAssigned] = useState<TA[]>([
    { id: 'ta2', name: 'Ayşe Fatma' },
  ]);
  // newly selected
  const [selected, setSelected] = useState<TA[]>([]);
  const [resetKey, setResetKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const available = sampleTAs.filter(
    ta =>
      !alreadyAssigned.some(a => a.id === ta.id) &&
      !selected.some(s => s.id === ta.id)
  );

  const handleSelect = (ta: TA) => {
    setSelected(prev => [...prev, ta]);
    setResetKey(k => k + 1);
  };

  const handleRemoveSelected = (ta: TA) => {
    setSelected(prev => prev.filter(s => s.id !== ta.id));
  };

  const handleRemoveAssigned = (ta: TA) => {
    setAlreadyAssigned(prev => prev.filter(a => a.id !== ta.id));
  };

  const handleSubmit = () => {
    if (selected.length === 0 && alreadyAssigned.length === 0) {
      setError('Please have at least one TA assigned.');
    } else {
      setConfirmOpen(true);
    }
  };

  const handleConfirm = () => {
    // merge new into existing
    const merged = [...alreadyAssigned, ...selected];
    console.log('Final assigned:', merged, 'for course', courseID);
    // TODO: send merged to backend
    setConfirmOpen(false);
    navigate(`/instructor/workload/${courseID}/${courseSec}`);
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to={`/instructor/workload/${courseID}/${courseSec}`} />
        <h1 className={styles.title}>Assign Task To TAs</h1>
      </div>

      <div className={styles.mainContainer}>
        <SearchSelect<TA>
          key={resetKey}
          options={available}
          filterOption={ta => ta.name}
          renderOption={ta => <>{ta.name}</>}
          placeholder="Search TAs…"
          onSelect={handleSelect}
          className={styles.search}
        />

        <div className={styles.assignedContainer}>
          <h2>Assigned TAs</h2>
          <div className={styles.tags}>
            {alreadyAssigned.map(ta => (
              <span key={ta.id} className={styles.tag}>
                {ta.name}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveAssigned(ta)}
                >
                  ×
                </button>
              </span>
            ))}
            {selected.map(ta => (
              <span key={ta.id} className={styles.tag}>
                {ta.name}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveSelected(ta)}
                >
                  ×
                </button>
              </span>
            ))}
          </div>
        </div>

        <button className={styles.submitBtn} onClick={handleSubmit}>
          Confirm Assignment
        </button>
      </div>

      {error && <ErrPopUp message={error} onConfirm={() => setError(null)} />}

      {confirmOpen && (
        <ConPop
          message={`Assign ${alreadyAssigned.length + selected.length} TA${alreadyAssigned.length + selected.length > 1 ? 's' : ''}?`}
          onConfirm={handleConfirm}
          onCancel={() => setConfirmOpen(false)}
        />
      )}
    </div>
  );
};

export default AssignTATask; */