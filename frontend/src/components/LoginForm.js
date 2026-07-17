import React, { useState } from 'react';
import api from "../services/api";
import { useNavigate } from "react-router-dom";
import './RegisterForm';
import Swal from 'sweetalert2';
// import Footer from '../Footer/Footer';

const LoginForm = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await api.post('/customers/login', { email, password });
            const { userId, userRole, userName } = response.data;
            sessionStorage.setItem('userId', userId);
            sessionStorage.setItem('userRole', userRole);
            sessionStorage.setItem('userName', userName);

            Swal.fire({
                            title: `Welcome back, ${userName}!`,
                            icon: 'success',
                            toast: true,
                            position: 'top-end',
                            showConfirmButton: false,
                            timer: 2000,
                            timerProgressBar: true
            }).then(() => {
                            navigate('/');
                            window.location.reload();
            });

        } catch (error) {
            console.error('Login error:', error);

            Swal.fire({
                            title: 'Login Failed',
                            text: error.response?.data || 'Invalid email or password configuration.',
                            icon: 'error',
                            confirmButtonColor: '#d33'
            });
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
