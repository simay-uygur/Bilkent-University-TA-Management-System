import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './CourseTA.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';

interface TaDto {
  id: number;
  name: string;
  surname: string;
}
interface SectionTACount {
  sectionCode: string;
  assignedTACount: number;
}
interface PreferTasRequest {
  requestId: number;
  requestType: string;
  description: string;
  senderName: string;
  receiverName: string;
  sentTime: {
    day: number; month: number; year: number;
    hour: number; minute: number;
  };
  instructorId: number;
  courseCode: string;
  sectionId: number;
  sectionCode: string;
  taNeeded: number;
  amountOfAssignedTas: number;
  preferredTas: TaDto[];
  nonPreferredTas: TaDto[];
  rejected: boolean;
  approved: boolean;
  pending: boolean;
}

// Helper to extract section number
const extractSectionNumber = (code: string): string => code.split('-')[2] || '';

// Optional mapping from courseCode to human-readable course name
const courseNameMap: Record<string, string> = {
  'CS-101': 'Intro to CS',
  'MATH-201': 'Calculus II',
  'PHY-301': 'Physics III',
  // add more mappings as needed
};

const CourseTA: React.FC = () => {
  const navigate = useNavigate();
// In your CourseTA component, add this new state
const [sectionTACounts, setSectionTACounts] = useState<Record<string, number>>({});
const [loadingCounts, setLoadingCounts] = useState(false);

  // state for department requests
  const [requests, setRequests] = useState<PreferTasRequest[]>([]);
  const [loadingReq, setLoadingReq] = useState(false);
  const [reqError, setReqError] = useState<string | null>(null);

  // First effect - fetch requests only once on mount
useEffect(() => {
  const deptCode = localStorage.getItem('departmentCode') || '';
  setLoadingReq(true);
  axios
    .get<PreferTasRequest[]>(`/api/department/${deptCode}/preferTas`)
    .then(res => setRequests(res.data))
    .catch(err => {
      console.error(err);
      setReqError('Failed to load TA‐preference requests');
    })
    .finally(() => setLoadingReq(false));
}, []); 


useEffect(() => {
  if (requests.length === 0) return;
  
  setLoadingCounts(true);
  
  // Create a list of promises for each section code
  const promises = requests.map(request => 
    axios.get(`/api/ta/sectionCode/${request.sectionCode}`)
      .then(res => ({
        sectionCode: request.sectionCode,
        assignedTACount: Array.isArray(res.data) ? res.data.length : 0
      }))
      .catch(err => {
        console.error(`Failed to fetch TAs for ${request.sectionCode}:`, err);
        return { sectionCode: request.sectionCode, assignedTACount: 0 };
      })
  );
   // Wait for all requests to complete
  Promise.all(promises)
    .then(results => {
      // Convert array of results to a record object for easy lookup
      const countMap: Record<string, number> = {};
      results.forEach(item => {
        countMap[item.sectionCode] = item.assignedTACount;
      });
      
      setSectionTACounts(countMap);
    })
    .catch(err => {
      console.error('Error fetching TA counts:', err);
    })
    .finally(() => {
      setLoadingCounts(false);
    });
}, [requests]);
 /*  useEffect(() => {
    const deptCode = localStorage.getItem('departmentCode') || '';
    setLoadingReq(true);
    axios
      .get<PreferTasRequest[]>(`/api/department/${deptCode}/preferTas`)
      .then(res => setRequests(res.data))
      .catch(err => {
        console.error(err);
        setReqError('Failed to load TA‐preference requests');
      })
      .finally(() => setLoadingReq(false));
       if (requests.length === 0) return;
  
  setLoadingCounts(true);
  
  // Create a list of promises for each section code
  const promises = requests.map(request => 
    axios.get(`/api/ta/sectionCode/${request.sectionCode}`)
      .then(res => ({
        sectionCode: request.sectionCode,
        assignedTACount: Array.isArray(res.data) ? res.data.length : 0
      }))
      .catch(err => {
        console.error(`Failed to fetch TAs for ${request.sectionCode}:`, err);
        return { sectionCode: request.sectionCode, assignedTACount: 0 };
      })
  );
  // Wait for all requests to complete
  Promise.all(promises)
    .then(results => {
      // Convert array of results to a record object for easy lookup
      const countMap: Record<string, number> = {};
      results.forEach(item => {
        countMap[item.sectionCode] = item.assignedTACount;
      });
      
      setSectionTACounts(countMap);
    })
    .catch(err => {
      console.error('Error fetching TA counts:', err);
    })
    .finally(() => {
      setLoadingCounts(false);
    });
}, [requests]); */
const [confirmFinish, setConfirmFinish] = useState<PreferTasRequest | null>(null);

const handleFinishAssignment = (request: PreferTasRequest) => {
  // Show confirmation dialog
  setConfirmFinish(request);
};

// Add this to your JSX
{confirmFinish && (
  <ConPop
    message={`Mark TA assignment for ${confirmFinish.courseCode} Section ${extractSectionNumber(confirmFinish.sectionCode)} as complete?`}
    onConfirm={() => {
      // Here you can make an API call to mark the request as complete
      // For now, just close the dialog
      axios.put(`/api/request/${confirmFinish.requestId}/approve`)
        .then(() => {
          // Update the request in the local state
          setRequests(prev => 
            prev.map(r => r.requestId === confirmFinish.requestId ? {...r, approved: true, pending: false} : r)
          );
        })
        .catch(err => {
          console.error('Error approving request:', err);
          // Show error to user
        });
      setConfirmFinish(null);
    }}
    onCancel={() => setConfirmFinish(null)}
  />
)}
  if (loadingReq) {
    return <LoadingPage/>;
  }
  if (reqError) {
    return <div className={styles.error}>{reqError}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>Assign TAs to Course</h1>
      </div>

      <div className={styles.container}>
        <table className={styles.table}>
        <thead className={styles.headings}>
  <tr>
    <th>Course Name</th>
    <th>Section</th>
    <th>Course ID</th>
    <th>Needed TAs</th>
    <th>Preferred TAs</th>
    <th>Non-Preferred TAs</th>
    <th>Assigned TAs</th>
    <th>Status</th>
    <th>Actions</th>
  </tr>
</thead>
          <tbody>
  {requests.map(r => {
    const section = extractSectionNumber(r.sectionCode);
    const courseId = r.courseCode;
    const courseName = courseNameMap[courseId] || courseId;
    const preferredCount = r.preferredTas.length;
    const unprefferredCount = r.nonPreferredTas.length;
    const assignedCount = sectionTACounts[r.sectionCode] || 0;
    const leftCount = r.taNeeded - assignedCount;
    const completed = leftCount <= 0;
    const allAssigned = assignedCount >= r.taNeeded;

    return (
      <tr
        key={r.requestId}
        className={`${styles.rowBase} ${
          allAssigned ? styles.completedRow : styles.incompleteRow
        }`}
      >
        <td>{courseName}</td>
        <td>{section}</td>
        <td>{courseId}</td>
        <td>{r.taNeeded}</td>
        <td>
          {r.preferredTas.length > 0
            ? r.preferredTas
                .map(ta => `${ta.name} ${ta.surname}`)
                .join(', ')
            : 'None'}
        </td>
        <td>
          {r.nonPreferredTas.length > 0
            ? r.nonPreferredTas
                .map(ta => `${ta.name} ${ta.surname}`)
                .join(', ')
            : 'None'}
        </td>
        <td>
          {/* Add this new column */}
          {assignedCount} / {r.taNeeded} {loadingCounts && '...'}
        </td>
        <td>
          {r.pending
            ? 'Pending'
            : r.approved
            ? 'Approved'
            : 'Rejected'}
        </td>
        <td className={styles.actionsCell}>
          <button
            className={styles.assignBtn}
            onClick={() => navigate(`/department-office/assign-course/${r.requestId}`)}
            disabled={allAssigned} // Disable when enough TAs are assigned
          >
            {allAssigned ? 'TAs Complete' : 'Assign TA'}
          </button>
          <button
            className={styles.finishBtn}
            onClick={() => handleFinishAssignment(r)}
            disabled={!allAssigned} // Only enable when enough TAs are assigned
          >
            Finish Assignment
          </button>
        </td>
      </tr>
    );
  })}
</tbody>
        </table>
      </div>
       {confirmFinish && (
      <ConPop
        message={`Mark TA assignment for ${confirmFinish.courseCode} Section ${extractSectionNumber(confirmFinish.sectionCode)} as complete?`}
        onConfirm={() => {
          axios.put(`/api/request/${confirmFinish.requestId}/approve`)
            .then(() => {
              setRequests(prev => 
                prev.map(r => r.requestId === confirmFinish.requestId ? 
                  {...r, approved: true, pending: false} : r)
              );
            })
            .catch(err => {
              console.error('Error approving request:', err);
            });
          setConfirmFinish(null);
        }}
        onCancel={() => setConfirmFinish(null)}
      />
    )}
    </div>
  );
};

export default CourseTA;


/* import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './CourseTA.module.css';
import axios from 'axios';

interface TaDto {
  id: number;
  name: string;
  surname: string;
}

interface PreferTasRequest {
  requestId: number;
  requestType: string;
  description: string;
  senderName: string;
  receiverName: string;
  sentTime: {
    day: number; month: number; year: number;
    hour: number; minute: number;
  };
  instructorId: number;
  courseCode: string;
  sectionId: number;
  sectionCode: string;
  taNeeded: number;
  amountOfAssignedTas: number;
  preferredTas: TaDto[];
  nonPreferredTas: TaDto[];
  rejected: boolean;
  approved: boolean;
  pending: boolean;
}

const CourseTA: React.FC = () => {
  const navigate = useNavigate();

  // state for department requests
  const [requests, setRequests] = useState<PreferTasRequest[]>([]);
  const [loadingReq, setLoadingReq] = useState(false);
  const [reqError, setReqError] = useState<string | null>(null);
  const [detailReq, setDetailReq] = useState<PreferTasRequest | null>(null);

  useEffect(() => {
    const deptCode = localStorage.getItem('departmentCode') || '';
    setLoadingReq(true);
    axios
      .get<PreferTasRequest[]>(`/api/department/${deptCode}/preferTas`)
      .then(res => setRequests(res.data))
      .catch(err => {
        console.error(err);
        setReqError('Failed to load TA‐preference requests');
      })
      .finally(() => setLoadingReq(false));
  }, []);

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>TA Preference Requests</h1>
      </div>

      {loadingReq && <div>Loading requests…</div>}
      {reqError && <div className={styles.error}>{reqError}</div>}

      {!loadingReq && !reqError && (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Course</th>
              <th>Section</th>
              <th>Needed</th>
              <th>Preferred</th>
              <th>Non‐Pref.</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {requests.map(r => (
              <tr key={r.requestId}>
                <td>{r.requestId}</td>
                <td>{r.courseCode}</td>
                <td>{r.sectionCode}</td>
                <td>{r.taNeeded}</td>
                <td>{r.preferredTas.length}</td>
                <td>{r.nonPreferredTas.length}</td>
                <td>
                  {r.pending
                    ? 'Pending'
                    : r.approved
                    ? 'Approved'
                    : 'Rejected'}
                </td>
                <td>
                  <button
                    className={styles.detailsButton}
                    onClick={() => setDetailReq(r)}
                  >
                    View
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {detailReq && (
        <ConPop
          message={
            <div>
              <h3>Request #{detailReq.requestId}</h3>
              <p>{detailReq.description}</p>
              <strong>Preferred TAs:</strong>
              <ul>
                {detailReq.preferredTas.map(t =>
                  <li key={t.id}>{t.name} {t.surname}</li>
                )}
              </ul>
              <strong>Non‐Preferred TAs:</strong>
              <ul>
                {detailReq.nonPreferredTas.map(t =>
                  <li key={t.id}>{t.name} {t.surname}</li>
                )}
              </ul>
            </div>
          }
          onConfirm={() => setDetailReq(null)}
          onCancel={() => setDetailReq(null)}
        />
      )}
    </div>
  );
};

export default CourseTA; */
/* // src/pages/AssignTACourse/AssignTACourse.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './CourseTA.module.css';
import axios from 'axios';

interface Course {
  id: string;
  name: string;
  section: string;
  neededTAs: number;
  preferredCount: number;
  tasLeft: number;
}

const CourseTA: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  // Keep your initial mock courses
  const initialCourses: Course[] = [
    { id: 'CS-101', name: 'Intro to CS', section: '1', neededTAs: 3, preferredCount: 2, tasLeft: 1 },
    { id: 'MATH-201', name: 'Calculus II', section: '2', neededTAs: 2, preferredCount: 1, tasLeft: 2 },
    { id: 'PHY-301', name: 'Physics III', section: '1', neededTAs: 1, preferredCount: 0, tasLeft: 0 },
  ];

  // Add the hardcoded section codes
  const sectionCodes = [
    "CS-299-1-2025-SPRING",
    "CS-464-1-2025-FALL",
    "CS-299-2-2025-SPRING",
    "CS-464-2-2025-FALL",
    "CS-115-1-2025-SPRING",
    "CS-319-1-2025-SPRING",
    "CS-115-2-2025-SPRING"
  ];

  const [courses, setCourses] = useState<Course[]>(initialCourses);
  const [confirmId, setConfirmId] = useState<string | null>(null);
  const [confirmMsg, setConfirmMsg] = useState<string | null>(null);

  // Replace the API fetch with hardcoded section data
  useEffect(() => {
    // Create Course objects from section codes
    const sectionCourses: Course[] = sectionCodes.map(sectionCode => {
      // Parse section code to extract course info
      const parts = sectionCode.split("-");
      const courseId = `${parts[0]}-${parts[1]}`;
      const sectionNum = parts[2];
      const semester = parts[4];
      const year = parts[3];
      
      // Assign different needed TAs based on course level
      const courseNum = parseInt(parts[1]);
      const neededTAs = courseNum >= 400 ? 1 : courseNum >= 300 ? 2 : 3;
      
      return {
        id: sectionCode, // Use the full section code as the unique ID
        name: `${courseId} ${semester} ${year}`,
        section: sectionNum,
        neededTAs: neededTAs,
        preferredCount: Math.floor(Math.random() * neededTAs), // Random number of preferred TAs
        tasLeft: neededTAs // Initially, all TAs are left to be assigned
      };
    });
    
    // Add the section courses to the existing mock courses
    setCourses(prev => [...prev, ...sectionCourses]);
  }, []);

  // Modified to handle both mock data and real section data
  const handleAssign = (id: string, section: string) => {
    // Check if this is a full section code (real data) or just a course ID (mock data)
    if (id.split('-').length > 2) {
      // This is a full section code like "CS-319-2-2025-SPRING"
      navigate(`/department-office/assign-course/${id}`);
    } else {
      // This is a mock course ID like "CS-101"
      navigate(`/department-office/assign-course/${id}-${section}-2025-SPRING`);
    }
  };

  const handleFinish = (id: string) => {
    const c = courses.find(c => c.id === id);
    if (!c) return;
    setConfirmMsg(
      c.tasLeft > 0
        ? `Still ${c.tasLeft} TA${c.tasLeft > 1 ? 's' : ''} left. Are you sure to finish anyway?`
        : 'Mark this course assignment finished?'
    );
    setConfirmId(id);
  };

  const handleConfirm = () => {
    if (confirmId) setCourses(prev => prev.filter(c => c.id !== confirmId));
    setConfirmId(null);
    setConfirmMsg(null);
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>Assign TAs to Course</h1>
      </div>

      <div className={styles.container}>
        <table className={styles.table}>
          <thead className={styles.headings}>
            <tr>
              <th>Course Name</th>
              <th>Section</th>
              <th>Course ID</th>
              <th>Needed TAs</th>
              <th>Preferred TAs</th>
              <th>TAs Left</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {courses.map(course => (
              <tr
                key={course.id}
                className={`${styles.rowBase} ${
                  course.tasLeft === 0
                    ? styles.completedRow
                    : styles.incompleteRow
                }`}
              >
                <td>{course.name}</td>
                <td>{course.section}</td>
                <td>{course.id}</td>
                <td>{course.neededTAs}</td>
                <td>{course.preferredCount}</td>
                <td>{course.tasLeft}</td>
                <td className={styles.actionsCell}>
                  <button
                    className={styles.assignBtn}
                    onClick={() => handleAssign(course.id, course.section)}
                  >
                    Assign TA
                  </button>
                  <button
                    className={styles.assignBtn}
                    onClick={() => handleFinish(course.id)}
                  >
                    Finish Assignment
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {confirmMsg && (
        <ConPop
          message={confirmMsg}
          onConfirm={handleConfirm}
          onCancel={() => setConfirmMsg(null)}
        />
      )}
    </div>
  );
};

export default CourseTA; */

/* // src/pages/AssignTACourse/AssignTACourse.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignTACourse.module.css';

interface Course {
  id: string;
  name: string;
  section: string;
  neededTAs: number;
  preferredCount: number;
  tasLeft: number;
}

const AssignTACourse: React.FC = () => {
  const navigate = useNavigate();

  const initialCourses: Course[] = [
    { id: 'CS-101', name: 'Intro to CS', section: '1', neededTAs: 3, preferredCount: 2, tasLeft: 1 },
    { id: 'MATH-201', name: 'Calculus II', section: '2', neededTAs: 2, preferredCount: 1, tasLeft: 2 },
    { id: 'PHY-301', name: 'Physics III', section: '1', neededTAs: 1, preferredCount: 0, tasLeft: 0 },
  ];

  const [courses, setCourses] = useState<Course[]>(initialCourses);
  const [confirmId, setConfirmId] = useState<string | null>(null);
  const [confirmMsg, setConfirmMsg] = useState<string | null>(null);

  const handleAssign = (id: string, section: string) => navigate(`/department-office/assign-course/${id}/${section}`);

  const handleFinish = (id: string) => {
    const c = courses.find(c => c.id === id);
    if (!c) return;
    setConfirmMsg(
      c.tasLeft > 0
        ? `Still ${c.tasLeft} TA${c.tasLeft > 1 ? 's' : ''} left. Are you sure to finish anyway?`
        : 'Mark this course assignment finished?'
    );
    setConfirmId(id);
  };

  const handleConfirm = () => {
    if (confirmId) setCourses(prev => prev.filter(c => c.id !== confirmId));
    setConfirmId(null);
    setConfirmMsg(null);
  };

  return (
    <div className={styles.pageWrapper}>
      

      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>Assign TAs to Course</h1>
      </div>

      <div className={styles.container}>
        <table className={styles.table}>
          <thead className={styles.headings}>
            <tr>
              <th>Course Name</th>
              <th>Section</th>
              <th>Course ID</th>
              <th>Needed TAs</th>
              <th>Preferred TAs</th>
              <th>TAs Left</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {courses.map(course => (
              <tr
                key={course.id}
                className={`${styles.rowBase} ${
                  course.tasLeft === 0
                    ? styles.completedRow
                    : styles.incompleteRow
                }`}
              >
                <td>{course.name}</td>
                <td>{course.section}</td>
                <td>{course.id}</td>
                <td>{course.neededTAs}</td>
                <td>{course.preferredCount}</td>
                <td>{course.tasLeft}</td>
                <td className={styles.actionsCell}>
                  <button
                    className={styles.assignBtn}
                    onClick={() => handleAssign(course.id, course.section)}
                  >
                    Assign TA
                  </button>
                  <button
                    className={styles.assignBtn}
                    onClick={() => handleFinish(course.id)}
                  >
                    Finish Assignment
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {confirmMsg && (
        <ConPop
          message={confirmMsg}
          onConfirm={handleConfirm}
          onCancel={() => setConfirmMsg(null)}
        />
      )}
    </div>
  );
};

export default AssignTACourse;
 */