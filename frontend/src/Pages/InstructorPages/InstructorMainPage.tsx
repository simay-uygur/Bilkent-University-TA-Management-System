import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

import CourseInfoPanel from './CourseInfoPanel';
import styles from './InstructorMainPage.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';

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
  //courseName: string;
  lessons: any[];
  instructor: Instructor;
  tas: any[];
  students: any[];
}

// Interface for course details API response
interface CourseDetails {
  courseId: number;
  courseCode: string;
  courseName: string;
  courseAcademicStatus: string;
  department: string;
  prereqs: string[];
}

// Utility function to extract course code from section code
const extractCourseCode = (sectionCode: string): string => {
  if (!sectionCode) return '';
  
  const parts = sectionCode.split('-');
  if (parts.length < 2) return sectionCode;
  
  return `${parts[0]}-${parts[1]}`; // Works for both "CS-319" and "EEE-222"
};

// Utility function to extract section number from section code
const extractSectionNumber = (sectionCode: string): string => {
  if (!sectionCode) return '1';
  
  const parts = sectionCode.split('-');
  if (parts.length < 3) return '1';
  
  return parts[2];
};

const InsMainPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [instructor, setInstructor] = useState<Instructor | null>(null);
  const [sections, setSections] = useState<Section[]>([]);
  const [infoCourse, setInfoCourse] = useState<Section | null>(null);
  const [courseDetails, setCourseDetails] = useState<Record<string, CourseDetails>>({});
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
        try {
          const instructorResponse = await axios.get(`/api/instructors/${instructorId}`);
          const instructorData = instructorResponse.data;
          setInstructor(instructorData);

          // Check if instructor has sections
          if (!instructorData.sections || instructorData.sections.length === 0) {
            console.log('Instructor has no sections');
            setSections([]);
            setLoading(false);
            return;
          }

          // Fetch details for each section
          try {
            const sectionPromises = instructorData.sections.map((sectionCode: string) => 
              axios.get(`/api/sections/sectionCode/${sectionCode}`)
                .catch(err => {
                  console.warn(`Failed to fetch section ${sectionCode}:`, err);
                  return null; // Return null for failed sections
                })
            );
            
            const sectionResponses = await Promise.all(sectionPromises);
            const sectionData = sectionResponses
              .filter(response => response !== null)
              .map(response => response!.data);
            
            setSections(sectionData);
            
            // Collect unique course codes
            const uniqueCourses = new Set<string>();
            sectionData.forEach(section => {
              const courseCode = extractCourseCode(section.sectionCode);
              if (courseCode) {
                uniqueCourses.add(courseCode);
              }
            });
            
            // Fetch course details (optional - won't block rendering)
            fetchCourseDetails(Array.from(uniqueCourses));
            
          } catch (sectionErr) {
            console.error('Error fetching sections:', sectionErr);
            // We'll continue with just the instructor data
          }
        } catch (instructorErr) {
          console.error('Error fetching instructor:', instructorErr);
          setError('Failed to load instructor data.');
        }
        
        setLoading(false);
      } catch (err) {
        console.error('Error in main fetch function:', err);
        setError('Failed to load courses. Please try again.');
        setLoading(false);
      }
    };

    // Separate function to fetch course details
   // Separate function to fetch course details
const fetchCourseDetails = async (courseCodes: string[]) => {
  try {
    // Configure axios to follow redirects
    const axiosConfig = {
      maxRedirects: 5, // Allow up to 5 redirects
      validateStatus: (status: number) => {
        // Consider 2xx and 3xx responses as successful
        return status >= 200 && status < 400;
      }
    };

    const courseDetailsPromises = courseCodes.map(courseCode => 
      axios.get(`/api/course/${courseCode}`, axiosConfig)
        .then(response => {
          // Handle both direct responses and redirected responses
          if (response.status >= 300) {
            console.log(`Redirect detected for ${courseCode}, status: ${response.status}`);
            // For redirects, you might need to make a follow-up request
            // or handle the response differently depending on your API
          }
          return response;
        })
        .catch(err => {
          // This will now only catch actual errors (4xx, 5xx), not 302 redirects
          console.warn(`Failed to fetch course details for ${courseCode}:`, err);
          return null;
        })
    );
    
    const courseDetailsResponses = await Promise.all(courseDetailsPromises);
    const courseDetailsMap: Record<string, CourseDetails> = {};
    
    courseDetailsResponses.forEach(response => {
      if (response && response.data) {
        const course = response.data;
        console.log("Course details fetched successfully:", course);
        courseDetailsMap[course.courseCode] = course;
      }
    });
    
    console.log("Course details map:", courseDetailsMap);
    setCourseDetails(courseDetailsMap);
  } catch (err) {
    console.error('Error in fetchCourseDetails:', err);
    // Still don't set error state as this is optional data
  }
};

    fetchInstructorData();
  }, []);

  // Handle showing course info
  const handleShowInfo = (section: Section) => {
    // Get the course name from courseDetails if available
    const courseCode = extractCourseCode(section.sectionCode);
    const courseDetail = courseDetails[courseCode];
    
    // Create an enhanced section with the course name
    const enhancedSection = {
      ...section,
      name: courseDetail?.courseName || courseCode // Use the real course name or fall back to course code
    };
    
    setInfoCourse(enhancedSection);
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
              const courseCode = extractCourseCode(section.sectionCode);
              const sectionNumber = extractSectionNumber(section.sectionCode);
              
              // Get course name from details if available
              const courseName = courseDetails[courseCode]?.courseName || courseCode;
              
              return (
                <div key={section.sectionId} className={styles.courseCard}>
                  <div className={styles.courseInfo}>
                    <span className={styles.courseCode}>
                      {courseCode}/{sectionNumber}
                    </span>
                    <span className={styles.courseName}>{courseName}</span>
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
                      onClick={() => navigate(`/instructor/workload/${section.sectionCode}`)}
                    >
                      Manage Course Works
                    </button>
                    <button 
                      className={styles.actionButton} 
                      onClick={() => navigate(`/instructor/assign-course/${section.sectionCode}`)}
                    >
                      Course TA
                    </button>
                    <button
                      className={`${styles.actionButton} ${styles.info}`}
                      onClick={() => handleShowInfo(section)}
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
/* import React, { useState, useEffect } from 'react';
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

const InstructorMainPage: React.FC = () => {
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
    return <LoadingPage />;
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

<<<<<<< Updated upstream
export default InsMainPage; */
=======
export default InstructorMainPage;
>>>>>>> Stashed changes
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
