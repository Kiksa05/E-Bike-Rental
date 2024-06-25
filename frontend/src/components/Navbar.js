import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const loggedIn = sessionStorage.getItem('userId') && sessionStorage.getItem('userRole');
        setIsLoggedIn(loggedIn);

        const userRole = sessionStorage.getItem('userRole');
        setIsAdmin(userRole === 'admin');
    }, []);

    const handleLogout = () => {
        sessionStorage.removeItem('userId');
        sessionStorage.removeItem('userRole');
        setIsLoggedIn(false);
        setIsAdmin(false);
        navigate('/login');
        window.location.reload();
    };

    return (
        <nav className="navbar">
            <h1>EcoRide Rentals</h1>
            <ul>
                {!isLoggedIn ? (
                    <>
                        <li>
                            <Link to="/register">Register</Link>
                        </li>
                        <li>
                            <Link to="/login">Login</Link>
                        </li>
                    </>
                ) : (
                    <>
                        <li>
                            <Link to="/">Dashboard</Link>
                        </li>
                        {isAdmin && (
                            <li>
                                <Link to="/admin">Admin Dashboard</Link>
                            </li>
                        )}
                        <li>
                            <button onClick={handleLogout}>Logout</button>
                        </li>
                    </>
                )}
            </ul>
        </nav>
    );
};

export default Navbar;
