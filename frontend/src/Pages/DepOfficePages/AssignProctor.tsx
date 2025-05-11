import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import AssignProctorRow from './AssignProctorRow';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import LoadingPage from '../CommonPages/LoadingPage';
import styles from './AssignProctor.module.css';
import BackBut from '../../components/Buttons/BackBut';

// Define types for the API responses
interface DateInfo {
  day: number;
  month: number;
  year: number;
  hour: number;
  minute: number;
}

interface CourseInfo {
  courseId: number;
  courseCode: string;
  courseName: string;
  courseAcademicStatus: string;
  department: string;
  prereqs: string[];
}

interface ProctorTaInDepartmentRequest {
  requestId: number;
  requestType: string;
  description: string;
  senderName: string | null;
  receiverName: string;
  sentTime: DateInfo;
  courseCode: string | null;
  instrId: number;
  examName: string;
  examId: number;
  requiredTas: number;
  tasLeft: number;
  rejected: boolean;
  approved: boolean;
  pending: boolean;
}

interface AvailableTA {
  workload: number;
  hasAdjacentExam: boolean;
  taId: number;
  name: string;
  surname: string;
  academicLevel: 'BS' | 'MS' | 'PHD';
}

interface ExamDetailResponse {
  examId: number;
  duration: {
    start: {
      day: number;
      month: number;
      year: number;
      hour: number;
      minute: number;
    };
    finish: {
      day: number;
      month: number;
      year: number;
      hour: number;
      minute: number;
    };
    ongoing: boolean;
  };
  courseCode: string;
  type: string;
  examRooms: string[];
  requiredTas: number;
  workload: number;
}

// Define the type for course detail results
interface CourseDetailResult {
  requestId: number;
  courseCode: string;
  courseName: string | null;
  courseAcademicStatus: string | null;
}

// Define the TA type as used in the AssignProctorRow component
export interface TA {
  id: string | number;
  name: string;
  level: string;
  workload: number;
  hasAdjacentExam: boolean;
  wantedState: 'Prefered' | 'Unprefered' | 'None';
}

// Define the Exam interface
export interface Exam {
  id: string;
  courseName: string;
  courseId: string;
  level: string;
  examType: string;
  date: string;
  startTime: string;
  endTime: string;
  needed: number;
  tasLeft: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  requestId?: number; // Adding this to link back to the original request
  examId?: number;    // Adding this to fetch available TAs
  examRooms?: string[]; // Add exam rooms from the detailed response
}

const AssignProctor: React.FC = () => {
  const navigate = useNavigate();
  const [exams, setExams] = useState<Exam[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Department code - in a real app, this could come from context or localStorage
  const departmentCode = 'CS';

  // Confirmation states
  const [confirmId, setConfirmId] = useState<string|null>(null);
  const [confirmMsg, setConfirmMsg] = useState<string|null>(null);
  
  // Demand TA states
  const [demandId, setDemandId] = useState<string|null>(null);
  const [demandError, setDemandError] = useState<string|null>(null);
  const [demandConfirmMsg, setDemandConfirmMsg] = useState<string|null>(null);
  
  // Delete request states
  const [deleteId, setDeleteId] = useState<string|null>(null);
  const [deleteError, setDeleteError] = useState<string|null>(null);
  const [deleteConfirmMsg, setDeleteConfirmMsg] = useState<string|null>(null);

  // Fetch proctoring requests from the API
  useEffect(() => {
    const fetchRequests = async () => {
      try {
        setLoading(true);
        
        // Get all department requests
        const response = await axios.get<ProctorTaInDepartmentRequest[]>(
          `/api/department/${departmentCode}/receivedAll`
        );
        
        // Filter requests to only include ProctorTaInDepartment type
        const proctoringRequests = response.data.filter(
          req => req.requestType === 'ProctorTaInDepartment'
        );
        
        console.log('Filtered proctoring requests:', proctoringRequests);
        
        if (proctoringRequests.length === 0) {
          setExams([]);
          setLoading(false);
          return;
        }
        
        // This is a temporary array to store exams before we get more details
        const tempExams: Exam[] = [];
        
        // Arrays to store promises for fetching available TAs and exam details
        const taPromises: Promise<{examId: number, tas: AvailableTA[]}>[] = [];
        const examDetailPromises: Promise<{requestId: number, examId: number, details: ExamDetailResponse | null}>[] = [];
        const courseDetailPromises: Promise<CourseDetailResult>[] = [];
        
        // For each proctoring request, process it
        for(const req of proctoringRequests) {
          // Use the courseCode directly from the request if available, otherwise use fallback
          const courseCode = req.courseCode || `${req.receiverName || 'CS'}-464`;
          
          console.log(`Processing request ${req.requestId} with courseCode: ${courseCode}`);
          
          // Add promise to fetch available TAs
          const taPromise = axios.get<AvailableTA[]>(
            `/api/course/${courseCode}/proctoring/exam/${req.examId}`
          )
          .then(res => ({
            examId: req.examId,
            tas: res.data
          }))
          .catch(err => {
            console.error(`Failed to fetch TAs for exam ${req.examId}:`, err);
            return {
              examId: req.examId,
              tas: [] as AvailableTA[]
            };
          });
          
          taPromises.push(taPromise);
          
          // Add promise to fetch exam details
          const examDetailPromise = axios.get<ExamDetailResponse>(
            `/api/instructors/${courseCode}/exams/${req.examId}`
          )
          .then(res => ({
            requestId: req.requestId,
            examId: req.examId,
            details: res.data
          }))
          .catch(err => {
            console.error(`Failed to fetch exam details for ${req.examId}:`, err);
            return {
              requestId: req.requestId,
              examId: req.examId,
              details: null
            };
          });
          
          examDetailPromises.push(examDetailPromise);
          
          // Add promise to fetch course details
          const courseDetailPromise = axios.get<{ courseName: string; courseAcademicStatus: string; }>(
            `/api/course/${courseCode}`
          )
          .then(res => ({
            requestId: req.requestId,
            courseCode,
            courseName: res.data.courseName,
            courseAcademicStatus: res.data.courseAcademicStatus
          }))
          .catch(err => {
            console.error(`Failed to fetch course details for ${courseCode}:`, err);
            return {
              requestId: req.requestId,
              courseCode,
              courseName: null,
              courseAcademicStatus: null
            };
          });
          
          courseDetailPromises.push(courseDetailPromise);
          
          // Format date string from request for fallback
          const date = `${req.sentTime.year}-${String(req.sentTime.month).padStart(2, '0')}-${String(req.sentTime.day).padStart(2, '0')}`;
          
          // Add to temporary exams array with placeholder data
          tempExams.push({
            id: `e${req.requestId}`, // Generate a unique ID
            requestId: req.requestId, // Store original request ID
            examId: req.examId,       // Store original exam ID
            courseName: 'Loading...', // Placeholder until we fetch course info
            courseId: courseCode,     // Store the course code directly
            level: 'BS',              // Default level
            examType: req.examName,
            date: date,
            startTime: `${String(req.sentTime.hour).padStart(2, '0')}:${String(req.sentTime.minute).padStart(2, '0')}`,
            endTime: `${String(req.sentTime.hour + 2).padStart(2, '0')}:${String(req.sentTime.minute).padStart(2, '0')}`,
            needed: req.requiredTas,
            tasLeft: req.tasLeft,
            assignedTAs: [],
            potentialTAs: []
          });
        }
        
        // Wait for all promises to resolve
        const [tasResults, courseDetailResults, examDetailResults] = await Promise.all([
          Promise.all(taPromises),
          Promise.all(courseDetailPromises),
          Promise.all(examDetailPromises)
        ]);
        
        // Create maps for easy lookup
        const tasByExamId: Record<number, AvailableTA[]> = {};
        tasResults.forEach(result => {
          // Use Number() to ensure the index is a number
          tasByExamId[Number(result.examId)] = result.tas;
        });
        
        // Create mapping from requestId to course details
        const courseDetailsById: Record<number, CourseDetailResult> = {};
        courseDetailResults.forEach(result => {
          courseDetailsById[result.requestId] = result;
        });
        
        const examDetailsMap: Record<number, ExamDetailResponse> = {};
        examDetailResults.forEach(result => {
          if (result.details) {
            // Use Number() to ensure the index is a number
            examDetailsMap[Number(result.examId)] = result.details;
          }
        });
        
        // Format the detailed exams data
        const examsList: Exam[] = tempExams.map(exam => {
          // Get course details
          const courseDetail = courseDetailsById[exam.requestId || 0];
          
          // Get exam details from API response
          const examDetail = examDetailsMap[exam.examId || 0];
          
          // Format date and times from exam details if available
          let date = exam.date;
          let startTime = exam.startTime;
          let endTime = exam.endTime;
          let examRooms: string[] = [];
          
          if (examDetail && examDetail.duration) {
            // Use the data from the exam details API
            const start = examDetail.duration.start;
            const finish = examDetail.duration.finish;
            
            date = `${start.year}-${String(start.month).padStart(2, '0')}-${String(start.day).padStart(2, '0')}`;
            startTime = `${String(start.hour).padStart(2, '0')}:${String(start.minute).padStart(2, '0')}`;
            endTime = `${String(finish.hour).padStart(2, '0')}:${String(finish.minute).padStart(2, '0')}`;
            
            // Store exam rooms
            examRooms = examDetail.examRooms || [];
          }
          
          // Get available TAs for this exam
          const availableTAs = tasByExamId[exam.examId || 0] || [];
          
          // Transform available TAs
          const potentialTAs: TA[] = availableTAs.map(ta => ({
            id: ta.taId,
            name: `${ta.name} ${ta.surname}`,
            level: ta.academicLevel,
            workload: ta.workload,
            hasAdjacentExam: ta.hasAdjacentExam,
            wantedState: 'None'
          }));
          
          // Determine the best course name to use
          let courseName = 'Unknown Course';
          if (courseDetail && courseDetail.courseName) {
            courseName = courseDetail.courseName;
          } else if (examDetail && examDetail.courseCode) {
            courseName = `Course ${examDetail.courseCode}`;
          }
          
          // Determine the academic level
          let level = 'BS';
          if (courseDetail && courseDetail.courseAcademicStatus) {
            level = courseDetail.courseAcademicStatus;
          }
          
          return {
            ...exam,
            courseName: courseName,
            level: level,
            date: date,
            startTime: startTime,
            endTime: endTime,
            examType: examDetail?.type || exam.examType,
            examRooms: examRooms,
            potentialTAs: potentialTAs
          };
        });
        
        setExams(examsList);
      } catch (err) {
        console.error('Error fetching proctoring requests:', err);
        setError('Failed to load proctoring requests. Please try again later.');
      } finally {
        setLoading(false);
      }
    };
    
    fetchRequests();
  }, [departmentCode]);
  
  // Handler for auto-assign button
  const handleAuto = (id: string) => {
    // Get the exam by ID
    const exam = exams.find(e => e.id === id);
    if (!exam) {
      console.error(`Exam with ID ${id} not found`);
      return;
    }
    
    // Make sure we have both IDs before navigating
    if (exam.examId === undefined || exam.requestId === undefined) {
      console.error('Missing examId or requestId', exam);
      setDemandError('Cannot assign TAs: missing exam information');
      return;
    }
    
    // Navigate to the assignment page with both IDs in the URL
    navigate(`/department-office/assign-proctor/${exam.courseId}/${exam.examId}/${exam.requestId}`);
  };

  // Handler for finish button
  const handleFinish = (id: string) => {
    const exam = exams.find(e => e.id === id);
    if (!exam) {
      console.error(`Exam with ID ${id} not found`);
      return;
    }
    
    setConfirmId(id);
    setConfirmMsg(
      exam.tasLeft > 0
        ? "You didn't fill all needed TA positions. Are you sure you want to mark this assignment as finished?"
        : 'Mark this assignment as finished?'
    );
  };

  // Handler for finish confirmation
  const handleConfirmFinish = async () => {
    if (!confirmId) return;
    
    const exam = exams.find(e => e.id === confirmId);
    if (!exam || !exam.requestId) {
      console.error(`Exam with ID ${confirmId} not found or missing requestId`);
      return;
    }
    
    try {
      console.log(`Approving request ${exam.requestId}`);
      // API call to approve/complete the request
      await axios.put(`/api/request/${exam.requestId}/approve`);
      
      // Remove the exam from the list on success
      setExams(prev => prev.filter(e => e.id !== confirmId));
      
      // Clear confirmation state
      setConfirmId(null);
      setConfirmMsg(null);
    } catch (err) {
      console.error('Failed to finish assignment:', err);
      setDemandError('Failed to mark assignment as finished. Please try again.');
    }
  };

  // Handler for demand more TAs button
  const handleDemand = (id: string) => {
    const exam = exams.find(e => e.id === id);
    if (!exam) {
      console.error(`Exam with ID ${id} not found`);
      return;
    }
    
    // Check if more TAs are actually needed
    if (exam.tasLeft === 0) {
      setDemandError('Cannot request more TAs: no additional TAs are needed.');
      return;
    }
    
    setDemandId(id);
    setDemandConfirmMsg(
      "Requesting more TAs from the dean's office will revoke your current authorizations. Proceed?"
    );
  };

  // Handler for demand confirmation
  const handleConfirmDemand = async () => {
    if (!demandId) return;
    
    const exam = exams.find(e => e.id === demandId);
    if (!exam || !exam.requestId) {
      console.error(`Exam with ID ${demandId} not found or missing requestId`);
      return;
    }
    
    try {
      console.log(`Forwarding request ${exam.requestId} to dean's office`);
      // API call to forward the request to dean's office
      await axios.post(`/api/request/${exam.requestId}/forward-to-dean`);
      
      // Remove the exam from the list on success
      setExams(prev => prev.filter(e => e.id !== demandId));
      
      // Clear states
      setDemandId(null);
      setDemandConfirmMsg(null);
    } catch (err) {
      console.error('Failed to forward request:', err);
      setDemandError('Failed to forward request to dean\'s office. Please try again.');
    }
  };

  // Handler for delete request button
  const handleDelete = (id: string) => {
    const exam = exams.find(e => e.id === id);
    if (!exam) {
      console.error(`Exam with ID ${id} not found`);
      return;
    }
    
    setDeleteId(id);
    setDeleteConfirmMsg(
      `Are you sure you want to delete the proctoring request for ${exam.courseName} ${exam.examType}? This action cannot be undone.`
    );
  };

  // Handler for delete confirmation
  // Handler for delete confirmation
const handleConfirmDelete = async () => {
  if (!deleteId) return;
  
  const exam = exams.find(e => e.id === deleteId);
  if (!exam || !exam.requestId) {
    console.error(`Exam with ID ${deleteId} not found or missing requestId`);
    return;
  }
  
  try {
    // Get the user ID from localStorage
    const approverId = localStorage.getItem('userId');
    
    if (!approverId) {
      setDeleteError('User not authenticated. Please log in again.');
      return;
    }
    
    console.log(`Rejecting request ${exam.requestId} by approver ${approverId}`);
    
    // API call to reject the request using the specified endpoint
    await axios.put(`/api/ta/${approverId}/departmentproctor/${exam.requestId}/reject`);
    
    // Remove the exam from the list on success
    setExams(prev => prev.filter(e => e.id !== deleteId));
    
    // Clear states
    setDeleteId(null);
    setDeleteConfirmMsg(null);
  } catch (err) {
    console.error('Failed to reject request:', err);
    setDeleteError('Failed to reject request. Please try again.');
  }
};

  // Loading state
  if (loading) {
    return <LoadingPage />;
  }

  // Error state
  if (error) {
    return (
      <div className={styles.errorContainer}>
        <h2>Error</h2>
        <p>{error}</p>
        <button 
          className={styles.retryButton}
          onClick={() => window.location.reload()}
        >
          Retry
        </button>
      </div>
    );
  }

  // Render the component
  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>Assign Proctors to Exams</h1>
      </div>

      {exams.length === 0 ? (
        <div className={styles.noExams}>
          <p>No pending proctor assignments found.</p>
        </div>
      ) : (
        <div className={styles.container}>
          <table className={styles.table}>
            <thead className={styles.headings}>
              <tr>
                <th>Course Name</th>
                <th>Course ID</th>
                <th>Level</th>
                <th>Exam Type</th>
                <th>Date</th>
                <th>Start</th>
                <th>End</th>
                <th>Needed</th>
                <th>TAs Left</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {exams.map(exam => (
                <AssignProctorRow
                  key={exam.id}
                  exam={exam}
                  onAuto={handleAuto}
                  onFinish={handleFinish}
                  onDemand={handleDemand}
                  onDelete={handleDelete}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Confirmation dialogs */}
      {confirmMsg && (
        <ConPop 
          message={confirmMsg} 
          onConfirm={handleConfirmFinish}
          onCancel={() => {
            setConfirmMsg(null);
            setConfirmId(null);
          }} 
        />
      )}
      
      {demandError && (
        <ErrPopUp 
          message={demandError} 
          onConfirm={() => setDemandError(null)} 
        />
      )}
      
      {demandConfirmMsg && (
        <ConPop 
          message={demandConfirmMsg}
          onConfirm={handleConfirmDemand}
          onCancel={() => {
            setDemandConfirmMsg(null);
            setDemandId(null);
          }} 
        />
      )}
      
      {/* Delete confirmation dialog */}
      {deleteConfirmMsg && (
        <ConPop 
          message={deleteConfirmMsg}
          onConfirm={handleConfirmDelete}
          onCancel={() => {
            setDeleteConfirmMsg(null);
            setDeleteId(null);
          }} 
        />
      )}
      
      {deleteError && (
        <ErrPopUp 
          message={deleteError} 
          onConfirm={() => setDeleteError(null)} 
        />
      )}
    </div>
  );
};

export default AssignProctor;