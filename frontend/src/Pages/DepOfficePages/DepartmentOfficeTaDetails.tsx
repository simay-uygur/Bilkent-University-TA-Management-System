import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './DepartmentOfficeTaDetails.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import BackBut from '../../components/Buttons/BackBut';

interface TaskDto {
  taskId: number;
  type: string;
  description?: string;
  sectionCode?: string;
  duration: { start: string; finish: string; ongoing?: boolean };
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

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [profileRes, tasksRes, sectionsRes] = await Promise.all([
          axios.get(`/api/ta/${taId}`),
          axios.get(`/api/ta/${taId}/tasks`),
          axios.get(`/api/ta/${taId}/sections`),
        ]);
        setProfile(profileRes.data);
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
                    {new Date(task.duration.start).toLocaleDateString()} –{' '}
                    {task.duration.ongoing ? 'Ongoing' : new Date(task.duration.finish).toLocaleDateString()}
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



/* // src/pages/TaDetailedPage.tsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './DepartmentOfficeTaDetails.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';

interface TaskDto {
  taskId: number;
  type: string;
  description?: string;
  duration: { start: string; finish: string; ongoing?: boolean };
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
  const [error, setError]     = useState<string | null>(null);
  const [profile, setProfile] = useState<TaProfile | null>(null);
  const [tasks, setTasks]     = useState<TaskDto[]>([]);
  const [sections, setSections] = useState<SectionDto[]>([]);
  const [newSectionCode, setNewSectionCode] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [profileRes, tasksRes, sectionsRes] = await Promise.all([
          axios.get(`/api/ta/${taId}`),
          axios.get(`/api/ta/${taId}/tasks`),
          axios.get(`/api/ta/${taId}/sections`),
        ]);
        setProfile(profileRes.data);
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

  const handleDeleteTask = async (taskId: number) => {
    try {
      await axios.delete(`/api/ta/${taId}/tasks/${taskId}`);
      setTasks(tasks.filter(t => t.taskId !== taskId));
    } catch {
      alert('Error deleting task');
    }
  };

  const handleUnassignSection = async (sectionId: number) => {
    try {
      await axios.delete(`/api/ta/${taId}/sections/${sectionId}`);
      setSections(sections.filter(s => s.sectionId !== sectionId));
    } catch {
      alert('Error unassigning section');
    }
  };

  const handleAssignSection = async () => {
    if (!newSectionCode) return;
    try {
      const res = await axios.post(`/api/ta/${taId}/sections`, { sectionCode: newSectionCode });
      setSections([...sections, res.data]);
      setNewSectionCode('');
    } catch {
      alert('Error assigning section');
    }
  };

  if (loading) return <LoadingPage/>;
  if (error)   return <div className={styles.error}>{error}</div>;

  return (
    <div className={styles.pageWrapper}>
      <button className={styles.backButton} onClick={() => navigate(-1)}>← Back</button>
      <h1 className={styles.heading}>{profile?.name} {profile?.surname}</h1>
      <p className={styles.subheading}>
        Level: {profile?.academicLevel} | Workload: {profile?.totalWorkload}
      </p>

      
      <div className={styles.card}>
        <h2 className={styles.tableHeading}>Tasks</h2>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Type</th><th>Description</th><th>Duration</th><th>Status</th><th>Workload</th><th>Action</th>
            </tr>
          </thead>
          <tbody>
            {tasks.map(t => (
              <tr key={t.taskId}>
                <td>{t.type}</td>
                <td>{t.description || '-'}</td>
                <td>
                  {new Date(t.duration.start).toLocaleString()} –{' '}
                  {new Date(t.duration.finish).toLocaleString()}
                </td>
                <td>{t.status}</td>
                <td>{t.workload}</td>
                <td>
                  <button
                    className={styles.actionButton}
                    onClick={() => handleDeleteTask(t.taskId)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
            {tasks.length === 0 && <tr><td colSpan={6}>No tasks found</td></tr>}
          </tbody>
        </table>
      </div>

     
      <div className={styles.card}>
        <h2 className={styles.tableHeading}>Enrolled Sections</h2>
        <table className={styles.table}>
          <thead>
            <tr><th>Section Code</th><th>Course Name</th><th>Action</th></tr>
          </thead>
          <tbody>
            {sections.map(s => (
              <tr key={s.sectionId}>
                <td>{s.sectionCode}</td>
                <td>{s.courseName}</td>
                <td>
                  <button
                    className={styles.actionButton}
                    onClick={() => handleUnassignSection(s.sectionId)}
                  >
                    Unassign
                  </button>
                </td>
              </tr>
            ))}
            {sections.length === 0 && <tr><td colSpan={3}>No sections assigned</td></tr>}
          </tbody>
        </table>

        <div className={styles.formGroup}>
          <input
            className={styles.input}
            type="text"
            placeholder="Section Code"
            value={newSectionCode}
            onChange={e => setNewSectionCode(e.target.value)}
          />
          <button
            className={styles.button}
            onClick={handleAssignSection}
          >
            Assign Section
          </button>
        </div>
      </div>
    </div>
  );
};

export default TaDetailedPage;
 */