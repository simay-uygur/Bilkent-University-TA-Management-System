// src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './Pages/ProtectedRoute';

import TALayout            from './components/Layouts/TALayout';
import InstructorLayout    from './components/Layouts/InstructorLayout';
import DepartmentLayout    from './components/Layouts/DepartmentLayout';
import DeansLayout         from './components/Layouts/DeansLayout';

import DeansDepartmentDetailPage from './Pages/DeanOfficePages/DeansDepartmentDetailPage';
//import DeansOffice            from './Pages/DeanOfficePages/DeansOffice';
import DepartmentOffice from './Pages/DepOfficePages/DepartmentOffice';

import CourseDetails  from './Pages/DepOfficePages/DepartmentCourseDetails';
import InstructorDetails from './Pages/DepOfficePages/DepartmentInsturctorDetails';

import Login               from './Pages/CommonPages/Login';
import TAMainPage from './Pages/TAPages/TAMainPage';
import Notification from './Pages/CommonPages/Notification';
import InstructorMainPage from './Pages/InstructorPages/InstructorMainPage';
import MakeLeaveRequest from './Pages/TAPages/MakeLeaveRequest';
import TAMonthlySchedulePage from './Pages/TAPages/TAMonthlySchedulePage';
import TAViewProctoringPage from './Pages/TAPages/TAViewProctoringPage';
import Settings from './Pages/CommonPages/Settings';
import CourseTARequest from './Pages/InstructorPages/CourseTARequest';
import AssignTATask from './Pages/InstructorPages/AssignTATask';
import ViewAddExam from './Pages/DepOfficePages/ViewAddExam';
import SelectTACourse from './Pages/DepOfficePages/SelectTACourse';
import AssignTACourse from './Pages/DepOfficePages/AssignTACourse';
import AssignTAProctor from './Pages/DepOfficePages/AssignTAProctor';
import AssignProctor from './Pages/DepOfficePages/AssignProctor';
import ManageWorkload from './Pages/InstructorPages/ManageWorkload';
import ExamProctorPage from './Pages/InstructorPages/ExamProctorPage';
import AdminMainPage from './Pages/AdminPages/AdminMainPage';
import AdminLayout from './components/Layouts/AdminLayout';
import ViewLogs from './Pages/AdminPages/ViewLogs';
import ProctorTASelection from './Pages/DeanOfficePages/ProctorTASelection';
import ProctorCourseSelection from './Pages/DeanOfficePages/ProctorCourseSelection';
import DeansOffice from "./Pages/DeanOfficePages/DeansOffice.tsx";
import AddExam from "./Pages/DeanOfficePages/AddExam.tsx";

const App: React.FC = () => (
  <BrowserRouter>
    <Routes>

      {/* Public */}
      <Route path="/login" element={<Login />} />
      {/* TA Area (requires ROLE_TA) */}
      <Route element={<ProtectedRoute requiredRole="ROLE_TA" />}>
        <Route path="/ta" element={<TALayout />}>
          <Route index element={<TAMainPage />} />
          <Route path="/ta/leave-request" element={<MakeLeaveRequest />} />
          <Route path="/ta/monthly-schedule" element={<TAMonthlySchedulePage />} />
          <Route path="/ta/view-proctoring" element={<TAViewProctoringPage />} />
          <Route path="/ta/settings" element={<Settings />} />
          <Route path="/ta/notification" element={<Notification />} />
        </Route>
      </Route>

      {/* Instructor Area (requires ROLE_INSTRUCTOR) */}
      <Route element={<ProtectedRoute requiredRole="ROLE_INSTRUCTOR" />}>
        <Route path="/instructor" element={<InstructorLayout />}>
          <Route index element={<InstructorMainPage />} />
          <Route path="/instructor/exam-proctor-request/:courseID" element={<ExamProctorPage />} />
          <Route path="/instructor/exam-printing/:courseID" element={<ExamProctorPage />} />
          <Route path="/instructor/assign-course/:courseID/:courseSec" element={<CourseTARequest />} />
          <Route path="/instructor/workload/:courseID/:courseSec" element={<ManageWorkload />} />
          <Route path="/instructor/workload/:courseID/:courseSec/:taskID" element={<AssignTATask />} />
          <Route path="/instructor/settings" element={<Settings />} />
          <Route path="/instructor/notification" element={<Notification />} />
        </Route>
      </Route>

      {/* Department Office Area (requires ROLE_DEPARTMENT) */}
      <Route element={<ProtectedRoute requiredRole="ROLE_DEPARTMENT_STAFF" />}>
        <Route path="/department-office" element={<DepartmentLayout />}>
          <Route index element={<DepartmentOffice />} />
          <Route path="/department-office/:courseCode" element={<CourseDetails />} />
          <Route path="/department-office/instructor/:id" element={<InstructorDetails />} />
          <Route path="/department-office/assign-course/:courseID/:courseSec" element={<SelectTACourse/>} />
          <Route path="/department-office/assign-course" element={<AssignTACourse />} />
          <Route path="/department-office/assign-proctor/:examID" element={<AssignTAProctor />} />
          <Route path="/department-office/assign-proctor" element={<AssignProctor />} />
          <Route path="/department-office/settings" element={<Settings />} />
          <Route path="/department-office/notification" element={<Notification />} />
        </Route>
      </Route>

      {/* Dean’s Office Area (requires ROLE_DEAN) */}
      <Route element={<ProtectedRoute requiredRole="ROLE_DEANS_OFFICE" />}>
        <Route path="/deans-office" element={<DeansLayout />}>
          <Route index element={<DeansOffice />} />
          <Route path= "/deans-office/proctor"  element={<ProctorCourseSelection />} />
          <Route path="/deans-office/department/:dept" element={<DeansDepartmentDetailPage />}/>
          <Route path="/deans-office/settings" element={<Settings />} />
          <Route path="/deans-office/notification" element={<Notification />} />
          <Route path="/deans-office/proctor/:examID" element={<ProctorTASelection />} />
          <Route path="/deans-office/view-add-exams" element={<ViewAddExam />} />
          <Route path="/deans-office/add-exams" element={<AddExam />} />
        </Route>
      </Route>

      {/* Admin's Pages */}
      {/*<Route element={<ProtectedRoute requiredRole="ROLE_ADMIN" />}>*/}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminMainPage />} />
          <Route path= "/admin/view-logs"  element={<ViewLogs />} />
          <Route path="/admin/settings" element={<Settings />} />
          <Route path="/admin/notification" element={<Notification />} />
        </Route>
      {/*</Route>*/}

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  </BrowserRouter>
);
export default App;


/* 
const App: React.FC = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/ta" element={<TAMainPage />} />
      <Route path="/ins" element={<InsMainPage />} />
      <Route path="/asgnTAC" element={<AssignTACourse />} />
      <Route path="/asgnTA/:courseCode" element={<SelectTACourse />} />
      <Route path="/courseTA/:courseCode" element={<CourseTAReq />} />
      <Route path="/man/:courseCode" element={<ManageWorkload />} />
      <Route path="/man/:courseCode/:taskid" element={<AssignTATask />} />
      <Route path="/set" element={<Settings />} />
      <Route path="/make" element={<MakeLeaveReq />} />
      <Route path="/mon" element={<TAMonSchPage />} />
      <Route path="/adm" element={<AdminMainPage />} />
      <Route path="/viewP" element={<TAViewPPage />} />
      <Route path="/not" element={<Not />} />
      <Route path="/viewL" element={<ViewLogs />} />
      <Route path="/examProc" element={<ExamProctorPage />} />
      <Route path="/asP" element={<AssignProctor />} />
      <Route path="/assign/:examId" element={<AssignTA />} />
      <Route path="/examProc/:courseCode" element={<ExamProctorPage />} />
      <Route path="/dean" element={<DeansOffice />} />
      <Route path="/de" element={<DeansDepartmentDetailPage />} />
      <Route path="/deanP" element={<DeansProctoringPage />} />
      <Route path="/viewE" element={<ViewAddExam />} />
      <Route path="/inotfac" element={<ProctorLeftTA />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  </BrowserRouter> */
  /*<Router>
          <Routes>
            {}
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<Login />} />

            {}
            <Route
              path="/*"
              element={
                <ProtectedRoute>
                  <Routes>
                    <Route path="/ta" element={<TAMainPage />} />
                    <Route path="/vol" element={<VolunteerProctoringPage />} />
                    <Route path="/ins" element={<InsMainPage />} />
                    <Route path="/setTA" element={<SettingsTA />} />
                    <Route path="/man" element={<ManageWorkload />} />
                    <Route path="/set" element={<Settings />} />
                    <Route path="/make" element={<MakeLeaveReq />} />
                    <Route path="/mon" element={<TAMonSchPage />} />
                    <Route path="/adm" element={<AdminMainPage />} />
                    <Route path="/viewP" element={<TAViewPPage />} />
                    <Route path="/not" element={<Not />} />
                    <Route path="*" element={<Navigate to="/login" replace />} />
                  </Routes>
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
    </Router>
);
*/