import React, { useState, useEffect } from 'react';
import { Link, Navigate } from 'react-router-dom';
import Swal from 'sweetalert2';
import api, { errorMessage } from '../services/api';
import { rentalPrice } from '../services/rental';
import './RegisterForm.css';
import './AdminDashboard.css';

const STATUSES = ['Pending', 'Approved', 'Rejected', 'Cancelled'];

// Mirrors the server rule: pending or approved, and not started yet.
const canCancel = (rental) =>
    ['Pending', 'Approved'].includes(rental.rentalStatus) && new Date(rental.rentalStartTime) > new Date();

// Users see their own reservations; admins see everyone's and can filter by status.
const ReservationHistory = () => {
    const isLoggedIn = Boolean(sessionStorage.getItem('token'));
    const isAdmin = sessionStorage.getItem('userRole') === 'admin';
    const [rentals, setRentals] = useState(null);
    const [loadFailed, setLoadFailed] = useState(false);
    const [filter, setFilter] = useState('');

    useEffect(() => {
        if (!isLoggedIn) {
            return;
        }
        api.get(isAdmin ? '/rentals' : '/rentals/mine')
            .then((response) => setRentals(response.data))
            .catch(() => setLoadFailed(true));
    }, [isLoggedIn, isAdmin]);

    if (!isLoggedIn) {
        return <Navigate to="/login" replace state={{ message: 'Log in to see your reservations.' }} />;
    }

    const cancel = async (rental) => {
        const answer = await Swal.fire({
            title: 'Cancel this reservation?',
            text: `${rental.ebike?.model}, ${new Date(rental.rentalStartTime).toLocaleString()}. The hours become free for others.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Cancel reservation',
            cancelButtonText: 'Keep it',
            confirmButtonColor: '#a3261b',
        });
        if (!answer.isConfirmed) {
            return;
        }
        try {
            await api.put(`/rentals/${rental.id}/cancel`);
            setRentals(rentals.map((r) => (r.id === rental.id ? { ...r, rentalStatus: 'Cancelled' } : r)));
        } catch (error) {
            Swal.fire({
                title: 'Not cancelled',
                text: errorMessage(error, 'The server could not be reached. Try again in a moment.'),
                icon: 'error',
                confirmButtonColor: '#123d2c',
            });
        }
    };

    const shown = rentals?.filter((rental) => !filter || rental.rentalStatus === filter);

    return (
        <div className="admin-dashboard">
            <h1>{isAdmin ? 'All reservations' : 'My reservations'}</h1>

            {isAdmin && rentals?.length > 0 && (
                <label className="history-filter">
                    <span>Status</span>
                    <select value={filter} onChange={(e) => setFilter(e.target.value)}>
                        <option value="">All</option>
                        {STATUSES.map((status) => <option key={status} value={status}>{status}</option>)}
                    </select>
                </label>
            )}

            {loadFailed && <p className="admin-note">The reservations could not be loaded. Refresh the page to try again.</p>}
            {rentals?.length === 0 && (
                <p className="admin-note">
                    {isAdmin ? 'No reservations yet.' : <>You have no reservations yet. <Link to="/">Find a bike</Link></>}
                </p>
            )}
            {rentals?.length > 0 && shown.length === 0 && <p className="admin-note">No {filter.toLowerCase()} reservations.</p>}

            {shown?.length > 0 && (
                <table className="admin-table">
                    <thead>
                        <tr>
                            {isAdmin && <th scope="col">Customer</th>}
                            <th scope="col">Bike</th>
                            <th scope="col">Start</th>
                            <th scope="col">End</th>
                            <th scope="col">Price</th>
                            <th scope="col">Status</th>
                            <th scope="col"><span className="visually-hidden">Actions</span></th>
                        </tr>
                    </thead>
                    <tbody>
                        {shown.map((rental) => (
                            <tr key={rental.id}>
                                {isAdmin && <td data-label="Customer">{rental.customer?.name}</td>}
                                <td data-label="Bike">{rental.ebike?.model}</td>
                                <td data-label="Start">{new Date(rental.rentalStartTime).toLocaleString()}</td>
                                <td data-label="End">{new Date(rental.rentalEndTime).toLocaleString()}</td>
                                <td data-label="Price">
                                    ${rentalPrice(rental.rentalStartTime, rental.rentalEndTime, rental.ebike?.price || 0).toFixed(2)}
                                </td>
                                <td data-label="Status">
                                    <span className={`status-badge ${(rental.rentalStatus || '').toLowerCase()}`}>
                                        {rental.rentalStatus}
                                    </span>
                                </td>
                                <td className="admin-actions">
                                    {canCancel(rental) && (
                                        <button className="auth-button secondary" onClick={() => cancel(rental)}>Cancel</button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default ReservationHistory;
