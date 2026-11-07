import React from 'react';
import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-content">
                <div className="footer-brand">
                    <h2>EcoRide Rentals</h2>
                    <p>Explore the city in an eco-friendly way with our e-bike rental service.</p>
                </div>

                <div className="footer-section">
                    <h3>Contact</h3>
                    <p><a href="mailto:support@ecoride.com">support@ecoride.com</a></p>
                    <p><a href="tel:+38970123456">+389 70 123 456</a></p>
                </div>

                <div className="footer-section">
                    <h3>Hours</h3>
                    <p>Mon - Fri: 08:00 - 20:00</p>
                    <p>Sat - Sun: 09:00 - 18:00</p>
                </div>
            </div>

            <div className="footer-bottom">
                <p>© 2026 EcoRide Rentals. Developed by Kristijan Stefanoski.</p>
            </div>
        </footer>
    );
}

export default Footer;
