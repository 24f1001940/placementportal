import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import Shell from './layouts/Shell.jsx'
import AdminDashboard from './pages/AdminDashboard.jsx'
import ApplicationHistory from './pages/ApplicationHistory.jsx'
import CompanyDashboard from './pages/CompanyDashboard.jsx'
import DriveDetails from './pages/DriveDetails.jsx'
import Login from './pages/Login.jsx'
import Profile from './pages/Profile.jsx'
import Register from './pages/Register.jsx'
import StudentDashboard from './pages/StudentDashboard.jsx'
import { useAuth } from './context/AuthContext.jsx'

function DashboardRedirect() {
  const { user } = useAuth()
  if (user?.role === 'ADMIN') return <AdminDashboard />
  if (user?.role === 'COMPANY') return <CompanyDashboard />
  return <StudentDashboard />
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Shell />}>
          <Route path="/" element={<DashboardRedirect />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/applications" element={<ApplicationHistory />} />
          <Route path="/drives/:id" element={<DriveDetails />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
