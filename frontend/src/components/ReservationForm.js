import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useNavigate, useLocation } from 'react-router-dom';
import './RegisterForm.css';

const ReservationForm = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { ebikeId, ebikeName, customerId, pricePerDay } = location.state || {};

    const [passportNumber, setPassportNumber] = useState('');
    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');
    const [totalPrice, setTotalPrice] = useState(0);


    useEffect(() => {
        const now = new Date();
        const defaultEndTime = new Date(now.getTime() + 60 * 60 * 1000);
        setStartTime(now.toISOString().substring(0, 16));
        setEndTime(defaultEndTime.toISOString().substring(0, 16));
    }, []);

    useEffect(() => {
        if (startTime && endTime) {
            const start = new Date(startTime);
            const end = new Date(endTime);
            const diffTime = Math.abs(end - start);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            setTotalPrice(diffDays * pricePerDay);
        }
    }, [startTime, endTime, pricePerDay]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/rentals/pending', {
                customer: { id: parseInt(customerId) }, // Ensure this is a number
                ebike: { id: parseInt(ebikeId) }, // Ensure this is a number
                rentalStartTime: startTime,
                rentalEndTime: endTime,
                passportNumber: passportNumber,
                rentalStatus: 'Pending',
            });
            alert('Reservation created successfully, please wait on admin to approve it!');
            navigate('/'); // Redirect to home or another page after successful reservation
        } catch (error) {
            console.error('Error creating reservation:', error);
        }
    };

    return (
        <div className="reservation-form">
            <h2>Reserve {ebikeName}</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Customer Id:</label>
                    <input type="text" value={customerId} readOnly />
                </div>
                <div>
                    <label>Ebike Id:</label>
                    <input type="text" value={ebikeId} readOnly />
                </div>
                <div>
                    <label>Passport Number:</label>
                    <input
                        type="text"
                        value={passportNumber}
                        onChange={(e) => setPassportNumber(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Start Time:</label>
                    <input
                        type="datetime-local"
                        value={startTime}
                        onChange={(e) => setStartTime(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>End Time:</label>
                    <input
                        type="datetime-local"
                        value={endTime}
                        onChange={(e) => setEndTime(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Total Price:</label>
                    <input type="text" value={`$${totalPrice.toFixed(2)}`} readOnly />
                </div>
                <button type="submit">Reserve</button>
            </form>
        </div>
    );
};

export default ReservationForm;
