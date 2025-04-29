import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import styles from './ExamProctoringPage.module.css';

const ExamProctoringPage: React.FC = () => {
  const { examId } = useParams(); // Getting exam ID from route params
  const [numTAs, setNumTAs] = useState(1);

  const handleExamProctoringRequest = () => {
    fetch(`/api/exam/proctoring/${examId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ numberOfTAs: numTAs }),
    })
      .then(response => response.json())
      .then(data => {
        console.log('Request sent successfully', data);
      })
      .catch(error => console.error('Error:', error));
  };

  return (
    <div className={styles.pageWrapper}>
      <h1>Request TAs for Exam {examId}</h1>
      <label>Number of TAs</label>
      <input
        type="number"
        value={numTAs}
        onChange={e => setNumTAs(Number(e.target.value))}
        min="1"
      />
      <button onClick={handleExamProctoringRequest}>Submit</button>
    </div>
  );
};

export default ExamProctoringPage;
