import React, { useState, useEffect } from 'react';
import { Link, Navigate, useParams } from 'react-router-dom';
import Swal from 'sweetalert2';
import api, { errorMessage } from '../services/api';
import './RegisterForm.css';

const toast = (title) => Swal.fire({
    title,
    icon: 'success',
    iconColor: '#1a7346',
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 2500,
});

const failed = (title, error) => Swal.fire({
    title,
    text: errorMessage(error, 'The server could not be reached. Try again in a moment.'),
    icon: 'error',
    confirmButtonColor: '#123d2c',
});

// /profile edits your own account; /admin/users/:id lets an admin edit someone else's.
const ProfileForm = () => {
    const { id } = useParams();
    const ownId = sessionStorage.getItem('userId');
    const customerId = id || ownId;
    const isSelf = customerId === ownId;

    const [profile, setProfile] = useState(null);
    const [loadFailed, setLoadFailed] = useState(false);
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');

    useEffect(() => {
        if (!customerId) {
            return;
        }
        api.get(`/customers/${customerId}`)
            .then((response) => setProfile({
                name: response.data.name || '',
                email: response.data.email || '',
                phone: response.data.phone || '',
                passportNumber: response.data.passportNumber || '',
            }))
            .catch(() => setLoadFailed(true));
    }, [customerId]);

    if (!ownId) {
        return <Navigate to="/login" replace state={{ message: 'Log in to see your profile.' }} />;
    }

    const field = (key) => (e) => setProfile({ ...profile, [key]: e.target.value });

    const saveProfile = async (e) => {
        e.preventDefault();
        try {
            await api.put(`/customers/${customerId}`, profile);
            if (isSelf) {
                sessionStorage.setItem('userName', profile.name);
            }
            toast('Profile saved');
        } catch (error) {
            failed('Profile not saved', error);
        }
    };

    const savePassword = async (e) => {
        e.preventDefault();
        try {
            await api.put(`/customers/${customerId}/password`, { currentPassword, newPassword });
            setCurrentPassword('');
            setNewPassword('');
            toast('Password changed');
        } catch (error) {
            failed('Password not changed', error);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                {!isSelf && <div className="auth-link"><Link to="/admin/users">Back to users</Link></div>}
                <h1>{isSelf ? 'Your profile' : `Edit ${profile?.name || 'user'}`}</h1>

                {loadFailed && <p className="form-error">The profile could not be loaded. Refresh the page to try again.</p>}

                {profile && (
                    <>
                        <form className="auth-form" onSubmit={saveProfile}>
                            <label className="form-group">
                                <span>Name</span>
                                <input type="text" autoComplete="name" value={profile.name} onChange={field('name')} required />
                            </label>
                            <label className="form-group">
                                <span>Email</span>
                                <input type="email" autoComplete="email" value={profile.email} onChange={field('email')} required />
                            </label>
                            <label className="form-group">
                                <span>Phone</span>
                                <input type="tel" autoComplete="tel" value={profile.phone} onChange={field('phone')} />
                            </label>
                            <label className="form-group">
                                <span>Passport number</span>
                                <input type="text" value={profile.passportNumber} onChange={field('passportNumber')} />
                            </label>
                            <button className="auth-button" type="submit">Save profile</button>
                        </form>

                        <form className="auth-form" onSubmit={savePassword}>
                            <h2>{isSelf ? 'Change password' : 'Set a new password'}</h2>
                            {isSelf && (
                                <label className="form-group">
                                    <span>Current password</span>
                                    <input type="password" autoComplete="current-password" value={currentPassword}
                                           onChange={(e) => setCurrentPassword(e.target.value)} required />
                                </label>
                            )}
                            <label className="form-group">
                                <span>New password (at least 8 characters)</span>
                                <input type="password" autoComplete="new-password" minLength={8} value={newPassword}
                                       onChange={(e) => setNewPassword(e.target.value)} required />
                            </label>
                            <button className="auth-button" type="submit">
                                {isSelf ? 'Change password' : 'Set password'}
                            </button>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
};

export default ProfileForm;
