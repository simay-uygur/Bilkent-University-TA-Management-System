// src/pages/TaDetailedPage.tsx
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

      {/* Tasks */}
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

      {/* Enrolled Sections */}
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
