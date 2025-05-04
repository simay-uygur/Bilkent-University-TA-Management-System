/* import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { fetchCourseTAs, CourseTA } from '../api';
import styles from './CourseTAList.module.css';

const CourseTAList: React.FC = () => {
  const { courseId } = useParams<{ courseId: string }>();
  const [tas, setTAs] = useState<CourseTA[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (courseId) {
      fetchCourseTAs(parseInt(courseId, 10))
        .then(r => setTAs(r.data))
        .finally(() => setLoading(false));
    }
  }, [courseId]);

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>TAs for Course {courseId}</h1>
      {loading ? (
        <p>Loading…</p>
      ) : (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Name</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {tas.map(ta => (
              <tr key={ta.id}>
                <td>{ta.name}</td>
                <td>
                  <button className={styles.requestBtn}>
                    Request TA
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default CourseTAList;
 */
// src/components/CourseTAList.tsx
// src/components/CourseTAList.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import styles from './CourseTAList.module.css';

export interface CourseTA {
  id: string;
  name: string;
}

// Mock TA data keyed by courseId
const mockTAsByCourse: Record<string, CourseTA[]> = {
  '1': [
    { id: 'ta1', name: 'Ali Veli' },
    { id: 'ta2', name: 'Ayşe Yılmaz' },
    { id: 'ta3', name: 'Mehmet Demir' },
  ],
  '2': [
    { id: 'ta4', name: 'Fatma Şahin' },
    { id: 'ta5', name: 'Emre Kaya' },
  ],
  '3': [
    { id: 'ta6', name: 'Zeynep Aydın' },
    { id: 'ta7', name: 'Can Yılmaz' },
    { id: 'ta8', name: 'Ece Çelik' },
  ],
  '4': [
    { id: 'ta9', name: 'Murat Aksoy' },
    { id: 'ta10', name: 'Seda Özkan' },
  ],
};

const CourseTAList: React.FC = () => {
  const { courseId } = useParams<{ courseId: string }>();
  const navigate = useNavigate();
  const [tas, setTAs] = useState<CourseTA[]>([]);

  useEffect(() => {
    if (courseId && mockTAsByCourse[courseId]) {
      setTAs(mockTAsByCourse[courseId]);
    } else {
      setTAs([]);
    }
  }, [courseId]);

  // Navigate to a form to request a NEW TA for this course
  const handleRequestNewTA = () => {
    navigate(`/instructor/courses/${courseId}/request-ta`);
  };

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>TAs for Course {courseId}</h1>

      
   <button
     className={styles.newRequestBtn}
     onClick={handleRequestNewTA}
   >
     Request New TA
   </button>

      {tas.length === 0 ? (
        <p>No TAs found for this course.</p>
      ) : (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>TA Name</th>
            </tr>
          </thead>
          <tbody>
            {tas.map(ta => (
              <tr key={ta.id}>
                <td>{ta.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default CourseTAList;