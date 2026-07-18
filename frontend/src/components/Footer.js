import React from 'react';
import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-content">
                <div className="footer-brand">
                    <h3>EcoRide Rentals</h3>
                    <p>Explore the city in an eco-friendly way with our premium e-bike rental service.</p>
                </div>

                <div className="footer-section">
                    <h4>Contact</h4>
                    <p>📧 support@ecoride.com</p>
                    <p>📞 +389 70 123 456</p>
                </div>

                <div className="footer-section">
                    <h4>Hours</h4>
                    <p>Mon - Fri: 08:00 - 20:00</p>
                    <p>Sat - Sun: 09:00 - 18:00</p>
                </div>
            </div>

            <div className="footer-bottom">
                <p>© 2026 EcoRide Rentals • Developed by Kristijan Stefanoski</p>
            </div>
        </footer>
    );
}

export default Footer;
