import './App.css';
import RegisterForm from './components/RegisterForm'; // Ensure the correct path
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <h1>Welcome to EcoRide Rentals</h1>
        <Router>
          <Routes>
            <Route path="/register" element={<RegisterForm />} />
          </Routes>
        </Router>
      </header>
    </div>
  );
}

export default App;
