import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import SearchSelect from '../Benim/SearchSelect';
import styles from './MultiSearch.module.css';
// Remove the API imports and use direct fetch calls

// Define types for our search entities
interface Course {
  courseId: number;
  courseCode: string;
  courseName: string;
}

interface Instructor {
  id: number;
  name: string;
  surname: string;
}

interface TA {
  id: number;
  name: string;
  surname: string;
}

type SearchType = 'course' | 'instructor' | 'ta';

export default function MultiSearch() {
  const navigate = useNavigate();
  const [searchType, setSearchType] = useState<SearchType>('course');
  const [courses, setCourses] = useState<Course[]>([]);
  const [instructors, setInstructors] = useState<Instructor[]>([]);
  const [tas, setTAs] = useState<TA[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    // Load data based on current search type
    setLoading(true);
    
    if (searchType === 'course') {
      fetch('/api/course/department/CS')
        .then(res => res.json())
        .then((data: Course[]) => setCourses(data))
        .catch(error => console.error('Error fetching courses:', error))
        .finally(() => setLoading(false));
    } 
    else if (searchType === 'instructor') {
      fetch('/api/instructors/department/CS')
        .then(res => res.json())
        .then((data: Instructor[]) => setInstructors(data))
        .catch(error => console.error('Error fetching instructors:', error))
        .finally(() => setLoading(false));
    } 
    else {
      fetch('/api/ta/department/CS')
        .then(res => res.json())
        .then((data: TA[]) => setTAs(data))
        .catch(error => console.error('Error fetching TAs:', error))
        .finally(() => setLoading(false));
    }
  }, [searchType]);

  // Rest of the component remains the same
  const handleCourseSelect = (course: Course) => {
    navigate(`/course/${course.courseId}`);
  };

  const handleInstructorSelect = (instructor: Instructor) => {
    navigate(`/instructor/${instructor.id}`);
  };

  const handleTASelect = (ta: TA) => {
    navigate(`/ta/${ta.id}`);
  };

  // Return JSX remains unchanged
  return (
    <div className={styles.multiSearch}>
      {/* Existing JSX */}
      <div className={styles.tabs}>
        <button 
          className={`${styles.tab} ${searchType === 'course' ? styles.active : ''}`}
          onClick={() => setSearchType('course')}
        >
          Courses
        </button>
        <button 
          className={`${styles.tab} ${searchType === 'instructor' ? styles.active : ''}`}
          onClick={() => setSearchType('instructor')}
        >
          Instructors
        </button>
        <button 
          className={`${styles.tab} ${searchType === 'ta' ? styles.active : ''}`}
          onClick={() => setSearchType('ta')}
        >
          TAs
        </button>
      </div>

      {loading ? (
        <div className={styles.loading}>Loading...</div>
      ) : (
        <>
          {searchType === 'course' && (
            <SearchSelect
              options={courses}
              renderOption={(course) => (
                <div className={styles.courseOption}>
                  <span className={styles.courseCode}>{course.courseCode}</span>
                  <span className={styles.courseName}>{course.courseName}</span>
                </div>
              )}
              filterOption={(course) => `${course.courseCode} ${course.courseName}`}
              placeholder="Search courses..."
              onSelect={handleCourseSelect}
            />
          )}

          {searchType === 'instructor' && (
            <SearchSelect
              options={instructors}
              renderOption={(instructor) => (
                <div className={styles.personOption}>
                  {instructor.name} {instructor.surname}
                </div>
              )}
              filterOption={(instructor) => `${instructor.name} ${instructor.surname}`}
              placeholder="Search instructors..."
              onSelect={handleInstructorSelect}
            />
          )}

          {searchType === 'ta' && (
            <SearchSelect
              options={tas}
              renderOption={(ta) => (
                <div className={styles.personOption}>
                  {ta.name} {ta.surname}
                </div>
              )}
              filterOption={(ta) => `${ta.name} ${ta.surname}`}
              placeholder="Search TAs..."
              onSelect={handleTASelect}
            />
          )}
        </>
      )}
    </div>
  );
}