import React from 'react';
import { X } from 'lucide-react';
import type { Instructor } from './InsMainPage';
import styles from './CourseInfoPanel.module.css';

interface Course {
  id: number;
  code: string;
  name: string;
  sec: number;
  studentNum: number;
  instructor: Instructor;
}

interface CourseInfoPanelProps {
  course: Course;
  onClose: () => void;
}

const CourseInfoPanel: React.FC<CourseInfoPanelProps> = ({ course, onClose }) => (
  <div className={styles.backdrop} onClick={onClose}>
    <div className={styles.modal} onClick={e => e.stopPropagation()}>
      <header className={styles.header}>
        <h2>Course Info</h2>
        <button className={styles.closeBtn} onClick={onClose}>
          <X size={20} />
        </button>
      </header>
      <div className={styles.body}>
        <p><strong>Code:</strong> {course.code}</p>
        <p><strong>Section:</strong> {course.sec}</p>
        <p><strong>Name:</strong> {course.name}</p>
        <p>
          <strong>Instructor:</strong>{' '}
          {course.instructor.name} {course.instructor.surname}
        </p>
        <p><strong>Enrollment:</strong> {course.studentNum} students</p>
      </div>
    </div>
  </div>
);

export default CourseInfoPanel;