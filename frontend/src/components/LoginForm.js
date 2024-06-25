import React, { useState } from 'react';
import api from "../services/api";
import { useNavigate } from "react-router-dom";
import './RegisterForm';
// import Footer from '../Footer/Footer';

const LoginForm = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await api.post('/customers/login', { email, password });
            const { userId, userRole } = response.data;
            sessionStorage.setItem('userId', userId);
            sessionStorage.setItem('userRole', userRole);
            navigate('/');
            window.location.reload();
        } catch (error) {
            console.error('Login error:', error);
        }
    };

    return (
        <div>
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Email:</label>
                    <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                </div>
                <div>
                    <label>Password:</label>
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                </div>
                <button type="submit">Login</button>
            </form>
            {/* <Footer /> */}
        </div>
    );
};

export default LoginForm;
