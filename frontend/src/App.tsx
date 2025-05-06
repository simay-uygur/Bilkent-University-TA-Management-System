import React from 'react';
import { BrowserRouter ,Router, Routes, Route, Navigate} from 'react-router-dom';//BrowserRouter as Router
import "react-toastify/dist/ReactToastify.css";
import Login from './Pages/CommonPages/Login';
import TAMainPage from './Pages/TAPages/TAMainPage';
import InsMainPage from './Pages/InstructorPages/InsMainPage';
import ManageWorkload from './Pages/InstructorPages/ManageWorkload';
import Settings from './Pages/CommonPages/Settings';
import MakeLeaveReq from './Pages/TAPages/MakeLeaveReq';
import TAMonSchPage from './Pages/TAPages/TAMonSchPage';
import AdminMainPage from './Pages/AdminPages/AdminMainPage';
import TAViewPPage from './Pages/TAPages/TAViewPPage';
import Not from './Pages/CommonPages/Not';
import LeaveReqMes from './components/Messages/LeaveReqMes';
import ProtectedRoute from './ProtectedRoute';
import ViewLogs from './Pages/AdminPages/ViewLogs';
import ExamProctorPage from './Pages/InstructorPages/ExamProctorPage';
import SearchSelect from './components/SearchSelect';
import CourseInfoPanel from './Pages/InstructorPages/CourseInfoPanel';
import ExamProctorReq from './Pages/InstructorPages/ExamProctorReq';
import AssignProctor from './Pages/DepOfficePages/AssignProctor';
import AssignTA from './Pages/DepOfficePages/AssignTA';
import DeansOffice from './Pages/DeanOfficePages/DeansOffice';
import DeansProctoringPage from './Pages/DeanOfficePages/DeansProctoringPage';
import DeansDepartmentDetailPage from './Pages/DeanOfficePages/DeansDepartmentDetailPage';
import CourseTAReq from './Pages/InstructorPages/CourseTAReq';
import AssignTATask from './Pages/InstructorPages/AssignTATask';
import AssignTACourse from './Pages/DepOfficePages/AssignTACourse';
import SelectTACourse from './Pages/DepOfficePages/SelectTACourse';
import ViewAddExam from './Pages/DepOfficePages/ViewAddExam';
import ProctorLeftTA from './Pages/DeanOfficePages/ProctorLeftTA';



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
  </BrowserRouter>
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
    </Router>*/
);

export default App;