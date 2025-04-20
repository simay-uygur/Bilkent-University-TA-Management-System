import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage         from './components/LoginPage';
import Login  from './Pages/Login';
import AdminDashboard    from './components/AdminDashboard';
import TADashboard       from './Pages/TADashboard';
import LeaveRequestForm  from './components/LeaveRequestForm';
import VolunteerProctoring from './Pages/VolunteerProctoring';
import Layout            from './components/Layout';
import InsMainPage       from './Pages/InsMainPage';
import CourseTAList      from './components/CourseTAList';
import InstructorLayout  from './components/InstructorLayout';
import RequestTAForm     from './components/RequestTAForm';
import SettingsTA        from './components/SettingsTA';
const App: React.FC = () => (
  <BrowserRouter>
    <Routes>
      {/* Public */}
      <Route path="/login" element={<Login />} />
      <Route path="/admin" element={<AdminDashboard />} />

      
   {/* Instructor routes, all share InsNavBar */}
     <Route element={<InstructorLayout />}>
       <Route path="/instructor" element={<InsMainPage />} />
       <Route path="/courses/:courseId/tas" element={<CourseTAList />} />
       <Route path="/courses/:courseId/request-ta" element={<RequestTAForm />} />
       <Route path="/instructor/settings" element={<SettingsTA />} />
     </Route>
      {/* Routes with the instructor nav bar */}
      {/* Routes with the TA nav bar */}
      <Route element={<Layout />}>
        <Route path="/dashboard" element={<TADashboard />} />
        
        <Route path="/volunteer" element={<VolunteerProctoring />} />
        <Route path="/leave-request/:scheduleId" element={<LeaveRequestForm />} />
        <Route path="/notifications" element={<Navigate to="/dashboard" />} />
        
        {/* you can add instructor/admin protected routes here */}
      </Route>

      {/* Fallback: redirect all unknown paths to login or dashboard */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  </BrowserRouter>
);

export default App;
