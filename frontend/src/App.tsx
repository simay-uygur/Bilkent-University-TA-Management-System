// src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import ProtectedRoute from './Pages/ProtectedRoute';

import Login               from './Pages/Login';
import AdminDashboard      from './Benim/AdminDashboard';
import TADashboard         from './Pages/TADashboard';
//import VolunteerProctoring from './Pages/VolunteerProctoring';
import Notifications       from './Pages/Notifications';
import Settings            from './Pages/Settings';
import LeaveRequestForm    from './Benim/LeaveRequestForm';

import TALayout            from './components/TALayout';
import InstructorLayout    from './components/InstructorLayout';
import DepartmentLayout    from './components/DepartmentLayout';
import DeansLayout         from './components/DeansLayout';

//import InsMainPage         from './Pages/InsMainPage';
import ExamProctoringPage  from './components/ExamProctoringPage';
import CourseTAList        from './Benim/CourseTAList';
import RequestTAForm       from './Benim/RequestTAForm';
import SettingsTA          from './Benim/SettingsTA';
//import ManageWorkload      from './Benim/ManageWorkload';

import ProctorAssignmentsPage from './components/ProctorAssignmentPage';
import LeaveRequestsPage      from './components/LeaveRequestPage';
import DeansOffice            from './Pages/DeansOffice';
import DeanAssignProctors     from './components/DeanAssignProctors';
import DepartmentOffice from './Pages/DepartmentOffice';


import TAMainPage from './Pages/TAPages/TAMainPage';
import MakeLeaveReq from './Pages/TAPages/MakeLeaveReq';
import TAMonSchPage from './Pages/TAPages/TAMonSchPage';
import TAViewPPage from './Pages/TAPages/TAViewPPage';
import CourseTAReq from './Pages/InstructorPages/CourseTAReq';
import AssignTATask from './Pages/InstructorPages/AssignTATask';
import ExamProctorPage from './Pages/InstructorPages/ExamProctorPage';
import ManageWorkload from './Pages/InstructorPages/ManageWorkload';
import InsMainPage from './Pages/InstructorPages/InsMainPage';
import AssignTACourse from './Pages/DepOfficePages/AssignTACourse';
import SelectTACourse from './Pages/DepOfficePages/SelectTACourse';
import ViewAddExam from './Pages/DepOfficePages/ViewAddExam';
import DeansProctoringPage from './components/DeansProctoringPage';
import ProctorLeftTA from './Pages/DeanOfficePages/ProctorLeftTA';
import DepartmentCourseDetails from './components/DepartmentCourseDetails';
import DepartmentInsturctorDetails from './components/DepartmentInsturctorDetails';

const App: React.FC = () => (
  <BrowserRouter>
    <Routes>

      {/* Public */}
      <Route path="/login" element={<Login />} />
      <Route path="/admin" element={<AdminDashboard />} />

      {/* Shared */}
      <Route path="/notifications" element={<Notifications />} />
      <Route path="/settings"      element={<Settings />} />

      {/* TA Area (requires ROLE_TA) */}
      <Route element={<ProtectedRoute requiredRole="ROLE_TA" />}>
        <Route element={<TALayout />}>
          <Route path="/ta"             element={<TAMainPage />} />
          <Route path="/dashboard"             element={<TAMainPage />} />
        <Route path="/make" element={<MakeLeaveReq />} />
           <Route path="/mon" element={<TAMonSchPage />} />
           <Route path="/viewP" element={<TAViewPPage />} />
           {/* <Route path="/leave-request/:scheduleId" element={<LeaveRequestForm />} /> */}
          {/* <Route path="/volunteer"             element={<VolunteerProctoring />} /> */}
          <Route path="/leave-request/:scheduleId" element={<LeaveRequestForm />} />
        </Route>
      </Route>

      {/* Instructor Area (requires ROLE_INSTRUCTOR) */}
      <Route element={<ProtectedRoute requiredRole="ROLE_INSTRUCTOR" />}>
        <Route element={<InstructorLayout />}>
          <Route path="/instructor"                          element={<InsMainPage />} />



          <Route path="/instructor/exam-proctoring/:examId"  element={<ExamProctoringPage />} />
          <Route path="/instructor/exam-printing/:courseId"  element={<ExamProctoringPage />} />
          <Route path="/instructor/courses/:courseId/tas"    element={<CourseTAList />} />
          <Route path="/instructor/courses/:courseId/request-ta" element={<RequestTAForm />} />
          <Route path="/instructor/settings"                 element={<SettingsTA />} />
          <Route path="/instructor/workload"                 element={<ManageWorkload />} />
          <Route path="/instructor/workload/:courseId"       element={<ManageWorkload />} />

               {/* <Route path="/instructor/courses/:courseId/tas"    element={<CourseTAList />} /> */}


           <Route path="/courseTA/:courseCode" element={<CourseTAReq />} />
           <Route path="/man/:courseCode" element={<ManageWorkload />} />
           <Route path="/man/:courseCode/:taskid" element={<AssignTATask />} />
           <Route path="/examProc" element={<ExamProctorPage />} />
           <Route path="/examProc/:courseCode" element={<ExamProctorPage />} />
          {/*  <Route path="/instructor/courses/:courseId/request-ta" element={<RequestTAForm />} />
           <Route path="/instructor/settings"                 element={<SettingsTA />} /> */}
         {/*   <Route path="/instructor/workload"                 element={<ManageWorkload />} />
          <Route path="/instructor/workload/:courseId"       element={<ManageWorkload />} /> */}
        </Route>
      </Route>

      {/* Department Office Area (requires ROLE_DEPARTMENT) */}
      <Route element={<ProtectedRoute requiredRole="ROLE_DEPARTMENT_STAFF" />}>
        <Route path="/dept-office" element={<DepartmentLayout />}>
          <Route index                          element={<DepartmentOffice />} />
          <Route path="proctor"                 element={<ProctorAssignmentsPage />} />
          
          <Route path="proctor/:courseId/:mode" element={<ProctorAssignmentsPage />} />
          <Route path="course/:courseCode"        element={<DepartmentCourseDetails />} />
          <Route path="instructor/:id"         element={<DepartmentInsturctorDetails />} />

          <Route path="leave"                   element={<LeaveRequestsPage />} />


         
         {/* <Route path="/asgnTAC" element={<AssignTACourse />} />
         <Route path="/asgnTA/:courseCode" element={<SelectTACourse />} />
         <Route path="/viewE" element={<ViewAddExam />} /> */}
        </Route>
      </Route>

      {/* Dean’s Office Area (requires ROLE_DEAN) */}
      <Route element={<ProtectedRoute requiredRole="ROLE_DEAN_STAFF" />}>
        <Route path="/deans-office" element={<DeansLayout />}>
          <Route index                          element={<DeansOffice />} />
          <Route path= "proctor"                          element={<DeansProctoringPage />} />
          <Route path="assign/:courseId/:mode"  element={<DeanAssignProctors />} />
          
          
        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  </BrowserRouter>
);

export default App;

// // src/App.tsx
// import React from 'react';
// import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

// import ProtectedRoute from './Pages/ProtectedRoute';

// import Login               from './Pages/Login';
// import AdminDashboard      from './Benim/AdminDashboard';
// //import TADashboard         from './Pages/TADashboard';
// //import VolunteerProctoring from './Pages/VolunteerProctoring';
// import Notifications       from './Pages/Notifications';
// import Settings            from './Pages/Settings';


// import TALayout            from './components/TALayout';
// import InstructorLayout    from './components/InstructorLayout';
// import DepartmentLayout    from './components/DepartmentLayout';
// import DeansLayout         from './components/DeansLayout';




// import ProctorAssignmentsPage from './components/ProctorAssignmentPage';
// import LeaveRequestsPage      from './components/LeaveRequestPage';
// import DeansOffice            from './Pages/DeansOffice';
// import DeanAssignProctors     from './components/DeanAssignProctors';
// import DepartmentOffice from './Pages/DepartmentOffice';
// import DeansProctoringPage from './components/DeansProctoringPage';
// import DeansDepartmentDetailPage from './components/DeansDepartmentDetailPage';
// import  CourseDetails  from './components/DepartmentCourseDetails';
// import InstructorDetails from './components/DepartmentInsturctorDetails';
// import TAMainPage from './Pages/TAPages/TAMainPage';
// import MakeLeaveReq from './Pages/TAPages/MakeLeaveReq';
// import TAMonSchPage from './Pages/TAPages/TAMonSchPage';
// import TAViewPPage from './Pages/TAPages/TAViewPPage';
// import CourseTAReq from './Pages/InstructorPages/CourseTAReq';
// import AssignTATask from './Pages/InstructorPages/AssignTATask';
// import ExamProctorPage from './Pages/InstructorPages/ExamProctorPage';
// import AssignTACourse from './Pages/DepOfficePages/AssignTACourse';
// import SelectTACourse from './Pages/DepOfficePages/SelectTACourse';
// import ViewAddExam from './Pages/DepOfficePages/ViewAddExam';
// import ProctorLeftTA from './Pages/DeanOfficePages/ProctorLeftTA';
// import ManageWorkload from './Pages/InstructorPages/ManageWorkload';

// import InsMainPage from './Pages/InstructorPages/InsMainPage';

// const App: React.FC = () => (
//   <BrowserRouter>
//     <Routes>

//       {/* Public */}
//       <Route path="/login" element={<Login />} />
//       <Route path="/admin" element={<AdminDashboard />} />

//       {/* Shared */}
//       <Route path="/notifications" element={<Notifications />} />
//       <Route path="/settings"      element={<Settings />} />

//       {/* TA Area (requires ROLE_TA) */}
//       <Route element={<ProtectedRoute requiredRole="ROLE_TA" />}>
//         <Route element={<TALayout />}>
//           <Route path="/dashboard"             element={<TAMainPage />} />
//           <Route path="/make" element={<MakeLeaveReq />} />
//           <Route path="/mon" element={<TAMonSchPage />} />
//           <Route path="/viewP" element={<TAViewPPage />} />
//           {/* <Route path="/leave-request/:scheduleId" element={<LeaveRequestForm />} /> */}
//         </Route>
//       </Route>

//       {/* Instructor Area (requires ROLE_INSTRUCTOR) */}
//       <Route element={<ProtectedRoute requiredRole="ROLE_INSTRUCTOR" />}>
//         <Route element={<InstructorLayout />}>
//           {/* <Route path="/instructor/courses/:courseId/tas"    element={<CourseTAList />} /> */}
//           <Route path="/ins" element={<InsMainPage />} />
//           <Route path="/courseTA/:courseCode" element={<CourseTAReq />} />
//           <Route path="/man/:courseCode" element={<ManageWorkload />} />
//           <Route path="/man/:courseCode/:taskid" element={<AssignTATask />} />
//           <Route path="/examProc" element={<ExamProctorPage />} />
//           <Route path="/examProc/:courseCode" element={<ExamProctorPage />} />
//          {/*  <Route path="/instructor/courses/:courseId/request-ta" element={<RequestTAForm />} />
//           <Route path="/instructor/settings"                 element={<SettingsTA />} /> */}
//         {/*   <Route path="/instructor/workload"                 element={<ManageWorkload />} />
//           <Route path="/instructor/workload/:courseId"       element={<ManageWorkload />} /> */}
//         </Route>
//       </Route>

//       {/* Department Office Area (requires ROLE_DEPARTMENT) */}
//       <Route element={<ProtectedRoute requiredRole="ROLE_DEPARTMENT_STAFF" />}>
//         <Route path="/dept-office" element={<DepartmentLayout />}>
//           <Route index                          element={<DepartmentOffice />} />
//           <Route path="course/:courseCode"        element={<CourseDetails />} />
//           <Route path="instructor/:id"         element={<InstructorDetails />} />
//           <Route path="proctor"                 element={<ProctorAssignmentsPage />} />
//           <Route path="proctor/:courseId/:mode" element={<ProctorAssignmentsPage />} />
//           <Route path="leave"                   element={<LeaveRequestsPage />} />
//           <Route path="/asgnTAC" element={<AssignTACourse />} />
//         <Route path="/asgnTA/:courseCode" element={<SelectTACourse />} />
//         <Route path="/viewE" element={<ViewAddExam />} />
//         </Route>
//       </Route>

//       {/* Dean’s Office Area (requires ROLE_DEAN) */}
//       <Route element={<ProtectedRoute requiredRole="ROLE_DEANS_OFFICE" />}>
//         <Route path="/deans-office" element={<DeansLayout />}>
//           <Route index                          element={<DeansOffice />} />
//           <Route path= "proctor"                          element={<DeansProctoringPage />} />
//           <Route path="/deans-office/department/:dept" element={<DeansDepartmentDetailPage />}/>
//           <Route path="proctor/:courseId/:mode"  element={<DeanAssignProctors />} />
//           <Route path="/deanP" element={<DeansProctoringPage />} />
//           <Route path="/inotfac" element={<ProctorLeftTA />} />
//         </Route>
//       </Route>
      
//       {/* Fallback */}
//       <Route path="*" element={<Navigate to="/login" replace />} />
//     </Routes>
//   </BrowserRouter>
// );
// export default App;
/* 
const App: React.FC = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
     
      
     
      
      
      
      <Route path="/set" element={<Settings />} />
      
      <Route path="/adm" element={<AdminMainPage />} />
      
      <Route path="/not" element={<Not />} />
      <Route path="/viewL" element={<ViewLogs />} />
      
      <Route path="/asP" element={<AssignProctor />} />
      <Route path="/assign/:examId" element={<AssignTA />} />
      
      <Route path="/dean" element={<DeansOffice />} />
      <Route path="/de" element={<DeansDepartmentDetailPage />} />
      
      
     
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
    </Router> */
