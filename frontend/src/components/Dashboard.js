import React from 'react';
import './Dashboard.css';
import BikeOffer from './BikeOffer';
import heroBike from '../assets/1.jpg';

const Dashboard = () => {

    return (
        <div className="dashboard">
            <section className="hero">
                <div className="hero-text">
                    <h1>Rent an e-bike and leave the car at home.</h1>
                    <p>Choose a bike, pick your times and reserve it online. We confirm every reservation before you ride.</p>
                    <a className="auth-button hero-cta" href="#bikes">See the bikes</a>
                </div>
                <div className="hero-photo">
                    <img src={heroBike} alt="" />
                </div>
            </section>

            <BikeOffer />

            <section className="find-us">
                <div className="find-us-text">
                    <h2>Where to find us</h2>
                    <p>Pick up your bike on Ptujska cesta in Maribor.</p>
                    <a
                        href="https://www.google.com/maps/search/?api=1&query=Ptujska+cesta+Maribor"
                        target="_blank"
                        rel="noreferrer">
                        Get directions
                    </a>
                </div>
                <iframe
                    className="find-us-map"
                    src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2745.0068511732375!2d15.666212976809604!3d46.52776626185047!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x476f79fda6c9cc37%3A0x8b3f6d26b442e8f7!2sPtujska%20cesta%2C%20Maribor!5e0!3m2!1sen!2ssi!4v1683713064679!5m2!1sen!2ssi"
                    title="Map of Ptujska cesta, Maribor"
                    allowFullScreen=""
                    loading="lazy"
                    referrerPolicy="no-referrer-when-downgrade"
                ></iframe>
            </section>
        </div>
    );
};

export default Dashboard;
