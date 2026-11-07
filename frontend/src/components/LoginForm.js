import React, { useState } from 'react';
import api, { errorMessage } from "../services/api";
import { Link, useLocation, useNavigate } from "react-router-dom";
import './RegisterForm.css';
import Swal from 'sweetalert2';

const LoginForm = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const message = useLocation().state?.message;

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await api.post('/customers/login', { email, password });
            const { token, userId, userRole, userName } = response.data;
            sessionStorage.setItem('token', token);
            sessionStorage.setItem('userId', userId);
            sessionStorage.setItem('userRole', userRole);
            sessionStorage.setItem('userName', userName);

            Swal.fire({
                title: `Welcome back, ${userName}!`,
                icon: 'success',
                iconColor: '#1a7346',
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 2000,
                timerProgressBar: true
            });
            navigate('/');

        } catch (error) {
            console.error('Login error:', error);

            Swal.fire({
                title: 'Login failed',
                text: errorMessage(error, 'The server could not be reached. Try again in a moment.'),
                icon: 'error',
                confirmButtonColor: '#123d2c'
            });
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h1>Log in</h1>
                {message && <p className="auth-message" role="status">{message}</p>}
                <form className="auth-form" onSubmit={handleSubmit}>
                    <label className="form-group">
                        <span>Email</span>
                        <input
                            type="email"
                            autoComplete="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </label>

                    <label className="form-group">
                        <span>Password</span>
                        <input
                            type="password"
                            autoComplete="current-password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </label>

                    <button className="auth-button" type="submit">Log in</button>
                </form>

                <div className="auth-link">
                    Don't have an account? <Link to="/register">Register</Link>
                </div>
            </div>
        </div>
    );
};

export default LoginForm;
