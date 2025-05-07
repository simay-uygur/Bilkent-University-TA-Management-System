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

        {/* Courses */}
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

        {/* TAs */}
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