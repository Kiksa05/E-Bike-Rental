import React from 'react';
import './Dashboard.css';
import Footer from './Footer';
import Reviews from './Reviews';
import BikeOffer from './BikeOffer';

const Dashboard = () => {

    return (
        <div className="dashboard">

            <BikeOffer />

            <h3>Where can you find us?</h3>
            <div className="maps" style={{ width: '80%', maxWidth: '800px', margin: '20px auto', borderRadius: '25px', overflow: 'hidden' }}>
                <iframe
                    src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2745.0068511732375!2d15.666212976809604!3d46.52776626185047!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x476f79fda6c9cc37%3A0x8b3f6d26b442e8f7!2sPtujska%20cesta%2C%20Maribor!5e0!3m2!1sen!2ssi!4v1683713064679!5m2!1sen!2ssi"
                    title="Google Maps"
                    width="100%"
                    height="500"
                    style={{ borderRadius: '25px', border: 'none' }}
                    allowFullScreen=""
                    loading="lazy"
                    referrerPolicy="no-referrer-when-downgrade"
                ></iframe></div>
            <Reviews />
            <Footer />
        </div>
    );
};

export default Dashboard;
