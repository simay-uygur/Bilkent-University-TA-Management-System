import React from 'react';
import { X } from 'lucide-react';
import type { Instructor } from './InstructorMainPage';
import styles from './CourseInfoPanel.module.css';

export interface Section {
  sectionId: number;
  sectionCode: string;
  lessons: any[];
  instructor: Instructor;
  tas: any[];
  students: any[];
  name?: string; // This will hold the course name from your API
}

interface CourseInfoPanelProps {
  course: Section;
  onClose: () => void;
}

const CourseInfoPanel: React.FC<CourseInfoPanelProps> = ({ course, onClose }) => {
  // Parse section code to extract course code and section number
  const parts = course.sectionCode ? course.sectionCode.split('-') : [];
  const courseCode = parts.length >= 2 ? `${parts[0]}-${parts[1]}` : course.sectionCode;
  const sectionNum = parts.length >= 3 ? parts[2] : '1';
  const semester = parts.length >= 5 ? parts[4] : '';
  const year = parts.length >= 4 ? parts[3] : '';
  
  return (
    <div className={styles.backdrop} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <header className={styles.header}>
          <h2>Course Info</h2>
          <button className={styles.closeBtn} onClick={onClose}>
            <X size={20} />
          </button>
        </header>
        <div className={styles.body}>
          <p><strong>Code:</strong> {courseCode}</p>
          <p><strong>Section:</strong> {sectionNum}</p>
          <p><strong>Term:</strong> {semester} {year}</p>
          <p><strong>Name:</strong> {course.name || 'Unknown Course'}</p>
          <p>
            <strong>Instructor:</strong>{' '}
            {course.instructor.name} {course.instructor.surname}
          </p>
          <p><strong>Enrollment:</strong> {course.students?.length || 0} students</p>
          <p><strong>Teaching Assistants:</strong> {course.tas?.length || 0}</p>
          {course.tas?.length > 0 && (
            <div className={styles.taList}>
              {course.tas.map((ta, index) => (
                <div key={index} className={styles.taItem}>
                  {ta.name} {ta.surname}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CourseInfoPanel;
/* import React from 'react';
import { X } from 'lucide-react';
import type { Instructor } from './InstructorMainPage';
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

export default CourseInfoPanel; */