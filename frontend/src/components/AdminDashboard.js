import React, { useState, useEffect } from 'react';
import api from '../services/api';
import "../components/RegisterForm.css";
import { useNavigate } from 'react-router-dom';

const AdminDashboard = () => {
    const [pendingRentals, setPendingRentals] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchPendingRentals = async () => {
            try {
                const response = await api.get('/rentals/pending');
                setPendingRentals(response.data);
            } catch (error) {
                console.error('Error fetching pending rentals:', error);
            }
        };

        fetchPendingRentals();
    }, []);

    const handleApprove = async (rentalId) => {
        try {
            await api.put(`/rentals/approve/${rentalId}`);
            setPendingRentals(pendingRentals.filter(rental => rental.id !== rentalId));
            navigate('/');
        } catch (error) {
            console.error('Error approving rental:', error);
        }
    };

    const handleReject = async (rentalId) => {
        try {
            await api.put(`/rentals/reject/${rentalId}`);
            setPendingRentals(pendingRentals.filter(rental => rental.id !== rentalId));
            navigate('/');
        } catch (error) {
            console.error('Error rejecting rental:', error);
        }
    };

    return (
        <div className="admin-dashboard">
            <h2>Pending Rentals</h2>
            <ul>
                {pendingRentals.map((rental) => (
                    <li key={rental.id}>
                        <p>Customer: {rental.customer.name} (ID: {rental.customer.id})</p>
                        <p>Ebike: {rental.ebike.model} (ID: {rental.ebike.id})</p>
                        <p>Passport Number: {rental.passportNumber}</p>
                        <p>Rental Start Time: {new Date(rental.rentalStartTime).toLocaleString()}</p>
                        <p>Rental End Time: {new Date(rental.rentalEndTime).toLocaleString()}</p>
                        <button onClick={() => handleApprove(rental.id)}>Approve</button>
                        <button onClick={() => handleReject(rental.id)}>Reject</button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default AdminDashboard;
