import React from 'react';
import AdminNavBar from '../../components/NavBars/AdminNavBar';
import BulkUpload from '../../components/BulkUpload';
import styles from './AdminMainPage.module.css';

const AdminMainPage: React.FC = () => {
  const handleUploadTA = (file: File) => {
    console.log('Uploaded Users file:', file);
    // TODO: parse and import users
  };
  const handleImportTA = () => console.log('Import Users clicked');
  const handleCancelTA = () => console.log('Cancel Users clicked');

  const handleUploadClassrooms = (file: File) => {
    console.log('Uploaded Classrooms file:', file);
    // TODO: parse and import classrooms
  };
  const handleImportClassrooms = () => console.log('Import Classrooms clicked');
  const handleCancelClassrooms = () => console.log('Cancel Classrooms clicked');

  const handleUploadInstructors = (file: File) => {
    console.log('Uploaded Faculties file:', file);
    // TODO: parse and import faculties
  };
  const handleImportInstructors = () => console.log('Import Faculties clicked');
  const handleCancelInstructors = () => console.log('Cancel Faculties clicked');

  const handleUploadCourses = (file: File) => {
    console.log('Uploaded Courses file:', file);
    // TODO: parse and import courses
  };
  const handleImportCourses = () => console.log('Import Courses clicked');
  const handleCancelCourses = () => console.log('Cancel Courses clicked');

  return (
    <div>
      
      <div className={styles.container}>
      <BulkUpload
        label="Upload Students/TAs"
        onUpload={handleUploadTA}
        primaryButtonText="Import By File"
        onPrimaryClick={handleImportTA}
        secondaryButtonText="Add TA or Student"
        onSecondaryClick={handleCancelTA}
      />
      <BulkUpload
        label="Upload Instructors"
        onUpload={handleUploadInstructors}
        primaryButtonText="Import By File"
        onPrimaryClick={handleImportInstructors}
        secondaryButtonText="Add Instructor"
        onSecondaryClick={handleCancelInstructors}
      />
      <BulkUpload
        label="Upload Classrooms"
        onUpload={handleUploadClassrooms}
        primaryButtonText="Import By File"
        onPrimaryClick={handleImportClassrooms}
        secondaryButtonText="Add Classroom"
        onSecondaryClick={handleCancelClassrooms}
      />
      <BulkUpload
        label="Upload Courses"
        onUpload={handleUploadCourses}
        primaryButtonText="Import By File"
        onPrimaryClick={handleImportCourses}
        secondaryButtonText="Add Course"
        onSecondaryClick={handleCancelCourses}
      />
    </div>
    </div>
  );
};

export default AdminMainPage;