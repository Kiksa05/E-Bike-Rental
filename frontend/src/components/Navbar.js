import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
    const navigate = useNavigate();
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    // Re-renders on every route change, so this always matches the current session.
    useLocation();
    const isLoggedIn = Boolean(sessionStorage.getItem('token'));
    const isAdmin = sessionStorage.getItem('userRole') === 'admin';

    const closeMenu = () => setIsMenuOpen(false);

    const handleLogout = () => {
        sessionStorage.clear();
        closeMenu();
        navigate('/login');
    };

    return (
        <nav className="navbar" aria-label="Main">
            <Link to="/" className="navbar-logo" onClick={closeMenu}>
                <svg viewBox="0 0 32 32" width="28" height="28" aria-hidden="true">
                    <circle cx="16" cy="16" r="16" fill="#123d2c" />
                    <path d="M18 5 9 18h6l-1 9 9-13h-6z" fill="#dff0e4" />
                </svg>
                EcoRide Rentals
            </Link>
            <button
                className="hamburger"
                aria-label="Menu"
                aria-expanded={isMenuOpen}
                onClick={() => setIsMenuOpen(!isMenuOpen)}>
                ☰
            </button>

            <ul className={isMenuOpen ? "nav-links open" : "nav-links"}>
                {!isLoggedIn ? (
                    <>
                        <li>
                            <Link to="/register" onClick={closeMenu}>Register</Link>
                        </li>
                        <li>
                            <Link to="/login" onClick={closeMenu}>Log in</Link>
                        </li>
                    </>
                ) : (
                    <>
                        <li>
                            <Link to="/" onClick={closeMenu}>Bikes</Link>
                        </li>
                        {isAdmin && (
                            <li>
                                <Link to="/admin" onClick={closeMenu}>Reservations</Link>
                            </li>
                        )}
                        <li>
                            <button className="nav-logout" onClick={handleLogout}>Log out</button>
                        </li>
                    </>
                )}
            </ul>
        </nav>
    );
};

export default Navbar;
