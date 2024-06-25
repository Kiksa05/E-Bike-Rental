import './App.css';
import Dashboard from './components/Dashboard';
import LoginForm from './components/LoginForm';
import RegisterForm from './components/RegisterForm'; // Ensure the correct path
import ReservationForm from './components/ReservationForm';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import AdminDashboard from './components/AdminDashboard';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <Router>
          <Navbar />
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/register" element={<RegisterForm />} />
            <Route path="/login" element={<LoginForm />} />
            <Route path="/reserve/:id" element={<ReservationForm />} />
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </Router>
      </header>
    </div>
  );
}

export default App;
