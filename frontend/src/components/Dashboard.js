import React from 'react';
import './Dashboard.css';
import Footer from './Footer';

import img1 from '../assets/1.jpg';
import img2 from '../assets/2.jpg';
import img3 from '../assets/3.jpg';
import { useNavigate } from 'react-router-dom';

const ebikes = [
    {
        id: 1,
        name: 'EcoBike Pro',
        description: 'The EcoBike Pro is perfect for long rides with a battery life of up to 70 miles.',
        imageUrl: img1,
    },
    {
        id: 2,
        name: 'City Cruiser',
        description: 'Ideal for city commuting, the City Cruiser combines style and efficiency.',
        imageUrl: img2,
    },
    {
        id: 3,
        name: 'Mountain Master',
        description: 'Conquer any terrain with the Mountain Master, built for off-road adventures.',
        imageUrl: img3,
    },
];


const Dashboard = () => {
    const navigate = useNavigate();

    const customerId = 1; // Replace with actual logged-in customer ID

    const handleRent = (bike) => {
        navigate('/reservations', {
            state: {
                ebikeId: bike.id,
                ebikeName: bike.name,
                customerId,
            },
        });
    };

    return (
        <div className="dashboard">
            <div className="ebike-list">
                {ebikes.map((ebike) => (
                    <div key={ebike.id} className="ebike-item">
                        <img src={ebike.imageUrl} alt={ebike.name} />
                        <h3>{ebike.name}</h3>
                        <p>{ebike.description}</p>
                        <button onClick={() => handleRent(ebike)}>Rent Now</button>
                    </div>
                ))}
            </div>

            <Footer />
        </div>
    );
};

export default Dashboard;
