import React, { useState } from 'react';
import api, { errorMessage } from '../services/api';
import { rentalPrice, toLocalInput, nextHour } from '../services/rental';
import { getImage } from './BikeOffer';
import { Navigate, useNavigate, useLocation } from 'react-router-dom';
import Swal from 'sweetalert2';
import './RegisterForm.css';

const ReservationForm = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { ebikeId, ebikeName, pricePerDay } = location.state || {};

    const [passportNumber, setPassportNumber] = useState('');
    const [startTime, setStartTime] = useState(() => toLocalInput(new Date()));
    const [endTime, setEndTime] = useState(() => toLocalInput(nextHour(new Date())));

    if (!sessionStorage.getItem('token')) {
        return <Navigate to="/login" replace state={{ message: 'Log in to rent a bike.' }} />;
    }
    if (!ebikeId) {
        return <Navigate to="/" replace />;
    }

    const timesInOrder = new Date(endTime) > new Date(startTime);
    const image = getImage(ebikeId);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!timesInOrder) {
            return;
        }
        try {
            await api.post('/rentals/pending', {
                ebike: { id: ebikeId },
                rentalStartTime: startTime,
                rentalEndTime: endTime,
                passportNumber: passportNumber,
            });
            Swal.fire({
                title: 'Reservation sent',
                text: `We'll review your ${ebikeName} reservation and confirm it before you ride.`,
                icon: 'success',
                iconColor: '#1a7346',
                confirmButtonText: 'Back to bikes',
                confirmButtonColor: '#123d2c',
            }).then(() => navigate('/'));
        } catch (error) {
            console.error('Error creating reservation:', error);
            Swal.fire({
                title: 'Reservation not sent',
                text: errorMessage(error, 'The server could not be reached. Try again in a moment.'),
                icon: 'error',
                confirmButtonColor: '#123d2c',
            });
        }
    };

    return (
        <div className="reserve">
            <aside className="reserve-summary">
                {image && <img src={image} alt={ebikeName} />}
                <p className="reserve-total">
                    <span>Total</span>
                    <strong aria-live="polite">${rentalPrice(startTime, endTime, pricePerDay).toFixed(2)}</strong>
                </p>
                <p className="reserve-rate">${pricePerDay} per day, charged for each hour you start.</p>
            </aside>

            <div className="reserve-form">
                <h1>Reserve {ebikeName}</h1>
                <p className="reserve-as">Reserving as {sessionStorage.getItem('userName')}</p>
                <form className="auth-form" onSubmit={handleSubmit}>
                    <label className="form-group">
                        <span>Passport number</span>
                        <input
                            type="text"
                            value={passportNumber}
                            onChange={(e) => setPassportNumber(e.target.value)}
                            required
                        />
                    </label>

                    <div className="form-row">
                        <label className="form-group">
                            <span>Start</span>
                            <input
                                type="datetime-local"
                                value={startTime}
                                onChange={(e) => setStartTime(e.target.value)}
                                required
                            />
                        </label>

                        <label className="form-group">
                            <span>End</span>
                            <input
                                type="datetime-local"
                                value={endTime}
                                onChange={(e) => setEndTime(e.target.value)}
                                required
                            />
                        </label>
                    </div>

                    {!timesInOrder && <p className="form-error" role="alert">The end time must be after the start time.</p>}

                    <button className="auth-button" type="submit">
                        Send reservation
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ReservationForm;
