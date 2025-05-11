import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './DepartmentOfficeTaDetails.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import BackBut from '../../components/Buttons/BackBut';

// Update interface to match the new API response format
interface TaskDto {
  taskId: number;
  taId: number;
  type: string;
  description?: string;
  sectionCode?: string;
  duration: {
    start: {
      day: number;
      month: number;
      year: number;
      hour: number;
      minute: number;
    };
    finish: {
      day: number;
      month: number;
      year: number;
      hour: number;
      minute: number;
    };
    ongoing: boolean;
  };
  status: string;
  workload: number;
}

interface SectionDto {
  sectionId: number;
  sectionCode: string;
  courseName: string;
}

interface TaProfile {
  id: number;
  name: string;
  surname: string;
  academicLevel: string;
  totalWorkload: number;
}

const TaDetailedPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const taId = id!;
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [profile, setProfile] = useState<TaProfile | null>(null);
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [sections, setSections] = useState<SectionDto[]>([]);
  const [newSectionCode, setNewSectionCode] = useState('');
  
  // State for confirmation dialogs
  const [confirmUnassignSection, setConfirmUnassignSection] = useState<SectionDto | null>(null);
  const [confirmDeleteTask, setConfirmDeleteTask] = useState<TaskDto | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Helper function to format dates from the new structure
  const formatDate = (dateObj: { day: number; month: number; year: number; hour: number; minute: number; }) => {
    const date = `${dateObj.year}-${String(dateObj.month).padStart(2, '0')}-${String(dateObj.day).padStart(2, '0')}`;
    const time = `${String(dateObj.hour).padStart(2, '0')}:${String(dateObj.minute).padStart(2, '0')}`;
    return `${date} ${time}`;
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [profileRes, tasksRes, sectionsRes] = await Promise.all([
          axios.get(`/api/ta/${taId}`),
          axios.get(`/api/ta/${taId}/tasks`),
          axios.get(`/api/ta/${taId}/sections`),
        ]);
        
        setProfile(profileRes.data);
        
        // Log the tasks response to verify the format
        console.log('Tasks API Response:', tasksRes.data);
        
        // Set tasks directly from the API response
        setTasks(tasksRes.data);
        setSections(sectionsRes.data);
      } catch (err) {
        console.error(err);
        setError('Failed to load TA details.');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [taId]);

  const handleDeleteTask = async (task: TaskDto) => {
    // Add debugging to verify the task object
    console.log('Setting task for deletion:', task);
    console.log('Task ID:', task.taskId);
    
    // Ensure we have a valid taskId before setting the confirmation
    if (task && task.taskId !== undefined) {
      setConfirmDeleteTask(task);
    } else {
      setErrorMessage('Cannot unassign task: missing task ID');
    }
  };

  const confirmTaskDeletion = async () => {
    if (!confirmDeleteTask) return;
    
    try {
      // Let's add debugging to verify the task data
      console.log('Task to delete:', confirmDeleteTask);
      console.log('Task ID:', confirmDeleteTask.taskId);
      
      // Use the correct API endpoint for unassigning from a task
      if (confirmDeleteTask.sectionCode) {
        await axios.post(
          `/api/task/sectionCode/${confirmDeleteTask.sectionCode}/task/${confirmDeleteTask.taskId}/unassign`,
          { taId: taId } // Make sure to include the TA ID in the request body if needed
        );
      } else {
        // Fallback to the original endpoint if there's no section code
        await axios.delete(`/api/ta/${taId}/tasks/${confirmDeleteTask.taskId}/unassign`);
      }
      
      // Update the local state to remove the task
      setTasks(tasks.filter(t => t.taskId !== confirmDeleteTask.taskId));
      setSuccessMessage('Task has been unassigned successfully.');
      
      // Clear confirmation after short delay
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Error unassigning task:', err);
      // Log the exact error for debugging
      if (axios.isAxiosError(err) && err.response) {
        console.error('Error response:', err.response.data);
        console.error('Status code:', err.response.status);
      }
      setErrorMessage('Failed to unassign task. Please try again.');
    } finally {
      setConfirmDeleteTask(null);
    }
  };

  const handleUnassignSection = (section: SectionDto) => {
    setConfirmUnassignSection(section);
  };

  const confirmSectionUnassignment = async () => {
    if (!confirmUnassignSection) return;
    
    try {
      // Use the correct API endpoint for unassigning from a section
      await axios.post(`/api/sections/${confirmUnassignSection.sectionCode}/tas/${taId}/unassign`);
      
      setSections(sections.filter(s => s.sectionId !== confirmUnassignSection.sectionId));
      setSuccessMessage('Section has been unassigned successfully.');
      
      // Clear confirmation after short delay
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Error unassigning section:', err);
      setErrorMessage('Failed to unassign section. Please try again.');
    } finally {
      setConfirmUnassignSection(null);
    }
  };

  const handleAssignSection = async () => {
    if (!newSectionCode) {
      setErrorMessage('Please enter a section code');
      return;
    }
    
    try {
      // Use the correct API endpoint for assigning a TA to a section
      await axios.post(`/api/sections/${newSectionCode}/tas/${taId}`);
      
      // After successful assignment, fetch the section details to add to the list
      // This is needed because the assignment endpoint might not return the section details
      try {
        const sectionResponse = await axios.get(`/api/sections/${newSectionCode}`);
        const newSection = {
          sectionId: sectionResponse.data.sectionId || Date.now(), // Fallback ID if none is provided
          sectionCode: newSectionCode,
          courseName: sectionResponse.data.courseName || `Course for ${newSectionCode}`
        };
        
        setSections([...sections, newSection]);
      } catch (sectionErr) {
        console.warn('Could not fetch section details:', sectionErr);
        // Add with minimal info if section details fetch fails
        const newSection = {
          sectionId: Date.now(),
          sectionCode: newSectionCode,
          courseName: 'Unknown Course'
        };
        setSections([...sections, newSection]);
      }
      
      setNewSectionCode('');
      setSuccessMessage('TA has been assigned to the section successfully.');
      
      // Clear message after short delay
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Error assigning TA to section:', err);
      
      // More detailed error logging
      if (axios.isAxiosError(err) && err.response) {
        console.error('Error response:', err.response.data);
        console.error('Status code:', err.response.status);
      }
      
      setErrorMessage('Failed to assign TA to section. Please check the section code and try again.');
    }
  };

  if (loading) return <LoadingPage />;
  if (error) return (
    <div className={styles.errorContainer}>
      <p className={styles.errorMessage}>{error}</p>
      <button 
        className={styles.retryButton} 
        onClick={() => window.location.reload()}
      >
        Retry
      </button>
    </div>
  );

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut onClick={() => navigate(-1)} />
        <h1 className={styles.heading}>TA Details</h1>
      </div>
      
      <div className={styles.profileCard}>
        <div className={styles.profileHeader}>
          <h2 className={styles.profileName}>{profile?.name} {profile?.surname}</h2>
          <div className={styles.profileBadge}>{profile?.academicLevel}</div>
        </div>
        <div className={styles.profileDetails}>
          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>Total Workload:</span>
            <span className={styles.detailValue}>{profile?.totalWorkload}</span>
          </div>
          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>ID:</span>
            <span className={styles.detailValue}>{profile?.id}</span>
          </div>
        </div>
      </div>

      {/* Success message banner */}
      {successMessage && (
        <div className={styles.successBanner}>
          {successMessage}
        </div>
      )}

      {/* Tasks */}
      <div className={styles.card}>
        <h2 className={styles.tableHeading}>Tasks</h2>
        <div className={styles.tableContainer}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Type</th>
                <th>Description</th>
                <th>Section</th>
                <th>Duration</th>
                <th>Status</th>
                <th>Workload</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {tasks.length > 0 ? tasks.map(task => (
                <tr key={task.taskId}>
                  <td>{task.type}</td>
                  <td>{task.description || '-'}</td>
                  <td>{task.sectionCode || '-'}</td>
                  <td>
                    {task.duration && task.duration.start ? 
                      formatDate(task.duration.start) : 'Unknown'} –{' '}
                    {task.duration && task.duration.ongoing ? 
                      'Ongoing' : 
                      (task.duration && task.duration.finish ? formatDate(task.duration.finish) : 'Unknown')}
                  </td>
                  <td>
                    <span className={`${styles.status} ${task.status ? styles[task.status.toLowerCase()] : ''}`}>
                      {task.status}
                    </span>
                  </td>
                  <td>{task.workload}</td>
                  <td>
                    <button
                      className={styles.actionButton}
                      onClick={() => handleDeleteTask(task)}
                    >
                      Unassign
                    </button>
                  </td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={7} className={styles.emptyState}>
                    No tasks assigned to this TA
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Enrolled Sections */}
      <div className={styles.card}>
        <h2 className={styles.tableHeading}>Enrolled Sections</h2>
        <div className={styles.tableContainer}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Section Code</th>
                <th>Course Name</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {sections.length > 0 ? sections.map(section => (
                <tr key={section.sectionId}>
                  <td>{section.sectionCode}</td>
                  <td>{section.courseName}</td>
                  <td>
                    <button
                      className={styles.actionButton}
                      onClick={() => handleUnassignSection(section)}
                    >
                      Unassign
                    </button>
                  </td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={3} className={styles.emptyState}>
                    No sections assigned to this TA
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className={styles.formGroup}>
          <input
            className={styles.input}
            type="text"
            placeholder="Enter Section Code"
            value={newSectionCode}
            onChange={e => setNewSectionCode(e.target.value)}
          />
          <button
            className={styles.assignButton}
            onClick={handleAssignSection}
            disabled={!newSectionCode}
          >
            Assign Section
          </button>
        </div>
      </div>

      {/* Error popup */}
      {errorMessage && (
        <ErrPopUp 
          message={errorMessage} 
          onConfirm={() => setErrorMessage(null)} 
        />
      )}

      {/* Confirmation popups */}
      {confirmUnassignSection && (
        <ConPop
          message={`Are you sure you want to unassign this TA from section ${confirmUnassignSection.sectionCode}?`}
          onConfirm={confirmSectionUnassignment}
          onCancel={() => setConfirmUnassignSection(null)}
        />
      )}

      {confirmDeleteTask && (
        <ConPop
          message={`Are you sure you want to unassign this TA from the ${confirmDeleteTask.type.toLowerCase()} task?`}
          onConfirm={confirmTaskDeletion}
          onCancel={() => setConfirmDeleteTask(null)}
        />
      )}
    </div>
  );
};

export default TaDetailedPage;
/* import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './DepartmentOfficeTaDetails.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import BackBut from '../../components/Buttons/BackBut';


interface SectionDto {
  sectionId: number;
  sectionCode: string;
  courseName: string;
}

interface TaProfile {
  id: number;
  name: string;
  surname: string;
  academicLevel: string;
  totalWorkload: number;
}

const TaDetailedPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const taId = id!;
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [profile, setProfile] = useState<TaProfile | null>(null);
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [sections, setSections] = useState<SectionDto[]>([]);
  const [newSectionCode, setNewSectionCode] = useState('');
  
  // State for confirmation dialogs
  const [confirmUnassignSection, setConfirmUnassignSection] = useState<SectionDto | null>(null);
  const [confirmDeleteTask, setConfirmDeleteTask] = useState<TaskDto | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
// Update the TaskDto interface to match the new API response format
interface TaskDto {
  taskId: number;
  taId: number;
  type: string;
  description?: string;
  sectionCode?: string;
  duration: {
    start: {
      day: number;
      month: number;
      year: number;
      hour: number;
      minute: number;
    };
    finish: {
      day: number;
      month: number;
      year: number;
      hour: number;
      minute: number;
    };
    ongoing: boolean;
  };
  status: string;
  workload: number;
}

// Update the useEffect hook to handle the new format
useEffect(() => {
  const fetchData = async () => {
    try {
      const [profileRes, tasksRes, sectionsRes] = await Promise.all([
        axios.get(`/api/ta/${taId}`),
        axios.get(`/api/ta/${taId}/tasks`),
        axios.get(`/api/ta/${taId}/sections`),
      ]);
      
      setProfile(profileRes.data);
      
      // Log the tasks response to verify the format
      console.log('Tasks API Response:', tasksRes.data);
      
      // Process tasks to ensure they have the expected format
      setTasks(tasksRes.data);
      setSections(sectionsRes.data);
    } catch (err) {
      console.error(err);
      setError('Failed to load TA details.');
    } finally {
      setLoading(false);
    }
  };
  fetchData();
}, [taId]);

// Update how we format and display dates in the tasks table
// Format the dates from the new date structure
const formatDate = (dateObj: { day: number; month: number; year: number; hour: number; minute: number; }) => {
  const date = `${dateObj.year}-${String(dateObj.month).padStart(2, '0')}-${String(dateObj.day).padStart(2, '0')}`;
  const time = `${String(dateObj.hour).padStart(2, '0')}:${String(dateObj.minute).padStart(2, '0')}`;
  return `${date} ${time}`;
};

  const handleDeleteTask = async (task: TaskDto) => {
  // Add debugging to verify the task object
  console.log('Setting task for deletion:', task);
  console.log('Task ID:', task.taskId);
  
  // Ensure we have a valid taskId before setting the confirmation
  if (task && task.taskId !== undefined) {
    setConfirmDeleteTask(task);
  } else {
    setErrorMessage('Cannot unassign task: missing task ID');
  }
};

  const confirmTaskDeletion = async () => {
  if (!confirmDeleteTask) return;
  
  try {
    // Let's add debugging to verify the task data
    console.log('Task to delete:', confirmDeleteTask);
    console.log('Task ID:', confirmDeleteTask.taskId);
    
    // Use the correct API endpoint for unassigning from a task
    if (confirmDeleteTask.sectionCode) {
      await axios.post(
        `/api/task/sectionCode/${confirmDeleteTask.sectionCode}/task/${confirmDeleteTask.taskId}/unassign`,
        { taId: taId } // Make sure to include the TA ID in the request body if needed
      );
    } else {
      // Fallback to the original endpoint if there's no section code
      await axios.delete(`/api/ta/${taId}/tasks/${confirmDeleteTask.taskId}`);
    }
    
    // Update the local state to remove the task
    setTasks(tasks.filter(t => t.taskId !== confirmDeleteTask.taskId));
    setSuccessMessage('Task has been unassigned successfully.');
    
    // Clear confirmation after short delay
    setTimeout(() => setSuccessMessage(null), 3000);
  } catch (err) {
    console.error('Error unassigning task:', err);
    // Log the exact error for debugging
    if (axios.isAxiosError(err) && err.response) {
      console.error('Error response:', err.response.data);
      console.error('Status code:', err.response.status);
    }
    setErrorMessage('Failed to unassign task. Please try again.');
  } finally {
    setConfirmDeleteTask(null);
  }
};

  const handleUnassignSection = (section: SectionDto) => {
    setConfirmUnassignSection(section);
  };

  const confirmSectionUnassignment = async () => {
    if (!confirmUnassignSection) return;
    
    try {
      // Use the correct API endpoint for unassigning from a section
      await axios.post(`/api/sections/${confirmUnassignSection.sectionCode}/tas/${taId}/unassign`);
      
      setSections(sections.filter(s => s.sectionId !== confirmUnassignSection.sectionId));
      setSuccessMessage('Section has been unassigned successfully.');
      
      // Clear confirmation after short delay
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Error unassigning section:', err);
      setErrorMessage('Failed to unassign section. Please try again.');
    } finally {
      setConfirmUnassignSection(null);
    }
  };

 const handleAssignSection = async () => {
  if (!newSectionCode) {
    setErrorMessage('Please enter a section code');
    return;
  }
  
  try {
    // Use the correct API endpoint for assigning a TA to a section
    await axios.post(`/api/sections/${newSectionCode}/tas/${taId}`);
    
    // After successful assignment, fetch the section details to add to the list
    // This is needed because the assignment endpoint might not return the section details
    try {
      const sectionResponse = await axios.get(`/api/sections/${newSectionCode}`);
      const newSection = {
        sectionId: sectionResponse.data.sectionId || Date.now(), // Fallback ID if none is provided
        sectionCode: newSectionCode,
        courseName: sectionResponse.data.courseName || `Course for ${newSectionCode}`
      };
      
      setSections([...sections, newSection]);
    } catch (sectionErr) {
      console.warn('Could not fetch section details:', sectionErr);
      // Add with minimal info if section details fetch fails
      const newSection = {
        sectionId: Date.now(),
        sectionCode: newSectionCode,
        courseName: 'Unknown Course'
      };
      setSections([...sections, newSection]);
    }
    
    setNewSectionCode('');
    setSuccessMessage('TA has been assigned to the section successfully.');
    
    // Clear message after short delay
    setTimeout(() => setSuccessMessage(null), 3000);
  } catch (err) {
    console.error('Error assigning TA to section:', err);
    
    // More detailed error logging
    if (axios.isAxiosError(err) && err.response) {
      console.error('Error response:', err.response.data);
      console.error('Status code:', err.response.status);
    }
    
    setErrorMessage('Failed to assign TA to section. Please check the section code and try again.');
  }
};

  if (loading) return <LoadingPage />;
  if (error) return (
    <div className={styles.errorContainer}>
      <p className={styles.errorMessage}>{error}</p>
      <button 
        className={styles.retryButton} 
        onClick={() => window.location.reload()}
      >
        Retry
      </button>
    </div>
  );

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut onClick={() => navigate(-1)} />
        <h1 className={styles.heading}>TA Details</h1>
      </div>
      
      <div className={styles.profileCard}>
        <div className={styles.profileHeader}>
          <h2 className={styles.profileName}>{profile?.name} {profile?.surname}</h2>
          <div className={styles.profileBadge}>{profile?.academicLevel}</div>
        </div>
        <div className={styles.profileDetails}>
          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>Total Workload:</span>
            <span className={styles.detailValue}>{profile?.totalWorkload}</span>
          </div>
          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>ID:</span>
            <span className={styles.detailValue}>{profile?.id}</span>
          </div>
        </div>
      </div>


      {successMessage && (
        <div className={styles.successBanner}>
          {successMessage}
        </div>
      )}

     
      <div className={styles.card}>
        <h2 className={styles.tableHeading}>Tasks</h2>
        <div className={styles.tableContainer}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Type</th>
                <th>Description</th>
                <th>Section</th>
                <th>Duration</th>
                <th>Status</th>
                <th>Workload</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {tasks.length > 0 ? tasks.map(task => (
                <tr key={task.taskId}>
                  <td>{task.type}</td>
                  <td>{task.description || '-'}</td>
                  <td>{task.sectionCode || '-'}</td>
                  <td>
  {task.duration && task.duration.start ? 
    formatDate(task.duration.start) : 'Unknown'} –{' '}
  {task.duration && task.duration.ongoing ? 
    'Ongoing' : 
    (task.duration && task.duration.finish ? formatDate(task.duration.finish) : 'Unknown')}
</td>
                  <td>
                    <span className={`${styles.status} ${styles[task.status.toLowerCase()]}`}>
                      {task.status}
                    </span>
                  </td>
                  <td>{task.workload}</td>
                  <td>
                    <button
                      className={styles.actionButton}
                      onClick={() => handleDeleteTask(task)}
                    >
                      Unassign
                    </button>
                  </td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={7} className={styles.emptyState}>
                    No tasks assigned to this TA
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      
      <div className={styles.card}>
        <h2 className={styles.tableHeading}>Enrolled Sections</h2>
        <div className={styles.tableContainer}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Section Code</th>
                <th>Course Name</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {sections.length > 0 ? sections.map(section => (
                <tr key={section.sectionId}>
                  <td>{section.sectionCode}</td>
                  <td>{section.courseName}</td>
                  <td>
                    <button
                      className={styles.actionButton}
                      onClick={() => handleUnassignSection(section)}
                    >
                      Unassign
                    </button>
                  </td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={3} className={styles.emptyState}>
                    No sections assigned to this TA
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className={styles.formGroup}>
          <input
            className={styles.input}
            type="text"
            placeholder="Enter Section Code"
            value={newSectionCode}
            onChange={e => setNewSectionCode(e.target.value)}
          />
          <button
            className={styles.assignButton}
            onClick={handleAssignSection}
            disabled={!newSectionCode}
          >
            Assign Section
          </button>
        </div>
      </div>

   
      {errorMessage && (
        <ErrPopUp 
          message={errorMessage} 
          onConfirm={() => setErrorMessage(null)} 
        />
      )}

      
      {confirmUnassignSection && (
        <ConPop
          message={`Are you sure you want to unassign this TA from section ${confirmUnassignSection.sectionCode}?`}
          onConfirm={confirmSectionUnassignment}
          onCancel={() => setConfirmUnassignSection(null)}
        />
      )}

      {confirmDeleteTask && (
        <ConPop
          message={`Are you sure you want to unassign this TA from the ${confirmDeleteTask.type.toLowerCase()} task?`}
          onConfirm={confirmTaskDeletion}
          onCancel={() => setConfirmDeleteTask(null)}
        />
      )}
    </div>
  );
};

export default TaDetailedPage; */

