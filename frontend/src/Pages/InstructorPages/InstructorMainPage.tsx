import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

import CourseInfoPanel from './CourseInfoPanel';
import styles from './InstructorMainPage.module.css';
import axios from 'axios';

// Updated interfaces to match API response
export interface Instructor {
  id: number;
  name: string;
  surname: string;
  webmail: string;
  departmentName: string;
  sections: string[];
}

export interface Section {
  sectionId: number;
  sectionCode: string;
  lessons: any[];
  instructor: Instructor;
  tas: any[];
  students: any[];
}

export interface Course {
  id: number;
  code: string;
  name: string;
  sec: number;
  studentNum: number;
  instructor: Instructor;
}

const InsMainPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [instructor, setInstructor] = useState<Instructor | null>(null);
  const [sections, setSections] = useState<Section[]>([]);
  const [infoCourse, setInfoCourse] = useState<Course | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchInstructorData = async () => {
      try {
        setLoading(true);
        // Get instructor ID from localStorage
        const instructorId = localStorage.getItem('userId');
        
        if (!instructorId) {
          setError('Not logged in or missing user ID');
          setLoading(false);
          return;
        }

        // Fetch instructor data
        const instructorResponse = await axios.get(`/api/instructors/${instructorId}`);
        const instructorData = instructorResponse.data;
        setInstructor(instructorData);

        // Fetch details for each section
        const sectionPromises = instructorData.sections.map((sectionCode: string) => 
          axios.get(`/api/sections/sectionCode/${sectionCode}`)
        );
        
        const sectionResponses = await Promise.all(sectionPromises);
        const sectionData = sectionResponses.map(response => response.data);
        setSections(sectionData);
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Failed to load courses. Please try again.');
        setLoading(false);
      }
    };

    fetchInstructorData();
  }, []);

  // Convert section data to course format for compatibility with CourseInfoPanel
  const sectionToCourse = (section: Section): Course => {
    // Extract course code and section number from sectionCode (e.g., "CS-319-2-2025-SPRING")
    const parts = section.sectionCode.split('-');
    const courseCode = `${parts[0]}-${parts[1]}`;
    const sectionNumber = parseInt(parts[2]);
    
    return {
      id: section.sectionId,
      code: courseCode,
      name: courseCode, // You might want to add a proper name if available
      sec: sectionNumber,
      studentNum: section.students.length,
      instructor: section.instructor
    };
  };

  if (loading) {
    return <div className={styles.loading}>Loading courses...</div>;
  }

  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <main className={styles.content}>
        <h1 className={styles.heading}>My Courses</h1>
        {sections.length === 0 ? (
          <p>You don't have any assigned courses.</p>
        ) : (
          <div className={styles.courseList}>
            {sections.map(section => {
              // Extract course code for display
              const parts = section.sectionCode.split('-');
              const courseCode = `${parts[0]}-${parts[1]}`;
              const sectionNumber = parts[2];
              
              return (
                <div key={section.sectionId} className={styles.courseCard}>
                  <div className={styles.courseInfo}>
                    <span className={styles.courseCode}>
                      {courseCode}/{sectionNumber}
                    </span>
                    <span className={styles.courseName}>{courseCode}</span>
                  </div>
                  <div className={styles.actions}>
                    <button
                      className={styles.actionButton}
                      onClick={() => navigate(`/instructor/exam-printing/${courseCode}`)}
                    >
                      Exam Proctoring
                    </button>
                    <button className={styles.actionButton}>
                      Exam Printing
                    </button>
                    <button 
                      className={styles.actionButton} 
                      onClick={() => navigate(`/instructor/workload/${courseCode}/${sectionNumber}`)}
                    >
                      Manage Course Works
                    </button>
                    <button 
                      className={styles.actionButton} 
                      onClick={() => navigate(`/instructor/assign-course/${courseCode}/${sectionNumber}`)}
                    >
                      Course TA
                    </button>
                    <button
                      className={`${styles.actionButton} ${styles.info}`}
                      onClick={() => setInfoCourse(sectionToCourse(section))}
                    >
                      Course Info
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      {infoCourse && (
        <CourseInfoPanel
          course={infoCourse}
          onClose={() => setInfoCourse(null)}
        />
      )}
    </div>
  );
};

export default InsMainPage;
/* import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import CourseInfoPanel from './CourseInfoPanel';
import styles from './InstructorMainPage.module.css';

export interface Instructor {
  name: string;
  surname: string;
}

export interface Course {
  id: number;
  code: string;
  name: string;
  sec: number;
  studentNum: number;
  instructor: Instructor;
}

const initialCourses: Course[] = [
  {
    id: 1,
    code: 'CS-101',
    name: 'Introduction to Programming',
    sec: 3,
    studentNum: 30,
    instructor: { name: 'John', surname: 'Doe' },
  },
  {
    id: 2,
    code: 'ENG-202',
    name: 'Technical Writing',
    sec: 3,
    studentNum: 30,
    instructor: { name: 'Jane', surname: 'Smith' },
  },
  {
    id: 3,
    code: 'MATH-150',
    name: 'Calculus I',
    sec: 3,
    studentNum: 30,
    instructor: { name: 'Alan', surname: 'Turing' },
  },
  {
    id: 4,
    code: 'PHY-110',
    name: 'General Physics',
    sec: 3,
    studentNum: 30,
    instructor: { name: 'Marie', surname: 'Curie' },
  },
];

const InstructorMainPage: React.FC = () => {
  const [courses] = useState<Course[]>(initialCourses);
  const [infoCourse, setInfoCourse] = useState<Course | null>(null);
  const navigate = useNavigate();

  return (
    <div className={styles.pageWrapper}>
      

      <main className={styles.content}>
        <h1 className={styles.heading}>My Courses</h1>
        <div className={styles.courseList}>
          {courses.map(course => (
            <div key={course.id} className={styles.courseCard}>
              <div className={styles.courseInfo}>
                <span className={styles.courseCode}>
                  {course.code}/{course.sec}
                </span>
                <span className={styles.courseName}>{course.name}</span>
              </div>
              <div className={styles.actions}>
                <button
                  className={styles.actionButton}
                  onClick={() => navigate(`/instructor/exam-proctor-request/${course.code}`)}
                >
                  Exam Proctoring
                </button>
                <button className={styles.actionButton} onClick={() => navigate(`/instructor/exam-printing/${course.code}`)}>
                  Exam Printing
                </button>
                <button className={styles.actionButton} onClick={() => navigate(`/instructor/workload/${course.code}/${course.sec}`)} >
                  Manage Course Works
                </button>
                <button className={styles.actionButton} onClick={() => navigate(`/instructor/assign-course/${course.code}/${course.sec}`)}>
                  Course TA
                </button>
                <button
                  className={`${styles.actionButton} ${styles.info}`}
                  onClick={() => setInfoCourse(course)}
                >
                  Course Info
                </button>
              </div>
            </div>
          ))}
        </div>
      </main>

      {infoCourse && (
        <CourseInfoPanel
          course={infoCourse}
          onClose={() => setInfoCourse(null)}
        />
      )}
    </div>
  );
};

export default InsMainPage;
 */
