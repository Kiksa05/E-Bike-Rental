import React, { useState, useEffect } from 'react';
import { Link, Navigate } from 'react-router-dom';
import api from '../services/api';
import { rentalPrice } from '../services/rental';
import './RegisterForm.css';
import './AdminDashboard.css';

const STATUSES = ['Pending', 'Approved', 'Rejected'];

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
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default ReservationHistory;
