import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);
    const navigate = useNavigate();
    const [isMenuOpen, setIsMenuOpen] = useState(false);

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
        setIsMenuOpen(false);

        navigate('/login');
        window.location.reload();
    };

    return (
        <nav className="navbar">
            <h1><Link to="/">EcoRide Rentals</Link></h1>
            <button
                className='hamburger'
                onClick={() => setIsMenuOpen(!isMenuOpen)}>
                ☰
            </button>


            <ul className={isMenuOpen ? "nav-links open" : "nav-links"}>
                {!isLoggedIn ? (
                    <>
                        <li>
                            <Link to="/register" onClick={() => setIsMenuOpen(false)}>Register</Link>
                        </li>
                        <li>
                            <Link to="/login" onClick={() => setIsMenuOpen(false)}>Login</Link>
                        </li>
                    </>
                ) : (
                    <>
                        <li>
                            <Link to="/" onClick={() => setIsMenuOpen(false)}>Dashboard</Link>
                        </li>
                        {isAdmin && (
                            <li>
                                <Link to="/admin" onClick={() => setIsMenuOpen(false)}>Admin Dashboard</Link>
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
