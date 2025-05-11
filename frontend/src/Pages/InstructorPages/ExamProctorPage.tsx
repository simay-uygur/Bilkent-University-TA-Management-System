import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import BackBut from '../../components/Buttons/BackBut';
import ExamProctorReq from './ExamProctorRequest';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import LoadingPage from '../CommonPages/LoadingPage';
import styles from './ExamProctorPage.module.css';

// Define the exam interface to match the API response
export interface DateInfo {
  day: number;
  month: number;
  year: number;
  hour: number;
  minute: number;
}

export interface Duration {
  start: DateInfo;
  finish: DateInfo;
  ongoing: boolean;
}

export interface Exam {
  examId: number;
  duration: Duration;
  courseCode: string;
  type: string;
  examRooms: string[];
  requiredTas: number;
  workload: number;
}

interface ProctorRequest {
  examId: number;
  requiredTas: number;
}

interface ProctorTaRequestDto {
  receiverName: string;

   courseCode: string
  instrId: number;
  examId: number;
  examName: string;
  
  requiredTas: number;
  tasLeft: number;

  requestType: string;
  description: string;


  
}

const ExamProctorPage: React.FC = () => {
  const { courseID } = useParams<{ courseID: string }>();
  const courseCode = courseID || 'CS-464'; // Default for testing
  const requestType = "ProctorTaInDepartment";

  // State for exams and loading
  const [exams, setExams] = useState<Exam[]>([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);
  
  // State for request handling
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [confirmRequest, setConfirmRequest] = useState<ProctorRequest | null>(null);
  const [resetExamId, setResetExamId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState<boolean>(false);

  // Fetch exams on component mount
  useEffect(() => {
    const fetchExams = async () => {
      try {
        setLoading(true);
        setFetchError(null);
        
        const response = await axios.get<Exam[]>(`/api/instructors/${courseCode}/exams`);
        
        if (response.data && Array.isArray(response.data)) {
          setExams(response.data);
        } else {
          setFetchError('Invalid response format from server');
        }
      } catch (err) {
        console.error('Error fetching exams:', err);
        setFetchError('Failed to load exams. Please try again.');
      } finally {
        setLoading(false);
      }
    };
    
    fetchExams();
  }, [courseCode]);

  const handleSubmitRequest = (examId: number, requiredTas: number) => {
    if (requiredTas <= 0) {
      setErrorMsg("You must request at least 1 TA.");
      return;
    }
    
    setConfirmRequest({ examId, requiredTas });
  };



// Then update your handleConfirm function:
const handleConfirm = async () => {
  if (!confirmRequest) return;
  
  try {
    setSubmitting(true);
    
    const exam = exams.find(e => e.examId === confirmRequest.examId);
    if (!exam) {
      setErrorMsg('Exam not found');
      setConfirmRequest(null);
      return;
    }
    
    const instrId = parseInt(localStorage.getItem('userId') || '0', 10);
    if (!instrId) {
      setErrorMsg('User ID not found. Please log in again.');
      setConfirmRequest(null);
      return;
    }
    
     const depName = exam.courseCode.split('-')[0]; // Assuming the department name is part of the course code
    
    // Create a description with relevant exam information
    const examDate = exam.duration.start;
    const formattedDate = `${examDate.day}/${examDate.month}/${examDate.year}`;
    const formattedTime = `${examDate.hour}:${examDate.minute.toString().padStart(2, '0')}`;
    
    // Ensure all fields have proper values, especially IDs
    const requestDto: ProctorTaRequestDto = {
      receiverName: depName,

       courseCode: exam.courseCode,
      instrId: instrId,
      examId: exam.examId,
      examName: exam.type,

      requiredTas: confirmRequest.requiredTas,
      tasLeft: confirmRequest.requiredTas,

      requestType: requestType,
      description: `Request for ${confirmRequest.requiredTas} TAs to proctor ${exam.courseCode} ${exam.type} exam on ${formattedDate} at ${formattedTime}. Rooms: ${exam.examRooms.join(', ')}.`,

     
      
    };
    
    // Log the request for debugging
    console.log('Sending proctor request:', requestDto);
    
    // The URL was wrong - check with backend team for the correct endpoint
    await axios.post(`/api/proctor-in-faculty-department/${instrId}`, requestDto);
    
    // Show success
    setSubmitSuccess(true);
    
    // Remove the exam from the list
    setExams(prev => prev.filter(e => e.examId !== confirmRequest.examId));
    
    // Reset state
    setResetExamId(confirmRequest.examId);
    setConfirmRequest(null);
    
    // Hide success message after delay
    setTimeout(() => {
      setSubmitSuccess(false);
    }, 3000);
    
  } catch (err: any) {
    console.error('Error submitting request:', err);
    
    // More detailed error handling
    let errorMessage = 'Failed to submit request. Please try again.';
    
    if (err.response) {
      // Server responded with an error
      console.error('Error response:', err.response.data);
      if (err.response.data && err.response.data.message) {
        errorMessage = `Server error: ${err.response.data.message}`;
      }
    }
    
    setErrorMsg(errorMessage);
  } finally {
    setSubmitting(false);
  }
};

  if (loading) {
    return <LoadingPage />;
  }

  // Add this wrapper div in the return statement:
return (
  <div className={styles.container}>
    <div className={styles.headerRow}>
      <BackBut to="/instructor" />
      <h1 className={styles.title}>Exam Proctoring for {courseCode}</h1>
    </div>

    {fetchError && (
      <div className={styles.errorBanner}>
        <p>{fetchError}</p>
        <button 
          onClick={() => window.location.reload()}
          className={styles.retryButton}
        >
          Retry
        </button>
      </div>
    )}

    {submitSuccess && (
      <div className={styles.successBanner}>
        Your proctor request was submitted successfully!
      </div>
    )}

    <div className={styles.mainContainer}>
      {exams.length > 0 ? (
        <div className={styles.examCardsContainer}>
          <ExamProctorReq
            exams={exams}
            onSubmitRequest={handleSubmitRequest}
            resetExamId={resetExamId}
            onResetDone={() => setResetExamId(null)}
            submitting={submitting}
          />
        </div>
      ) : (
        <div className={styles.noExams}>
          <p>No exams available for proctor requests.</p>
        </div>
      )}
    </div>

    {errorMsg && (
      <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />
    )}

    {confirmRequest && (
      <ConPop
        message={`Request ${confirmRequest.requiredTas} TAs for this exam?`}
        onConfirm={handleConfirm}
        onCancel={() => setConfirmRequest(null)}
      />
    )}
  </div>
);
};

export default ExamProctorPage;
/* // src/pages/ExamProctor/ExamProctorPage.tsx
import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ExamProctorReq, { Exam } from './ExamProctorRequest';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './ExamProctorPage.module.css';

interface RawRequest {
  examId: string;
  neededTAs: number;
}

interface BackendPayload {
  examId: string;
  name: string;
  type: string;
  studentCount: number;
  neededTAs: number;
}

const ExamProctorPage: React.FC = () => {
  const { courseID } = useParams<{ courseID: string }>()
  const courseCode = courseID!;

  const initialExams: Exam[] = [
    { id: `${courseCode}-mid`,   name: `${courseCode} Midterm`, type: 'Midterm', studentCount: 120 },
    { id: `${courseCode}-final`, name: `${courseCode} Final`,   type: 'Final',   studentCount: 120 },
  ];

  // now with setter so we can remove one
  const [exams, setExams] = useState<Exam[]>(initialExams);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [confirmData, setConfirmData] = useState<RawRequest | null>(null);
  const [resetExamId, setResetExamId] = useState<string | null>(null);

  const handleSubmit = (requests: RawRequest[]) => {
    const req = requests[0];
    if (req.neededTAs === 0) {
      setErrorMsg("You can't request 0 TAs.");
    } else {
      setConfirmData(req);
    }
  };

  const handleConfirm = () => {
    if (!confirmData) return;
    const { examId, neededTAs } = confirmData;
    const exam = exams.find(e => e.id === examId)!;
    const payload: BackendPayload = {
      examId,
      name: exam.name,
      type: exam.type,
      studentCount: exam.studentCount,
      neededTAs,
    };
    console.log('Sending to backend:', payload);
    // TODO: POST payload to your API

    // remove that exam card
    setExams(prev => prev.filter(e => e.id !== examId));
    // reset and clear popup
    setConfirmData(null);
    setResetExamId(examId);
  };

  return (
    <div className={styles.container}>
      <div className={styles.headerRow}>
        <BackBut to="/instructor" />
        <h1 className={styles.title}>Exam Proctoring for {courseCode}</h1>
      </div>

      <div className={styles.mainContainer}>
        <ExamProctorReq
          exams={exams}
          onSubmit={handleSubmit}
          resetExamId={resetExamId || undefined}
          onResetDone={() => setResetExamId(null)}
        />
      </div>

      {errorMsg && (
        <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />
      )}

      {confirmData && (
        <ConPop
          message="Are you sure you want to submit this request?"
          onConfirm={handleConfirm}
          onCancel={() => setConfirmData(null)}
        />
      )}
    </div>
  );
};

export default ExamProctorPage;
 */