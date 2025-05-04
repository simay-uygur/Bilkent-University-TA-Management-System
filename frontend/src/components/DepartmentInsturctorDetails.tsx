// src/pages/InstructorDetails.tsx
import React, { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import styles from './DepartmentInstructorsDetails.module.css'

interface InstructorDto {
  id: number
  name: string
  surname: string
  webmail: string
  departmentName: string
  sections: string[]
}

const InstructorDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const [instructor, setInstructor] = useState<InstructorDto | null>(null)
  const [loading, setLoading]       = useState(true)
  const [error, setError]           = useState<string | null>(null)

  useEffect(() => {
    if (!id) {
      setError('No instructor ID provided')
      setLoading(false)
      return
    }

    fetch(`/api/instructors/${id}`)
      .then(res => {
        if (!res.ok) throw new Error(`Instructor ${id} not found`)
        return res.json()
      })
      .then((data: InstructorDto) => setInstructor(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  if (loading)   return <div className={styles.pageWrapper}>Loading…</div>
  if (error)     return <div className={styles.pageWrapper}>Error: {error}</div>
  if (!instructor) return <div className={styles.pageWrapper}>No data</div>

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>
        {instructor.name} {instructor.surname}
      </h1>
      <p>
        <strong>ID:</strong> {instructor.id}
      </p>
      <p>
        <strong>Email:</strong>{' '}
        <a href={`mailto:${instructor.webmail}`}>{instructor.webmail}</a>
      </p>
      <p>
        <strong>Department:</strong> {instructor.departmentName}
      </p>

      <div className={styles.sectionCard}>
        <strong>Sections:</strong>{' '}
        {instructor.sections.length > 0 ? (
          <ul>
            {instructor.sections.map(sec => (
              <li key={sec}>{sec}</li>
            ))}
          </ul>
        ) : (
          <span>None</span>
        )}
      </div>

      <Link to="/dept-office" className={styles.backLink}>
        ← Back to Department Overview
      </Link>
    </div>
  )
}

export default InstructorDetails
