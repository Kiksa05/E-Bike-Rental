import React, { useState, useEffect, useCallback } from 'react';
import api, { errorMessage } from '../services/api';
import { rentalPrice, toLocalInput, pickHour } from '../services/rental';
import { getImage } from './BikeOffer';
import HourCalendar from './HourCalendar';
import { Navigate, useNavigate, useLocation } from 'react-router-dom';
import Swal from 'sweetalert2';
import './RegisterForm.css';

const ReservationForm = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { ebikeId, ebikeName, pricePerDay } = location.state || {};

    const [passportNumber, setPassportNumber] = useState('');
    const [bookings, setBookings] = useState([]);
    const [selection, setSelection] = useState(null);
    const [now] = useState(() => new Date());

    const loadBookings = useCallback(() => {
        if (!ebikeId) {
            return;
        }
        api.get(`/ebikes/${ebikeId}/booked`)
            .then((response) => setBookings(response.data.map((b) => ({ start: new Date(b.start), end: new Date(b.end) }))))
            .catch(() => {});
    }, [ebikeId]);

    useEffect(loadBookings, [loadBookings]);

    // Pre-fill from the profile; the field stays editable for this one reservation.
    useEffect(() => {
        const userId = sessionStorage.getItem('userId');
        if (!userId) {
            return;
        }
        api.get(`/customers/${userId}`)
            .then((response) => setPassportNumber((typed) => typed || response.data.passportNumber || ''))
            .catch(() => {});
    }, []);

    if (!sessionStorage.getItem('token')) {
        return <Navigate to="/login" replace state={{ message: 'Log in to rent a bike.' }} />;
    }
    if (!ebikeId) {
        return <Navigate to="/" replace />;
    }

    const image = getImage(ebikeId);
    const hours = selection ? (selection.end - selection.start) / 3600000 : 0;
    const timeFormat = { weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selection) {
            return;
        }
        try {
            await api.post('/rentals/pending', {
                ebike: { id: ebikeId },
                rentalStartTime: toLocalInput(selection.start),
                rentalEndTime: toLocalInput(selection.end),
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
            if (error.response?.status === 409) {
                setSelection(null);
                loadBookings();
            }
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
                    <strong aria-live="polite">${selection ? rentalPrice(selection.start, selection.end, pricePerDay).toFixed(2) : '0.00'}</strong>
                </p>
                <p className="reserve-rate">${pricePerDay} per day, charged per hour.</p>
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

                    <fieldset className="form-group calendar-fieldset">
                        <legend><span>When</span></legend>
                        <p className="reserve-as">Click a start hour, then an end hour. Whole hours only.</p>
                        <HourCalendar
                            bookings={bookings}
                            selection={selection}
                            now={now}
                            onPick={(slot) => setSelection(pickHour(selection, slot, bookings))} />
                    </fieldset>

                    <p className="calendar-selection" aria-live="polite">
                        {selection
                            ? `${selection.start.toLocaleString(undefined, timeFormat)} – ${selection.end.toLocaleString(undefined, timeFormat)} (${hours} ${hours === 1 ? 'hour' : 'hours'})`
                            : 'No time picked yet'}
                    </p>

                    <button className="auth-button" type="submit" disabled={!selection}>
                        Send reservation
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ReservationForm;
