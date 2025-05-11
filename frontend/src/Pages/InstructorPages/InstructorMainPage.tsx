import axios from 'axios';
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import CourseInfoPanel from './CourseInfoPanel';
import styles from './InstructorMainPage.module.css';
import LoadingPage from '../CommonPages/LoadingPage';

interface ExamDto {
  examId: number;
  type: string;
  duration: {
    start:  { year: number; month: number; day: number; hour: number; minute: number };
    finish: { year: number; month: number; day: number; hour: number; minute: number };
  };
}

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

type CourseDetails = {
  courseId: number;
  courseCode: string;
  courseName: string;
  courseAcademicStatus: string;
  department: string;
  prereqs: string[];
};

// Utility functions
  const extractCourseCode = (sectionCode: string): string => {
    const parts = sectionCode.split('-');
    return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : sectionCode;
  };

const extractSectionNumber = (sectionCode: string): string => {
  const parts = sectionCode.split('-');
  return parts.length >= 3 ? parts[2] : '1';
};

const InsMainPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sections, setSections] = useState<Section[]>([]);
  const [infoCourse, setInfoCourse] = useState<Section | null>(null);
  const [courseDetails, setCourseDetails] = useState<Record<string, CourseDetails>>({});
  const [availableExams, setAvailableExams] = useState<ExamDto[]>([]);
  const [showExamSelector, setShowExamSelector] = useState(false);
  const [selectedCourseCode, setSelectedCourseCode] = useState<string>('');
  //const instructorId = localStorage.getItem('userId');

  const navigate = useNavigate();

  useEffect(() => {
    const fetchInstructorData = async () => {
      try {
        setLoading(true);
        const instructorId = localStorage.getItem('userId');
        if (!instructorId) {
          setError('Not logged in or missing user ID');
          return;
        }

        const instructorRes = await axios.get(`/api/instructors/${instructorId}`);
        const instructorData = instructorRes.data as Instructor;
        if (!instructorData.sections?.length) {
          setSections([]);
          return;
        }

        const sectionPromises = instructorData.sections.map(code =>
            axios.get<Section>(`/api/sections/sectionCode/${code}`)
                .then(res => res.data)
                .catch(err => {
                  console.warn(`Failed to fetch section ${code}:`, err);
                  return null;
                })
        );
        const results = await Promise.all(sectionPromises);
        const loaded = results.filter(Boolean) as Section[];
        setSections(loaded);

        // Fetch course details in background
        const uniqueCodes = Array.from(
            new Set(loaded.map(sec => extractCourseCode(sec.sectionCode)))
        );
        fetchCourseDetails(uniqueCodes);
      } catch (err) {
        console.error('Error loading data:', err);
        setError('Failed to load courses.');
      } finally {
        setLoading(false);
      }
    };

    const fetchCourseDetails = async (codes: string[]) => {
      try {
        const cfg = { maxRedirects: 5, validateStatus: (s: number) => s >= 200 && s < 400 };
        const promises = codes.map(code =>
            axios.get<CourseDetails>(`/api/course/${code}`, cfg)
                .then(res => res.data)
                .catch(err => {
                  console.warn(`Failed to fetch details for ${code}:`, err);
                  return null;
                })
        );
        const data = await Promise.all(promises);
        const map: Record<string, CourseDetails> = {};
        data.forEach(c => c && (map[c.courseCode] = c));
        setCourseDetails(map);
      } catch (err) {
        console.error('Error fetching course details:', err);
      }
    };

    fetchInstructorData();
  }, []);

  // Group by term
  const sectionsByTerm = sections.reduce((acc, sec) => {
    const parts = sec.sectionCode.split('-');
    const year = parts[3] || '';
    const term = parts[4] || '';
    const key = `${year}-${term}`;
    if (!acc[key]) acc[key] = [];
    acc[key].push(sec);
    return acc;
  }, {} as Record<string, Section[]>);

  // Sort terms, but prioritize currentSemester from localStorage
  const termOrder = ['SPRING', 'SUMMER', 'FALL'];
  const allKeys = Object.keys(sectionsByTerm).sort((a, b) => {
    const [yA, tA] = a.split('-');
    const [yB, tB] = b.split('-');
    if (yA !== yB) return parseInt(yA) - parseInt(yB);
    return termOrder.indexOf(tA) - termOrder.indexOf(tB);
  });

  const currentSemester = localStorage.getItem('currentSemester'); // e.g. "2025-SPRING"
  const sortedKeys = currentSemester && allKeys.includes(currentSemester)
      ? [currentSemester, ...allKeys.filter(k => k !== currentSemester)]
      : allKeys;

  // Function to check if a section is from a past semester
  const isOutdatedSection = (sectionCode: string): boolean => {
    // Extract year and term from section code
    const parts = sectionCode.split('-');
    if (parts.length < 5) return false;

    const year = parseInt(parts[3] || '0');
    const term = parts[4] || '';

    // Get current date for comparison
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth(); // 0-11

    // If year is in the past, section is outdated
    if (year < currentYear) return true;

    // If same year, check term
    if (year === currentYear) {
      if (term === 'SPRING' && currentMonth > 4) return true;  // Spring ends in May
      if (term === 'SUMMER' && currentMonth > 7) return true;  // Summer ends in August
      if (term === 'FALL' && currentMonth > 11) return true;   // Fall ends in December
    }

    return false;
  };

  const handleShowInfo = (sec: Section) => {
    const code = extractCourseCode(sec.sectionCode);
    const detail = courseDetails[code];
    setInfoCourse({ ...sec, name: detail?.courseName || code } as any);
  };

  if (loading) return <LoadingPage />;
  if (error) return <div className={styles.error}>{error}</div>;

  return (
      <div className={styles.pageWrapper}>
        <main className={styles.content}>
          <h1 className={styles.heading}>My Courses</h1>
          {sortedKeys.map(key => {
            const [yr, tm] = key.split('-');
            return (
                <section key={key}>
                  <h2>{`${yr} ${tm}`}</h2>
                  <div className={styles.courseList}>
                    {sectionsByTerm[key].map(sec => {
                      const code = extractCourseCode(sec.sectionCode);
                      const num = extractSectionNumber(sec.sectionCode);
                      const name = courseDetails[code]?.courseName || code;
                      const outdated = isOutdatedSection(sec.sectionCode);

                      return (
                          <div key={sec.sectionId} className={`${styles.courseCard} ${outdated ? styles.outdatedCard : ''}`}>
                            <div className={styles.courseInfo}>
                              <span className={styles.courseCode}>{`${code}/${num}`}</span>
                              <span className={styles.courseName}>{name}</span>
                              {outdated && <span className={styles.outdatedBadge}>Archive</span>}
                            </div>
                            <div className={styles.actions}>
                              <div className={styles.actions}>
                                <button
                                    className={styles.actionButton}
                                    onClick={() => !outdated && navigate(`/instructor/exam-proctor-request/${code}`)}
                                    disabled={outdated}
                                >
                                  {outdated ? 'View Exams'        : 'Exam Proctoring'}
                                </button>

                                <button
                                    className={styles.actionButton}
                                    disabled={outdated}
                                    onClick={async () => {
                                      if (outdated) return;
                                      //const instructorId = localStorage.getItem('userId');
                                      const code = extractCourseCode(sec.sectionCode);
                                      // 🔗 GET /api/instructors/{instructorId}/courses/{courseCode}/exams
                                      const resp = await axios.get<ExamDto[]>(
                                          `/api/instructors/${code}/exams`
                                      );
                                      setSelectedCourseCode(code);
                                      setAvailableExams(resp.data);
                                      setShowExamSelector(true);
                                    }}
                                >
                                  Exam Printing
                                </button>

                                <button
                                    className={styles.actionButton}
                                    onClick={() => !outdated && navigate(`/instructor/workload/${sec.sectionCode}`)}
                                    disabled={outdated}
                                >
                                  {outdated ? 'View Tasks'        : 'Manage Course Works'}
                                </button>

                                <button
                                    className={styles.actionButton}
                                    onClick={() => !outdated && navigate(`/instructor/assign-course/${sec.sectionCode}`)}
                                    disabled={outdated}
                                >
                                  {outdated ? 'View TAs'          : 'Course TA'}
                                </button>

                                <button
                                    className={`${styles.actionButton} ${styles.info}`}
                                    onClick={() => handleShowInfo(sec)}
                                    /* always enabled so you can still inspect the course */
                                >
                                  Course Info
                                </button>
                              </div>

                              {/*  <button
                          className={styles.actionButton}
                          onClick={() => navigate(`/instructor/exam-printing/${code}`)}
                          disabled={outdated}
                        >
                          {outdated ? 'View Exams' : 'Exam Proctoring'}
                        </button>

                        <button
                          className={styles.actionButton}
                          disabled={outdated}
                        >
                          {outdated ? 'View Printings' : 'Exam Printing'}
                        </button>

                        <button
                          className={styles.actionButton}
                          onClick={() => navigate(`/instructor/workload/${sec.sectionCode}`)}
                        >
                          {outdated ? 'View Tasks' : 'Manage Course Works'}
                        </button>

                        <button
                          className={styles.actionButton}
                          onClick={() => navigate(`/instructor/assign-course/${sec.sectionCode}`)}
                          disabled={outdated}
                        >
                          {outdated ? 'View TAs' : 'Course TA'}
                        </button>

                        <button
                          className={`${styles.actionButton} ${styles.info}`}
                          onClick={() => handleShowInfo(sec)}
                        >
                          Course Info
                        </button> */}
                            </div>
                          </div>
                      );
                    })}
                  </div>
                </section>
            );
          })}
        </main>



        {showExamSelector && (
            <div className={styles.modalOverlay}>
            <div className={styles.examSelectorModal}>
              <h3>Select an Exam to Export PDF</h3>
              <table className={styles.examTable}>
                <thead>
                <tr>
                  <th>ID</th>
                  <th>Type</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Action</th>
                </tr>
                </thead>
                <tbody>
                {availableExams.map(ex => {
                  const { examId, type, duration: { start, finish } } = ex;
                  const fmt = (n: number) => n.toString().padStart(2,'0');
                  const date = `${fmt(start.day)}.${fmt(start.month)}.${start.year}`;
                  const time = `${fmt(start.hour)}:${fmt(start.minute)}–${fmt(finish.hour)}:${fmt(finish.minute)}`;
                  return (
                      <tr key={examId}>
                        <td>{examId}</td>
                        <td>{type}</td>
                        <td>{date}</td>
                        <td>{time}</td>
                        <td>
                          <button
                              onClick={() => {
                                const instructorId = localStorage.getItem('userId');
                                window.open(
                                    `/api/instructors/${instructorId}/courses/${selectedCourseCode}/exams/${examId}/export/pdf`,
                                    "_blank"
                                );
                              }}
                          >
                            Export PDF
                          </button>
                        </td>
                      </tr>
                  )
                })}
                </tbody>
              </table>
                <button
                    className={styles.cancelButton}
                    onClick={() => setShowExamSelector(false)}
                >
                  Cancel
                </button>
            </div>
            </div>
        )}

        {infoCourse && <CourseInfoPanel course={infoCourse} onClose={() => setInfoCourse(null)} />}
      </div>
  );
};

export default InsMainPage;
