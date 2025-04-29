// src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import Login            from './Pages/Login';
import AdminDashboard   from './components/AdminDashboard';
import TADashboard      from './Pages/TADashboard';
import VolunteerProctoring from './Pages/VolunteerProctoring';
import Notifications    from './Pages/Notifications';
import Settings         from './Pages/Settings';
import LeaveRequestForm from './components/LeaveRequestForm';

import Layout           from './components/TALayout';
import InstructorLayout from './components/InstructorLayout';
import DepartmentLayout from './components/DepartmentLayout';
import DeansLayout       from './components/DeansLayout';

import InsMainPage          from './Pages/InsMainPage';
import ExamProctoringPage   from './components/ExamProctoringPage';
import CourseTAList         from './components/CourseTAList';
import RequestTAForm        from './components/RequestTAForm';
import SettingsTA           from './components/SettingsTA';
import ManageWorkload       from './components/ManagaWorkload';

import ProctorAssignmentsPage from './components/ProctorAssignmentPage';
import ProctorAssignmentPage  from './components/ProctorAssignmentPage';
import LeaveRequestsPage      from './components/LeaveRequestPage';
import DepartmentOffice       from './Pages/DepartmentOffice';
import DeansOffice            from './Pages/DeansOffice';

const App: React.FC = () => (
  <BrowserRouter>
    <Routes>

      {/* Public */}
      <Route path="/login" element={<Login />} />
      <Route path="/admin" element={<AdminDashboard />} />

      {/* Shared */}
      <Route path="/notifications" element={<Notifications />} />
      <Route path="/settings"      element={<Settings />} />

      {/* Instructor Area */}
      <Route element={<InstructorLayout />}>
        <Route path="/instructor"                            element={<InsMainPage />} />
        <Route path="/instructor/exam-proctoring/:examId"    element={<ExamProctoringPage />} />
        <Route path="/instructor/exam-printing/:courseId" element={<ExamProctoringPage />} />
        <Route path="/instructor/courses/:courseId/tas"      element={<CourseTAList />} />
        <Route path="/instructor/courses/:courseId/request-ta" element={<RequestTAForm />} />
        <Route path="/instructor/settings"                   element={<SettingsTA />} />
        <Route path="/instructor/workload"                   element={<ManageWorkload />} />
        <Route path="/instructor/workload/:courseId"         element={<ManageWorkload />} />
      </Route>

      {/* TA Area */}
      <Route element={<Layout />}>
        <Route path="/dashboard"             element={<TADashboard />} />
        <Route path="/volunteer"             element={<VolunteerProctoring />} />
        <Route path="/leave-request/:scheduleId" element={<LeaveRequestForm />} />
      </Route>

      {/* Department Office Area */}
      <Route path="/dept-office" element={<DepartmentLayout />}>
    <Route index                           element={<Navigate to="proctor" replace />} />
    <Route path="proctor"                  element={<ProctorAssignmentsPage />} />
    <Route path="proctor/:courseId/:mode"  element={<ProctorAssignmentsPage />} />
    <Route path="leave"                    element={<LeaveRequestsPage />} />
  </Route>

      {/* Dean Office Area */}
      <Route path="/deans-office" element={<DeansLayout />}>
      
    <Route index element={<DeansOffice />} />
    {/* add nested dean pages here */}
  </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  </BrowserRouter>
);

export default App;
