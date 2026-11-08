import './App.css';
import Dashboard from './components/Dashboard';
import LoginForm from './components/LoginForm';
import RegisterForm from './components/RegisterForm'; // Ensure the correct path
import ReservationForm from './components/ReservationForm';
import Footer from './components/Footer';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import AdminDashboard from './components/AdminDashboard';
import ProfileForm from './components/ProfileForm';
import UserManagement from './components/UserManagement';

function App() {
  return (
    <div className="App">
      <Router>
        <Navbar />
        <main className="App-main">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/register" element={<RegisterForm />} />
            <Route path="/login" element={<LoginForm />} />
            <Route path="/reserve/:id" element={<ReservationForm />} />
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<UserManagement />} />
            <Route path="/admin/users/:id" element={<ProfileForm />} />
            <Route path="/profile" element={<ProfileForm />} />
          </Routes>
        </main>
      </Router>

      <Footer />
    </div>
  );
}

export default App;
