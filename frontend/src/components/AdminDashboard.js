import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import Swal from 'sweetalert2';
import api, { errorMessage } from '../services/api';
import "../components/RegisterForm.css";
import './AdminDashboard.css';

const AdminDashboard = () => {
    const [pendingRentals, setPendingRentals] = useState(null);
    const [loadFailed, setLoadFailed] = useState(false);
    const isAdmin = sessionStorage.getItem('userRole') === 'admin';

    useEffect(() => {
        if (!isAdmin) {
            return;
        }
        api.get('/rentals/pending')
            .then((response) => setPendingRentals(response.data))
            .catch((error) => {
                console.error('Error fetching pending rentals:', error);
                setLoadFailed(true);
            });
    }, [isAdmin]);

    if (!isAdmin) {
        return <Navigate to="/" replace />;
    }

    const decide = async (rental, action, done) => {
        try {
            await api.put(`/rentals/${action}/${rental.id}`);
            setPendingRentals(pendingRentals.filter((other) => other.id !== rental.id));
            Swal.fire({
                title: `${done} ${rental.customer.name}'s reservation`,
                icon: 'success',
                iconColor: '#1a7346',
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 2500,
            });
        } catch (error) {
            console.error(`Error on ${action}:`, error);
            Swal.fire({
                title: 'Reservation not updated',
                text: errorMessage(error, 'The server could not be reached. Try again in a moment.'),
                icon: 'error',
                confirmButtonColor: '#123d2c',
            });
        }
    };

    return (
        <div className="admin-dashboard">
            <h1>Reservations to review</h1>

            {loadFailed && <p className="admin-note">The reservations could not be loaded. Refresh the page to try again.</p>}
            {pendingRentals?.length === 0 && <p className="admin-note">Nothing to review. New reservations will appear here.</p>}

            {pendingRentals?.length > 0 && (
                <table className="admin-table">
                    <thead>
                        <tr>
                            <th scope="col">Customer</th>
                            <th scope="col">Bike</th>
                            <th scope="col">Passport</th>
                            <th scope="col">Start</th>
                            <th scope="col">End</th>
                            <th scope="col"><span className="visually-hidden">Decision</span></th>
                        </tr>
                    </thead>
                    <tbody>
                        {pendingRentals.map((rental) => (
                            <tr key={rental.id}>
                                <td data-label="Customer">{rental.customer.name}</td>
                                <td data-label="Bike">{rental.ebike.model}</td>
                                <td data-label="Passport">{rental.passportNumber}</td>
                                <td data-label="Start">{new Date(rental.rentalStartTime).toLocaleString()}</td>
                                <td data-label="End">{new Date(rental.rentalEndTime).toLocaleString()}</td>
                                <td className="admin-actions">
                                    <button className="auth-button" onClick={() => decide(rental, 'approve', 'Approved')}>Approve</button>
                                    <button className="auth-button secondary" onClick={() => decide(rental, 'reject', 'Rejected')}>Reject</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default AdminDashboard;
