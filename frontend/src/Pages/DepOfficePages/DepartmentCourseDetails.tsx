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

interface LessonDto {
  duration: {
    start: { day: number; month: number; year: number; hour: number; minute: number }
    finish: { day: number; month: number; year: number; hour: number; minute: number }
  }
  day : string
  classroomId: string | null
  examCapacity: number | null
  lessonType: string
  sectionId: string
}

interface SectionDto {
  sectionId: number
  sectionCode: string
  lessons: LessonDto[] | null
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
    term: 'SPRING' | 'FALL'
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
  const [openSections, setOpenSections] = useState<Record<number, boolean>>({})

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
        // sort by year desc, then FALL > SPRING
        const termOrder = { FALL: 1, SPRING: 0 }
        const sorted = data.sort((a, b) => {
          if (b.semester.year !== a.semester.year) {
            return b.semester.year - a.semester.year
          }
          return termOrder[b.semester.term] - termOrder[a.semester.term]
        })
        setOfferings(sorted)
        setSelected(sorted[0])
      })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [courseCode])

  const toggleSection = (id: number) => {
    setOpenSections(prev => ({ ...prev, [id]: !prev[id] }))
  }

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
        <strong>Status:</strong> {course.courseAcademicStatus} &nbsp;|&nbsp;
        <strong>Dept:</strong> {course.department}
      </p>

      {/* Semester selector */}
      <div style={{ margin: '12px 0' }}>
        <label htmlFor="semSelect">Semester:&nbsp;</label>
        <select
          id="semSelect"
          value={selected.id}
          onChange={e => {
            const off = offerings.find(o => o.id === Number(e.target.value)) || offerings[0]
            setSelected(off)
            setOpenSections({}) // collapse all
          }}
        >
          {offerings.map(o => (
            <option key={o.id} value={o.id}>
              {o.semester.term} {o.semester.year}
            </option>
          ))}
        </select>
      </div>

      {course.prereqs.length > 0 && (
        <p><strong>Prerequisites:</strong> {course.prereqs.join(', ')}</p>
      )}

      {sections.length === 0
        ? <p>No sections available for this semester.</p>
        : sections.map(sec => {
            const isOpen = openSections[sec.sectionId] === true
            return (
              <div key={sec.sectionId} className={styles.sectionCard}>
                <div
                  className={styles.sectionHeader}
                  onClick={() => toggleSection(sec.sectionId)}
                >
                  <span>Section {sec.sectionCode}</span>
                  <span className={styles.toggleHint}>
                    {isOpen ? '– collapse' : '+ expand'}
                  </span>
                </div>

                {isOpen && (
                  <div className={styles.sectionContent}>
                    <p>
                      <strong>Instructor:</strong>{' '}
                      {sec.instructor.name} {sec.instructor.surname}{' '}
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
                   
<div>
  <strong>Lessons:</strong>{' '}
  {sec.lessons && sec.lessons.length > 0
    ? <ul className={styles.lessonList}>
        {sec.lessons.map((l, i) => (
          <li key={i} className={styles.lessonItem}>
            {l.lessonType} •{' '}
            {/* Replace the date formatting with the day field */}
            <strong>{l.day}</strong> {' '}
            {`${String(l.duration.start.hour).padStart(2,'0')}:${String(l.duration.start.minute).padStart(2,'0')}–`}
            {`${String(l.duration.finish.hour).padStart(2,'0')}:${String(l.duration.finish.minute).padStart(2,'0')}`} •{' '}
            Room: {l.classroomId ?? 'TBA'} • Cap: {l.examCapacity ?? '–'}
          </li>
        ))}
      </ul>
    : <span>No lessons scheduled.</span>
  }
</div>
                    {/* <div>
                      <strong>Lessons:</strong>{' '}
                      {sec.lessons && sec.lessons.length > 0
                        ? <ul className={styles.lessonList}>
                            {sec.lessons.map((l, i) => (
                              <li key={i} className={styles.lessonItem}>
                                {l.lessonType} •{' '}
                                {`${l.duration.start.day}/${l.duration.start.month}/${l.duration.start.year} `}
                                {`${String(l.duration.start.hour).padStart(2,'0')}:${String(l.duration.start.minute).padStart(2,'0')}–`}
                                {`${String(l.duration.finish.hour).padStart(2,'0')}:${String(l.duration.finish.minute).padStart(2,'0')}`} •{' '}
                                Room: {l.classroomId ?? 'TBA'} • Cap: {l.examCapacity ?? '–'}
                              </li>
                            ))}
                          </ul>
                        : <span>No lessons scheduled.</span>
                      }
                    </div> */}
                  </div>
                )}
              </div>
            )
          })
      }

      <Link to="/department-office" className={styles.backLink}>
        ← Back to Department Overview
      </Link>
    </div>
  )
}

export default CourseDetails



/* // src/pages/CourseDetails.tsx
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

 */