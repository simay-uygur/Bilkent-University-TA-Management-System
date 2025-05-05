// src/pages/CourseDetails.tsx
import React, { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import styles from './DepartmentCourseDetails.module.css'

interface StudentDto {
  studentId: number
  studentName: string
  studentSurname: string
  webmail: string
  academicStatus: string
  department: string
  isActive: boolean
  isGraduated: boolean
}

interface SectionDto {
  sectionId: number
  sectionCode: string
  lessons: any[]
  instructor: {
    id: number
    name: string
    surname: string
    webmail: string
    departmentName: string
    sections: string[]
  }
  tas: { id: number; name: string; surname: string }[]
  students: StudentDto[]
}

interface CourseOfferingDto {
  id: number
  course: {
    courseId: number
    courseCode: string
    courseName: string
    courseAcademicStatus: string
    department: string
    prereqs: string[]
  }
  semester: {
    id: number
    term: string   // "SPRING" or "FALL"
    year: number
  }
  sections: SectionDto[]
}

const CourseDetails: React.FC = () => {
  const { courseCode } = useParams<{ courseCode: string }>()
  const [offerings, setOfferings] = useState<CourseOfferingDto[]>([])
  const [selected, setSelected]   = useState<CourseOfferingDto | null>(null)
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState<string | null>(null)

  useEffect(() => {
    if (!courseCode) {
      setError('No course code provided')
      setLoading(false)
      return
    }
    fetch(`/api/v1/offerings/courseCode/${courseCode}`)
      .then(res => {
        if (res.status != 302) throw new Error(`No offerings for “${courseCode}”`)
        return res.json()
      })
      .then((data: CourseOfferingDto[]) => {
        // sort by year desc, then term FALL > SPRING
        const termOrder: Record<string, number> = { FALL: 1, SPRING: 0 }
        const sorted = data.sort((a, b) => {
          if (b.semester.year !== a.semester.year) {
            return b.semester.year - a.semester.year
          }
          return (termOrder[b.semester.term] || 0) - (termOrder[a.semester.term] || 0)
        })
        setOfferings(sorted)
        setSelected(sorted[0])
      })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [courseCode])

  if (loading)   return <div className={styles.pageWrapper}>Loading…</div>
  if (error)     return <div className={styles.pageWrapper}>Error: {error}</div>
  if (!selected) return <div className={styles.pageWrapper}>No data</div>

  const { course, semester, sections } = selected

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>
        {course.courseCode} — {course.courseName}
      </h1>
      <p>
        <strong>Status:</strong> {course.courseAcademicStatus} |{' '}
        <strong>Dept:</strong> {course.department}
      </p>

      {/* Semester selector */}
      <label htmlFor="semSelect">Semester:&nbsp;</label>
      <select
        id="semSelect"
        value={selected.id}
        onChange={e => {
          const id = Number(e.target.value)
          const off = offerings.find(o => o.id === id) || null
          setSelected(off)
        }}
      >
        {offerings.map(o => (
          <option key={o.id} value={o.id}>
            {o.semester.term} {o.semester.year}
          </option>
        ))}
      </select>

      {course.prereqs.length > 0 && (
        <p><strong>Prerequisites:</strong> {course.prereqs.join(', ')}</p>
      )}

      {sections.length === 0 ? (
        <p>No sections available for this semester.</p>
      ) : (
        sections.map(sec => (
          <div key={sec.sectionId} className={styles.sectionCard}>
            <h2>Section {sec.sectionCode}</h2>
            <p>
              <strong>Instructor:</strong> {sec.instructor.name}{' '}
              {sec.instructor.surname}{' '}
              (<a href={`mailto:${sec.instructor.webmail}`}>{sec.instructor.webmail}</a>)
            </p>

            <div>
              <strong>TAs:</strong>{' '}
              {sec.tas.length > 0
                ? <ul>
                    {sec.tas.map(ta => (
                      <li key={ta.id}>{ta.name} {ta.surname}</li>
                    ))}
                  </ul>
                : <span>None</span>
              }
            </div>

            <div>
              <strong>Students:</strong>{' '}
              {sec.students.length > 0
                ? <ul>
                    {sec.students.map(s => (
                      <li key={s.studentId}>
                        {s.studentName} {s.studentSurname} ({s.academicStatus})
                      </li>
                    ))}
                  </ul>
                : <span>None</span>
              }
            </div>
          </div>
        ))
      )}

      <Link to="/dept-office" className={styles.backLink}>
        ← Back to Department Overview
      </Link>
    </div>
  )
}

export default CourseDetails


/* // src/pages/CourseDetails.tsx
import React, { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import styles from './CourseDetails.module.css'

interface StudentDto {
  studentId: number
  studentName: string
  studentSurname: string
  webmail: string
  academicStatus: string
  department: string
  isActive: boolean
  isGraduated: boolean
}

interface SectionDto {
  sectionId: number
  sectionCode: string
  lessons: any[]
  instructor: {
    id: number
    name: string
    surname: string
    webmail: string
    departmentName: string
    sections: string[]
  }
  tas: { id: number; name: string; surname: string }[]
  students: StudentDto[]
}

interface CourseOfferingDto {
  id: number
  course: {
    courseId: number
    courseCode: string
    courseName: string
    courseAcademicStatus: string
    department: string
    prereqs: string[]
  }
  semester: {
    id: number
    term: string
    year: number
  }
  sections: SectionDto[]
}

const CourseDetails: React.FC = () => {
    const { courseCode } = useParams<{ courseCode: string }>()
  const [offering, setOffering] = useState<CourseOfferingDto | null>(null)
  const [loading, setLoading]   = useState(true)
  const [error, setError]       = useState<string | null>(null)


useEffect(() => {
  if (!courseCode) {
    setLoading(false)
    setError('No courseCode in URL')
    return
  }
  fetch(`/api/v1/offerings/courseCode/${courseCode}`)
    .then(r => {
      if (!r.ok) throw new Error(`Course ${courseCode} not found`)
      return r.json()
    })
    .then(setOffering)
    .catch(err => setError(err.message))
    .finally(() => setLoading(false))
}, [courseCode])

  if (loading)   return <div className={styles.pageWrapper}>Loading…</div>
  if (error)     return <div className={styles.pageWrapper}>Error: {error}</div>
  if (!offering) return <div className={styles.pageWrapper}>No data</div>

  const { course, semester, sections } = offering

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>
        {course.courseCode} — {course.courseName}
      </h1>
      <p>
        Status: {course.courseAcademicStatus} | Dept: {course.department} | Term: {semester.term} {semester.year}
      </p>
      {course.prereqs.length > 0 && (
        <p>Prerequisites: {course.prereqs.join(', ')}</p>
      )}

      {sections.length === 0 ? (
        <p>No sections available.</p>
      ) : sections.map(sec => (
        <div key={sec.sectionId} className={styles.sectionCard}>
          <h2>Section {sec.sectionCode}</h2>
          <p>
            Instructor: {sec.instructor.name} {sec.instructor.surname} (
            <a href={`mailto:${sec.instructor.webmail}`}>{sec.instructor.webmail}</a>)
          </p>
          <strong>TAs:</strong>
          {sec.tas.length > 0
            ? <ul>{sec.tas.map(ta => <li key={ta.id}>{ta.name} {ta.surname}</li>)}</ul>
            : <span> None</span>
          }
          <strong>Students:</strong>
          {sec.students.length > 0
            ? <ul>{sec.students.map(s => (
                <li key={s.studentId}>
                  {s.studentName} {s.studentSurname} ({s.academicStatus})
                </li>
              ))}</ul>
            : <span> None</span>
          }
        </div>
      ))}

      <Link to="/dept-office" className={styles.backLink}>
        ← Back to Department
      </Link>
    </div>
  )
}

export default CourseDetails
 */