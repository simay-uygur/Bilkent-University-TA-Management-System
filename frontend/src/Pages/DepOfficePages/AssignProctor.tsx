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
  const [courseInfo, setCourseInfo] = useState<Record<string, CourseInfo>>({});
  // Fetch proctoring requests from the API
  // Add this interface for course data
interface CourseInfo {
  courseId: number;
  courseCode: string;
  courseName: string;
  courseAcademicStatus: string;
  department: string;
  prereqs: string[];
}
  // Fetch proctoring requests from the API
  useEffect(() => {
    const fetchRequests = async () => {
      try {
        setLoading(true);
        const response = await axios.get<ProctorTaInDepartmentRequest[]>(
          `/api/department/${departmentCode}/receivedAll`
        );
        
        // Filter requests to only include ProctorTaInDepartment type
        const proctoringRequests = response.data.filter(
          req => req.requestType === 'ProctorTaInDepartment'
        );
        
        // Create a set of unique course codes
        const courseCodes = new Set<string>();
        
        // This is a temporary array to store exams before we get the course names
        const tempExams: Exam[] = [];
        
        // Array to store promises for fetching available TAs
        const taPromises: Promise<{examId: number, tas: AvailableTA[]}>[] = [];
        
        // For each proctoring request, process it
        proctoringRequests.forEach(req => {
          // Extract the course code from the receiverName/department or use default
          const courseCode = `${req.receiverName || 'CS'}-464`; // Default to CS-464 if missing
          courseCodes.add(courseCode);
          
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
          
          // Format date string
          const date = `${req.sentTime.year}-${String(req.sentTime.month).padStart(2, '0')}-${String(req.sentTime.day).padStart(2, '0')}`;
          
          // Add to temporary exams array
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
            endTime: `${String(req.sentTime.hour + 2).padStart(2, '0')}:${String(req.sentTime.minute).padStart(2, '0')}`, // Assuming 2-hour exams
            needed: req.requiredTas,
            tasLeft: req.tasLeft,
            assignedTAs: [], // No assigned TAs initially
            potentialTAs: [] // Will be populated after we get the TA data
          });
        });
        
        // Create promises for fetching course info
       // Update the part that creates promises for fetching course info:
// Update the part that creates promises for fetching course info:
const coursePromises = Array.from(courseCodes).map(courseCode => 
  axios.get<CourseInfo>(`/api/course/${courseCode}`, {
    // Configure axios to handle 302 response codes
    validateStatus: function (status) {
      return (status >= 200 && status < 300) || status === 302; // Accept 2xx and 302 status codes
    }
  })
    .then(res => {
      // If we got a 302 response, don't use fallback data
      if (res.status === 302) {
        console.log(`Received 302 for course ${courseCode}, using original data from the response`);
        
        // Try to extract data from the response if available
        if (res.data) {
          return {
            courseCode,
            courseInfo: res.data
          };
        }
        
        // If no data in response, return null to indicate we should use the exam's original data
        return {
          courseCode,
          courseInfo: null
        };
      }
      
      // Normal success response
      return {
        courseCode,
        courseInfo: res.data
      };
    })
    .catch(err => {
      // This will only trigger for network errors or status codes not in validateStatus
      console.error(`Failed to fetch course info for ${courseCode}:`, err);
      return {
        courseCode,
        courseInfo: null // Return null instead of fallback data
      };
    })
);
        
        // Wait for all TA and course info promises to resolve
        const [tasResults, courseResults] = await Promise.all([
          Promise.all(taPromises),
          Promise.all(coursePromises)
        ]);
        
        // Create maps for easy lookup
        const tasByExamId: Record<number, AvailableTA[]> = {};
        tasResults.forEach(result => {
          tasByExamId[result.examId] = result.tas;
        });
        
        const courseInfoMap: Record<string, CourseInfo | null> = {};
courseResults.forEach(result => {
  courseInfoMap[result.courseCode] = result.courseInfo;
});
        
        setCourseInfo(courseInfoMap as Record<string, CourseInfo>);

// Now update the exams with course names and TAs
const examsList: Exam[] = tempExams.map(exam => {
  // Get course info - might be null if we got a 302
  const course = courseInfoMap[exam.courseId];
  
  // Get available TAs for this exam
  const availableTAs = tasByExamId[exam.examId || 0] || [];
  
  // Transform available TAs to the format expected by AssignProctorRow
  const potentialTAs: TA[] = availableTAs.map(ta => ({
    id: ta.taId,
    name: `${ta.name} ${ta.surname}`,
    level: ta.academicLevel,
    workload: ta.workload,
    hasAdjacentExam: ta.hasAdjacentExam,
    wantedState: 'None' // Default state
  }));
  
          
            return {
    ...exam,
    // Only update courseName if we have valid course data
    courseName: course ? course.courseName : exam.courseName,
    // Only update level if we have valid course data
    level: course && course.courseAcademicStatus ? course.courseAcademicStatus : exam.level,
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
  
  // Fix the handlers to ensure they work correctly
  const handleAuto = (id: string) => {
    // Get the exam by ID
    const exam = exams.find(e => e.id === id);
    if (!exam) {
      console.error(`Exam with ID ${id} not found`);
      return;
    }
    
    // Navigate to the assignment details page with exam and request IDs
    navigate(`/department-office/assign-proctor/${exam.examId}/${exam.requestId}`);
  };

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

  const handleDemand = (id: string) => {
    const exam = exams.find(e => e.id === id);
    if (!exam) {
      console.error(`Exam with ID ${id} not found`);
      return;
    }
    
    if (exam.tasLeft === 0) {
      setDemandError('Cannot request more TAs: no additional TAs are needed.');
      return;
    }
    
    setDemandId(id);
    setDemandConfirmMsg(
      "Requesting more TAs from the dean's office will revoke your current authorizations. Proceed?"
    );
  };

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

// Rest of your component remains the same...

  if (loading) {
    return <LoadingPage />;
  }

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
                />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {confirmMsg && (
        <ConPop 
          message={confirmMsg} 
          onConfirm={handleConfirmFinish}
          onCancel={() => setConfirmMsg(null)} 
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
          onCancel={() => setDemandConfirmMsg(null)} 
        />
      )}
    </div>
  );
};

export default AssignProctor;
/* // src/pages/AssignProctor/AssignProctor.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AssignProctorRow, { Exam } from './AssignProctorRow';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import styles from './AssignProctor.module.css';
import BackBut from '../../components/Buttons/BackBut';

const sampleExams: Exam[] = [
  {
    id: 'e1',
    courseName: 'Algorithms',
    courseId: 'CS225',
    level: 'BS',
    examType: 'Midterm',
    date: '2025-05-10',
    startTime: '09:00',
    endTime: '11:00',
    needed: 3,
    tasLeft: 1,
    assignedTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'Prefered' },
      { id: 'ta2', name: 'Ayşe Fatma', level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'None'     },
    ],
    potentialTAs: [
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, wantedState: 'Unprefered' },
      { id: 'ta4', name: 'Jane Doe',    level: 'BS',  workload: 3, hasAdjacentExam: true,  wantedState: 'None'       },
    ],
  },
  {
    id: 'e2',
    courseName: 'Data Structures',
    courseId: 'CS226',
    level: 'MS',
    examType: 'Final',
    date: '2025-06-01',
    startTime: '13:00',
    endTime: '15:00',
    needed: 4,
    tasLeft: 0,
    assignedTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'None'     },
      { id: 'ta2', name: 'Ayşe Fatma', level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'Prefered' },
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, wantedState: 'Unprefered' },
      { id: 'ta4', name: 'John Smith', level: 'BS',  workload: 3, hasAdjacentExam: true,  wantedState: 'None'       },
    ],
    potentialTAs: [
      { id: 'ta5', name: 'Emily Johnson', level: 'PhD', workload: 5, hasAdjacentExam: true, wantedState: 'None' },
    ],
  },
  {
    id: 'e3',
    courseName: 'Machine Learning',
    courseId: 'CS450',
    level: 'PhD',
    examType: 'Midterm',
    date: '2025-05-20',
    startTime: '10:00',
    endTime: '12:00',
    needed: 2,
    tasLeft: 2,
    assignedTAs: [],
    potentialTAs: [
      { id: 'ta1', name: 'Ali Veli',        level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'Prefered' },
      { id: 'ta2', name: 'Ayşe Fatma',     level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'None'     },
    ],
  },
];

const AssignProctor: React.FC = () => {
  const navigate = useNavigate();
  const [exams, setExams] = useState<Exam[]>(sampleExams);

  // finish-assignment popups
  const [confirmId, setConfirmId]     = useState<string|null>(null);
  const [confirmMsg, setConfirmMsg]   = useState<string|null>(null);
  // demand-TA popups
  const [demandId, setDemandId]         = useState<string|null>(null);
  const [demandError, setDemandError]   = useState<string|null>(null);
  const [demandConfirmMsg, setDemandConfirmMsg] = useState<string|null>(null);

  const handleAuto = (id: string) => {
    navigate(`/department-office/assign-proctor/${id}`);
  };

  const handleFinish = (id: string) => {
    const exam = exams.find(e => e.id === id);
    if (!exam) return;
    setConfirmMsg(
      exam.tasLeft > 0
        ? "You didn't fill needed TAs. Are you sure?"
        : 'Mark this assignment as finished?'
    );
    setConfirmId(id);
  };

  const handleConfirmFinish = () => {
    if (confirmId) setExams(prev => prev.filter(e => e.id !== confirmId));
    setConfirmMsg(null);
    setConfirmId(null);
  };

  const handleDemand = (id: string) => {
    const exam = exams.find(e => e.id === id);
    if (!exam) return;
    if (exam.tasLeft === 0) {
      setDemandError('Cannot request more TAs: none left.');
      return;
    }
    setDemandConfirmMsg(
      "Requesting from the dean’s office will revoke your current authorizations. Proceed?"
    );
    setDemandId(id);
    setDemandError(null);
  };

  const handleConfirmDemand = () => {
    if (demandId) setExams(prev => prev.filter(e => e.id !== demandId));
    setDemandConfirmMsg(null);
    setDemandId(null);
    setDemandError(null);
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>Course of Exams</h1>
      </div>

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
              />
            ))}
          </tbody>
        </table>
      </div>

      {confirmMsg && (
        <ConPop message={confirmMsg} onConfirm={handleConfirmFinish}
          onCancel={() => setConfirmMsg(null)} />
      )}
      {demandError && (
        <ErrPopUp message={demandError} onConfirm={() => setDemandError(null)} />
      )}
      {demandConfirmMsg && (
        <ConPop message={demandConfirmMsg}
          onConfirm={handleConfirmDemand}
          onCancel={() => setDemandConfirmMsg(null)} />
      )}
    </div>
  );
};

export default AssignProctor;
 */