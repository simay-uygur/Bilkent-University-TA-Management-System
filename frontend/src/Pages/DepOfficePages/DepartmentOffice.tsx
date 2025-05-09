import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './DepartmentOffice.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';

interface StaffDto {
  id: number;
  name: string;
  surname: string;
  isActive: boolean;
  departmentName: string;
}

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

interface TaDto {
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

const DepartmentOffice: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [staffData, setStaffData] = useState<StaffDto | null>(null);
  const [instructors, setInstructors] = useState<InstructorDto[]>([]);
  const [courses, setCourses] = useState<CourseDto[]>([]);
  const [tas, setTAs] = useState<TaDto[]>([]);


    useEffect(() => {
      // Get department staff data
      const fetchDepartmentData = () => {
        setLoading(true);
        
        // Get staff ID from localStorage
        const staffId = localStorage.getItem('userId');
        if (!staffId) {
          setError('Not logged in or missing user ID');
          setLoading(false);
          return;
        }
        
        // Get department staff data using fetch
        fetch(`/api/department-staff/${staffId}`)
          .then(res => res.json())
          .then(staff => {
            setStaffData(staff);
            localStorage.setItem('departmentCode', staff.departmentName);
            const departmentCode = staff.departmentName;
            
            // Load instructors
            fetch(`/api/instructors/department/${departmentCode}`)
              .then(res => res.json())
              .then((data: InstructorDto[]) => setInstructors(data))
              .catch(err => {
                console.log('Instructor data fetch error:', err);
                setInstructors([]);
              });
            
            // Load courses
            fetch(`/api/course/department/${departmentCode}`)
              .then(res => res.json())
              .then((data: CourseDto[]) => setCourses(data))
              .catch(err => {
                console.log('Course data fetch error:', err);
                setCourses([]);
              });
            
            // Load TAs
            fetch(`/api/ta/department/${departmentCode}`)
              .then(res => res.json())
              .then((data: TaDto[]) => setTAs(data))
              .catch(err => {
                console.log('TA data fetch error:', err);
                setTAs([]);
              });
            
            setLoading(false);
          })
          .catch(err => {
            console.error('Error fetching staff data:', err);
            setError('Failed to load department data. Please try again.');
            setLoading(false);
          });
      };
      
      fetchDepartmentData();
    }, []);
    

  if (loading) {
    return <LoadingPage/>;
  }

  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  const departmentName = staffData?.departmentName || 'Department';

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>Department Overview ({departmentName})</h1>
      <div className={styles.grid}>

        {/* Instructors */}
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>Instructors</h2>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Name</th>
                <th>Level</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {instructors.length > 0 ? (
                instructors.map(i => (
                  <tr key={i.id}>
                    <td>{i.name} {i.surname}</td>
                    <td>{i.academicLevel}</td>
                    <td>
                      <button
                        type="button"
                        className={styles.detailsButton}
                        onClick={() => navigate(`instructor/${i.id}`)}
                      >
                        Details
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3}>No instructors found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Courses */}
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>Courses</h2>
          <table className={styles.table}>
            <thead>
              <tr><th>Code</th><th>Name</th><th>Action</th></tr>
            </thead>
            <tbody>
              {courses.length > 0 ? (
                courses.map(c => (
                  <tr key={c.courseId}>
                    <td>{c.courseCode}</td>
                    <td>{c.courseName}</td>
                    <td>
                      <button 
                        type="button"
                        className={styles.detailsButton}
                        onClick={() => navigate(`course/${c.courseCode}`)}
                      >
                        Details
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3}>No courses found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* TAs */}
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>TAs</h2>
          <table className={styles.table}>
            <thead>
              <tr><th>Name</th><th>Workload</th></tr>
            </thead>
            <tbody>
              {tas.length > 0 ? (
                tas.map(t => (
                  <tr key={t.id}>
                    <td>{t.name} {t.surname}</td>
                    <td>{t.totalWorkload}</td>
                    <td>
                      <button
                        type="button"
                        className={styles.detailsButton}
                        onClick={() => navigate(`ta/${t.id}`)}
                      >
                        Details
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={2}>No TAs found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

      </div>
    </div>
  );
};

export default DepartmentOffice;
/* // src/pages/DepartmentOffice.tsx
import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import styles from './DepartmentOffice.module.css'

interface InstructorDto {
  id: number
  name: string
  surname: string
  academicLevel: string
  totalWorkload: number
  isActive: boolean
  isGraduated: boolean
  department: string
  courses: string[]
  lessons: string[]
}

interface CourseDto {
  courseId: number
  courseCode: string
  courseName: string
  courseAcademicStatus: string
  department: string
}

interface TaDto {
  id: number
  name: string
  surname: string
  academicLevel: string
  totalWorkload: number
  isActive: boolean
  isGraduated: boolean
  department: string
  courses: string[]
  lessons: string[]
}

const DepartmentOffice: React.FC = () => {
  const navigate = useNavigate()
  const [instructors, setInstructors] = useState<InstructorDto[]>([])
  const [courses, setCourses]         = useState<CourseDto[]>([])
  const [tas, setTAs]                 = useState<TaDto[]>([])

  useEffect(() => {
    fetch('/api/instructors/department/CS')
      .then(res => res.json())
      .then((data: InstructorDto[]) => setInstructors(data))
      .catch(console.error)

    fetch('/api/course/department/CS')
      .then(res => res.json())
      .then((data: CourseDto[]) => setCourses(data))
      .catch(console.error)

    fetch('/api/ta/department/CS')
      .then(res => res.json())
      .then((data: TaDto[]) => setTAs(data))
      .catch(console.error)
  }, [])

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>Department Overview (CS)</h1>
      <div className={styles.grid}>

        
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>Instructors</h2>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Name</th>
                <th>Level</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {instructors.map(i => (
                <tr key={i.id}>
                  <td>{i.name} {i.surname}</td>
                  <td>{i.academicLevel}</td>
                  <td>
                    <button
                      type="button"
                      onClick={() => navigate(`instructor/${i.id}`)}
                    >
                      Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        //{ Courses }
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>Courses</h2>
          <table className={styles.table}>
            <thead>
              <tr><th>Code</th><th>Name</th><th>Action</th></tr>
            </thead>
            <tbody>
              {courses.map(c => (
                <tr key={c.courseId}>
                  <td>{c.courseCode}</td>
                  <td>{c.courseName}</td>
                  <td>
                    <button
                      type="button"
                      onClick={() => navigate(`/department-office/${c.courseCode}`)}
                    >
                      Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        //
        // {TaS}
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>TAs</h2>
          <table className={styles.table}>
            <thead>
              <tr><th>Name</th><th>Workload</th></tr>
            </thead>
            <tbody>
              {tas.map(t => (
                <tr key={t.id}>
                  <td>{t.name} {t.surname}</td>
                  <td>{t.totalWorkload}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

      </div>
    </div>
  )
}

export default DepartmentOffice */

/*
// src/pages/DepartmentOffice.tsx
import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import styles from './DepartmentOffice.module.css'

interface InstructorDto {
  id: number
  name: string
  surname: string
  academicLevel: string
  totalWorkload: number
  isActive: boolean
  isGraduated: boolean
  department: string
  courses: string[]
  lessons: string[]
}

interface CourseDto {
  courseId: number
  courseCode: string
  courseName: string
  courseAcademicStatus: string
  department: string
}

interface TaDto {
  id: number
  name: string
  surname: string
  academicLevel: string
  totalWorkload: number
  isActive: boolean
  isGraduated: boolean
  department: string
  courses: string[]
  lessons: string[]
}

const DepartmentOffice: React.FC = () => {
  const navigate = useNavigate()
  const [instructors, setInstructors] = useState<InstructorDto[]>([])
  const [courses, setCourses]         = useState<CourseDto[]>([])
  const [tas, setTAs]                 = useState<TaDto[]>([])

  useEffect(() => {
    fetch('/api/instructors/department/CS')
      .then(res => res.json())
      .then((data: InstructorDto[]) => setInstructors(data))
      .catch(console.error)

    fetch('/api/course/department/CS')
      .then(res => res.json())
      .then((data: CourseDto[]) => setCourses(data))
      .catch(console.error)

    fetch('/api/ta/department/CS')
      .then(res => res.json())
      .then((data: TaDto[]) => setTAs(data))
      .catch(console.error)
  }, [])

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>Department Overview (CS)</h1>
      <div className={styles.grid}>

       
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>Instructors</h2>
          <table className={styles.table}>
            <thead>
              <tr><th>Name</th><th>Level</th></tr>
            </thead>
            <tbody>
              {instructors.map(i => (
                <tr key={i.id}>
                  <td>{i.name} {i.surname}</td>
                  <td>{i.academicLevel}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>Courses</h2>
          <table className={styles.table}>
            <thead>
              <tr><th>Code</th><th>Name</th><th>Action</th></tr>
            </thead>
            <tbody>
              {courses.map(c => (
                <tr key={c.courseId}>
                  <td>{c.courseCode}</td>
                  <td>{c.courseName}</td>
                  <td>
                    <button
                      type="button"
                      onClick={() => navigate(`course/${c.courseCode}`)}
                    >
                      Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        
        <div className={styles.card}>
          <h2 className={styles.tableHeading}>TAs</h2>
          <table className={styles.table}>
            <thead>
              <tr><th>Name</th><th>Workload</th></tr>
            </thead>
            <tbody>
              {tas.map(t => (
                <tr key={t.id}>
                  <td>{t.name} {t.surname}</td>
                  <td>{t.totalWorkload}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

      </div>
    </div>
  )
}

export default DepartmentOffice
*/