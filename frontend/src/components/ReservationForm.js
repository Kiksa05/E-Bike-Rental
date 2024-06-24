import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useNavigate, useLocation } from 'react-router-dom';
import './RegisterForm.css';

const ReservationForm = () => {
    const [customerId, setCustomerId] = useState('');
    const [ebikeId, setEbikeId] = useState('');
    const [ebikeName, setEbikeName] = useState('');
    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        if (location.state) {
            const { ebikeId, ebikeName, customerId } = location.state;
            setEbikeId(ebikeId);
            setEbikeName(ebikeName);
            setCustomerId(customerId);

            const now = new Date();
            const oneHourLater = new Date(now.getTime() + 60 * 60 * 1000);

            setStartTime(now.toISOString().slice(0, 16));
            setEndTime(oneHourLater.toISOString().slice(0, 16));
        }
    }, [location.state]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await api.post('/reservations', {
                customer: { id: customerId },
                ebike: { id: ebikeId },
                startTime,
                endTime,
            });
            console.log('Reservation successful:', response.data);
            navigate('/');
        } catch (error) {
            console.error('Error creating reservation:', error);
        }
    };

    return (
        <div className="reservation-form">
            <h2>Make a Reservation</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Customer ID:</label>
                    <input
                        type="text"
                        value={customerId}
                        readOnly
                    />
                </div>
                <div>
                    <label>Ebike ID:</label>
                    <input
                        type="text"
                        value={ebikeId}
                        readOnly
                    />
                </div>
                <div>
                    <label>Ebike Name:</label>
                    <input
                        type="text"
                        value={ebikeName}
                        readOnly
                    />
                </div>
                <div>
                    <label>Start Time:</label>
                    <input
                        type="datetime-local"
                        value={startTime}
                        onChange={(e) => setStartTime(e.target.value)}
                    />
                </div>
                <div>
                    <label>End Time:</label>
                    <input
                        type="datetime-local"
                        value={endTime}
                        onChange={(e) => setEndTime(e.target.value)}
                    />
                </div>
                <button type="submit">Make Reservation</button>
            </form>
        </div>
    );
};

export default ReservationForm;
