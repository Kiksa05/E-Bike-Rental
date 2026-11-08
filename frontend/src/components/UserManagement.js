import React, { useState, useEffect } from 'react';
import { Link, Navigate } from 'react-router-dom';
import Swal from 'sweetalert2';
import api, { errorMessage } from '../services/api';
import './RegisterForm.css';
import './AdminDashboard.css';

const failed = (title, error) => Swal.fire({
    title,
    text: errorMessage(error, 'The server could not be reached. Try again in a moment.'),
    icon: 'error',
    confirmButtonColor: '#123d2c',
});

const UserManagement = () => {
    const [users, setUsers] = useState(null);
    const [loadFailed, setLoadFailed] = useState(false);
    const isAdmin = sessionStorage.getItem('userRole') === 'admin';
    const ownId = sessionStorage.getItem('userId');

    useEffect(() => {
        if (!isAdmin) {
            return;
        }
        api.get('/customers')
            .then((response) => setUsers(response.data))
            .catch(() => setLoadFailed(true));
    }, [isAdmin]);

    if (!isAdmin) {
        return <Navigate to="/" replace />;
    }

    const replace = (changed) => setUsers(users.map((user) => (user.id === changed.id ? changed : user)));

    const changeRole = async (user, role) => {
        try {
            const response = await api.put(`/customers/${user.id}/role`, { role });
            replace(response.data);
        } catch (error) {
            failed('Role not changed', error);
        }
    };

    const disable = async (user) => {
        const answer = await Swal.fire({
            title: `Disable ${user.name}?`,
            text: 'They will be logged out and can no longer log in. Their reservations that have not finished yet are removed. Their data is kept.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Disable account',
            confirmButtonColor: '#a3261b',
        });
        if (!answer.isConfirmed) {
            return;
        }
        try {
            await api.delete(`/customers/${user.id}`);
            replace({ ...user, disabled: true });
        } catch (error) {
            failed('Account not disabled', error);
        }
    };

    const enable = async (user) => {
        try {
            const response = await api.put(`/customers/${user.id}/enable`);
            replace(response.data);
        } catch (error) {
            failed('Account not enabled', error);
        }
    };

    return (
        <div className="admin-dashboard">
            <h1>Users</h1>

            {loadFailed && <p className="admin-note">The users could not be loaded. Refresh the page to try again.</p>}

            {users?.length > 0 && (
                <table className="admin-table">
                    <thead>
                        <tr>
                            <th scope="col">Name</th>
                            <th scope="col">Email</th>
                            <th scope="col">Role</th>
                            <th scope="col">Status</th>
                            <th scope="col"><span className="visually-hidden">Actions</span></th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => {
                            const isSelf = String(user.id) === ownId;
                            return (
                                <tr key={user.id}>
                                    <td data-label="Name">{user.name}</td>
                                    <td data-label="Email">{user.email}</td>
                                    <td data-label="Role">
                                        <select
                                            aria-label={`Role for ${user.name}`}
                                            value={user.role || 'user'}
                                            disabled={isSelf}
                                            title={isSelf ? 'You cannot change your own role' : undefined}
                                            onChange={(e) => changeRole(user, e.target.value)}>
                                            <option value="user">User</option>
                                            <option value="admin">Admin</option>
                                        </select>
                                    </td>
                                    <td data-label="Status">{user.disabled ? 'Disabled' : 'Active'}</td>
                                    <td className="admin-actions">
                                        <Link className="auth-button secondary" to={isSelf ? '/profile' : `/admin/users/${user.id}`}>Edit</Link>
                                        {!isSelf && (user.disabled
                                            ? <button className="auth-button" onClick={() => enable(user)}>Enable</button>
                                            : <button className="auth-button secondary" onClick={() => disable(user)}>Disable</button>)}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default UserManagement;
