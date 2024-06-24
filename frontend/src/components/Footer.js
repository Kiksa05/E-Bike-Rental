import React from 'react';
import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-content">
                <h3>EcoRide Rentals</h3>
                <p>Experience the best e-bike rentals with EcoRide. Explore, ride, and enjoy the journey!</p>
                {/* <ul className="socials">
                    <li><a href="#"><i className="fa fa-facebook"></i></a></li>
                    <li><a href="#"><i className="fa fa-twitter"></i></a></li>
                    <li><a href="#"><i className="fa fa-instagram"></i></a></li>
                    <li><a href="#"><i className="fa fa-linkedin"></i></a></li>
                </ul> */}
            </div>
            <div className="footer-bottom">
                <p>&copy; 2024 EcoRide Rentals | Kristijan Stefanoski</p>
            </div>
        </footer>
    );
}

export default Footer;
