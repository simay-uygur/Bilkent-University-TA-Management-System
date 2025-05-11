import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios, { CancelTokenSource } from 'axios';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import LoadingPage from '../CommonPages/LoadingPage';
import styles from './CourseTA.module.css';

// DTOs
interface TaDto { id: number; name: string; surname: string; }
interface PreferTasRequest {
  requestId: number;
  courseCode: string;
  sectionCode: string;
  taNeeded: number;
  preferredTas: TaDto[];
  nonPreferredTas: TaDto[];
  pending: boolean;
  approved: boolean;
  rejected: boolean;
}
interface CourseInfo { courseName: string; }

// Helpers
const extractSectionNumber = (code: string): string => code.split('-')[2] || code;

const CourseTA: React.FC = () => {
  const navigate = useNavigate();
  const isMounted = useRef(true);

  // State
  const [requests, setRequests] = useState<PreferTasRequest[]>([]);
  const [courseNames, setCourseNames] = useState<Record<string, string>>({});
  const [sectionCounts, setSectionCounts] = useState<Record<string, number>>({});

  const [loadingReq, setLoadingReq] = useState(true);
  const [loadingNames, setLoadingNames] = useState(false);
  const [loadingCounts, setLoadingCounts] = useState(false);
  const [reqError, setReqError] = useState<string | null>(null);

  const [confirmFinish, setConfirmFinish] = useState<PreferTasRequest | null>(null);

  // Fetch TA-preference requests
  useEffect(() => {
    const cancelSrc: CancelTokenSource = axios.CancelToken.source();
    const deptCode = localStorage.getItem('departmentCode');
    if (!deptCode) {
      setReqError('No department code found');
      setLoadingReq(false);
      return;
    }
    axios.get<PreferTasRequest[]>(`/api/department/${deptCode}/preferTas`, { cancelToken: cancelSrc.token })
      .then(res => {
        if (!isMounted.current) return;
        setRequests(res.data);
      })
      .catch(err => {
        if (axios.isCancel(err)) return;
        console.error(err);
        if (isMounted.current) setReqError('Failed to load TA-preference requests');
      })
      .finally(() => {
        if (isMounted.current) setLoadingReq(false);
      });
    return () => { isMounted.current = false; cancelSrc.cancel(); };
  }, []);

  // Fetch course names
  useEffect(() => {
    if (requests.length === 0) return;
    const cancelSrc = axios.CancelToken.source();
    setLoadingNames(true);
    const codes = Array.from(new Set(requests.map(r => r.courseCode)));
    Promise.all(codes.map(code =>
      axios.get<CourseInfo>(`/api/course/${code}`, { cancelToken: cancelSrc.token })
        .then(r => ({ code, name: r.data.courseName }))
        .catch(() => ({ code, name: code }))
    ))
      .then(list => {
        if (!isMounted.current) return;
        const map: Record<string, string> = {};
        list.forEach(item => { map[item.code] = item.name; });
        setCourseNames(map);
      })
      .finally(() => { if (isMounted.current) setLoadingNames(false); });
    return () => { cancelSrc.cancel(); };
  }, [requests]);

  // Fetch assigned TA counts per section
  useEffect(() => {
    if (requests.length === 0) return;
    const cancelSrc = axios.CancelToken.source();
    setLoadingCounts(true);
    Promise.all(requests.map(req =>
      axios.get<TaDto[]>(`/api/ta/section/${req.sectionCode}`, { cancelToken: cancelSrc.token })
        .then(r => ({ section: req.sectionCode, count: r.data.length }))
        .catch(() => ({ section: req.sectionCode, count: 0 }))
    ))
      .then(list => {
        if (!isMounted.current) return;
        const map: Record<string, number> = {};
        list.forEach(item => { map[item.section] = item.count; });
        setSectionCounts(map);
      })
      .finally(() => { if (isMounted.current) setLoadingCounts(false); });
    return () => { cancelSrc.cancel(); };
  }, [requests]);

  const handleFinish = (req: PreferTasRequest) => setConfirmFinish(req);

  const confirmAction = () => {
    if (!confirmFinish) return;
    const { requestId } = confirmFinish;
    axios.put(`/api/request/${requestId}/approve`)
      .then(() => {
        setRequests(prev => prev.map(r =>
          r.requestId === requestId
            ? { ...r, approved: true, pending: false }
            : r
        ));
      })
      .catch(err => console.error('Approve failed', err));
    setConfirmFinish(null);
  };

  if (loadingReq) return <LoadingPage />;
  if (reqError) return <div className={styles.error}>{reqError}</div>;

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>Assign TAs to Course</h1>
      </div>

      <div className={styles.container}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Course Name</th>
              <th>Section</th>
              <th>Course ID</th>
              <th>Needed</th>
              <th>Preferred</th>
              <th>Non-Preferred</th>
              <th>Assigned</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {requests.map(r => {
              const assigned = sectionCounts[r.sectionCode] || 0;
              const left = r.taNeeded - assigned;
              const done = left <= 0;
              return (
                <tr key={r.requestId} className={done ? styles.completedRow : styles.incompleteRow}>
                  <td>{courseNames[r.courseCode] || r.courseCode}</td>
                  <td>{extractSectionNumber(r.sectionCode)}</td>
                  <td>{r.courseCode}</td>
                  <td>{r.taNeeded}</td>
                  <td>{r.preferredTas.map(t => `${t.name} ${t.surname}`).join(', ') || 'None'}</td>
                  <td>{r.nonPreferredTas.map(t => `${t.name} ${t.surname}`).join(', ') || 'None'}</td>
                  <td>{assigned}{loadingCounts && '...'}</td>
                  <td>{r.pending ? 'Pending' : r.approved ? 'Approved' : 'Rejected'}</td>
                  <td className={styles.actionsCell}>
                    <button
                      className={styles.assignBtn}
                      onClick={() => navigate(`/department-office/assign-course/${r.requestId}`)}
                      disabled={done}
                    >
                      {done ? 'TAs Complete' : 'Assign TA'}
                    </button>
                    <button
                      className={styles.finishBtn}
                      onClick={() => handleFinish(r)}
                      disabled={!done || loadingNames || loadingCounts}
                    >
                      Finish
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
          onConfirm={confirmAction}
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