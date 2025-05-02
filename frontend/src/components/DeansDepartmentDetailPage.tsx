import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import NavBarDeans from '../components/NavBarDeans'
import styles from './DeansDepartmentDetailPage.module.css'

interface InstructorDto {
  id: number
  name: string
  surname: string
  academicLevel: string
}

interface CourseDto {
  courseId: number
  courseCode: string
  courseName: string
}

interface TaDto {
  id: number
  name: string
  surname: string
}

const DepartmentDetailPage: React.FC = () => {
  const { dept } = useParams<{ dept: string }>()
  const navigate = useNavigate()

  const [instructors, setInstructors] = useState<InstructorDto[]>([])
  const [courses, setCourses]       = useState<CourseDto[]>([])
  const [tas, setTAs]              = useState<TaDto[]>([])

  useEffect(() => {
    if (!dept) return

    Promise.all([
      fetch(`/api/instructors/department/${dept}`).then(r => r.json()),
      fetch(`/api/course/department/${dept}`).then(r => r.json()),
      fetch(`/api/ta/department/${dept}`).then(r => r.json()),
    ])
    .then(([ins, cou, t]) => {
      setInstructors(Array.isArray(ins) ? ins : [])
      setCourses    (Array.isArray(cou) ? cou : [])
      setTAs        (Array.isArray(t)   ? t   : [])
    })
    .catch(console.error)
  }, [dept])

  return (
    <>
    

      <div className={styles.pageWrapper}>
        <button className={styles.backBtn} onClick={() => navigate(-1)}>
          ← Back
        </button>
        <h1 className={styles.heading}>Department: {dept}</h1>

        <div className={styles.grid}>
          <div className={styles.card}>
            <h2>Instructors</h2>
            {instructors.length > 0 ? (
              <ul>
                {instructors.map(i => (
                  <li key={i.id}>
                    {i.name} {i.surname} — {i.academicLevel}
                  </li>
                ))}
              </ul>
            ) : (
              <p>No instructors found.</p>
            )}
          </div>

          <div className={styles.card}>
            <h2>Courses</h2>
            {courses.length > 0 ? (
              <ul>
                {courses.map(c => (
                  <li key={c.courseId}>
                    {c.courseCode} — {c.courseName}
                  </li>
                ))}
              </ul>
            ) : (
              <p>No courses found.</p>
            )}
          </div>

          <div className={styles.card}>
            <h2>Teaching Assistants</h2>
            {tas.length > 0 ? (
              <ul>
                {tas.map(t => (
                  <li key={t.id}>
                    {t.name} {t.surname}
                  </li>
                ))}
              </ul>
            ) : (
              <p>No TAs found.</p>
            )}
          </div>
        </div>
      </div>
    </>
  )
}

export default DepartmentDetailPage
