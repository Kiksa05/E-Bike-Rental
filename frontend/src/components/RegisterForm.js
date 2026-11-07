import React, { useState } from 'react';
import api, { errorMessage } from "../services/api";
import { Link, useNavigate } from "react-router-dom";
import './RegisterForm.css';
import Swal from 'sweetalert2';

const RegistrationForm = () => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [phone, setPhone] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/customers/register', { name, email, password, phone });
            Swal.fire({
                title: 'Account created',
                text: 'Log in to rent your first bike.',
                icon: 'success',
                iconColor: '#1a7346',
                confirmButtonText: 'Go to log in',
                confirmButtonColor: '#123d2c',
            }).then(() => {
                navigate('/login');
            });

        } catch (error) {
            console.error('Registration error:', error);

            Swal.fire({
                title: 'Registration failed',
                text: errorMessage(error, 'The server could not be reached. Try again in a moment.'),
                icon: 'error',
                confirmButtonColor: '#123d2c'
            });
        }

    };

    return (
        <div className="auth-container">
            <div className="auth-card">

                <h1>Create your account</h1>

                <form className="auth-form" onSubmit={handleSubmit}>

                    <label className="form-group">
                        <span>Name</span>
                        <input
                            type="text"
                            autoComplete="name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </label>

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
                            autoComplete="new-password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </label>

                    <label className="form-group">
                        <span>Phone</span>
                        <input
                            type="tel"
                            autoComplete="tel"
                            value={phone}
                            onChange={(e) => setPhone(e.target.value)}
                            required
                        />
                    </label>

                    <button className="auth-button" type="submit">
                        Create account
                    </button>

                </form>

                <div className="auth-link">
                    Already have an account? <Link to="/login">Log in</Link>
                </div>

            </div>
        </div>
    );
};

export default RegistrationForm;
