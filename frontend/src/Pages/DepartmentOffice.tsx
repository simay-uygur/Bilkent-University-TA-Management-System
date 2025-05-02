// src/pages/DepartmentOffice.tsx
import React, { useState, useEffect } from 'react';
import styles from './DepartmentOffice.module.css';

interface InstructorDto {
  id: number;
  name: string;
  surname: string;
  academicLevel: string;
  totalWorkload: number;
  isActive: boolean;
  isGraduated: boolean;
  department: string;
  courses: string[];
  lessons: string[];
}

interface CourseDto {
  courseId: number;
  courseCode: string;
  courseName: string;
  courseAcademicStatus: string;
  department: string;
}

const DepartmentOffice: React.FC = () => {
  const [instructors, setInstructors] = useState<InstructorDto[]>([]);
  const [courses, setCourses] = useState<CourseDto[]>([]);

  useEffect(() => {
    // Hard-code “CS” until you wire up real department logic
    fetch('/api/instructors/department/CS')
      .then((res) => res.json())
      .then((data: InstructorDto[]) => setInstructors(data))
      .catch(console.error);

    fetch('/api/course/department/CS')
      .then((res) => res.json())
      .then((data: CourseDto[]) => setCourses(data))
      .catch(console.error);
  }, []);

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.content}>
        <h1 className={styles.heading}>Department Overview</h1>

        <section className={styles.section}>
          <h2>Instructors in CS</h2>
          {instructors.length > 0 ? (
            <ul className={styles.list}>
              {instructors.map((i) => (
                <li key={i.id}>
                  {i.name} {i.surname} ({i.academicLevel})
                </li>
              ))}
            </ul>
          ) : (
            <p className={styles.noData}>No instructors found.</p>
          )}
        </section>

        <section className={styles.section}>
          <h2>Courses in CS</h2>
          {courses.length > 0 ? (
            <ul className={styles.list}>
              {courses.map((c) => (
                <li key={c.courseId}>
                  {c.courseCode} — {c.courseName}
                </li>
              ))}
            </ul>
          ) : (
            <p className={styles.noData}>No courses found.</p>
          )}
        </section>
      </div>
    </div>
  );
};

export default DepartmentOffice;



/* import React, { useState } from 'react';

import styles from './DepartmentOffice.module.css';

interface LeaveRequest {
  id: number;
  taName: string;
  course: string;
  proctoringDate: string;
  requestedTime: string;
  excuse: string;
  message: string;
  attachmentUrl?: string;
}

const initialRequests: LeaveRequest[] = [
  {
    id: 1,
    taName: 'Alice Smith',
    course: 'CS-101',
    proctoringDate: 'April 10, 2025',
    requestedTime: '08:00 – 10:00',
    excuse: 'Medical Appointment',
    message: 'A lightning struck to me.',
    attachmentUrl: '/attachments/attachment1.pdf',
  },
  {
    id: 2,
    taName: 'John Doe',
    course: 'CS-224',
    proctoringDate: 'April 11, 2025',
    requestedTime: '13:00 – 15:00',
    excuse: 'Family Emergency',
    message: 'I need to attend a family meeting due to an emergency.',
    // no attachment
  },
];

const DepartmentOffice: React.FC = () => {
  const [requests, setRequests] = useState<LeaveRequest[]>(initialRequests);

  const handleApprove = (id: number) => {
    // TODO: call API to approve, then remove from list
    setRequests(reqs => reqs.filter(r => r.id !== id));
  };

  const handleReject = (id: number) => {
    // TODO: call API to reject, then remove from list
    setRequests(reqs => reqs.filter(r => r.id !== id));
  };

  return (
    <div className={styles.pageWrapper}>
      

      <div className={styles.content}>
        <h1 className={styles.heading}>Pending Leave Requests</h1>

        <div className={styles.card}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>TA Name</th>
                <th>Course</th>
                <th>Proctoring Date</th>
                <th>Requested Time</th>
                <th>Excuse</th>
                <th>Message</th>
                <th>Attachment</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {requests.map(req => (
                <tr key={req.id}>
                  <td>{req.taName}</td>
                  <td>{req.course}</td>
                  <td>{req.proctoringDate}</td>
                  <td>{req.requestedTime}</td>
                  <td>{req.excuse}</td>
                  <td>{req.message}</td>
                  <td>
                    {req.attachmentUrl
                      ? <a href={req.attachmentUrl} target="_blank" rel="noopener noreferrer">View</a>
                      : 'No File'}
                  </td>
                  <td className={styles.actions}>
                    <button
                      className={styles.approve}
                      onClick={() => handleApprove(req.id)}
                    >
                      Approve
                    </button>
                    <button
                      className={styles.reject}
                      onClick={() => handleReject(req.id)}
                    >
                      Reject
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {requests.length === 0 && (
            <p className={styles.noRequests}>No pending requests.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default DepartmentOffice;
 */
