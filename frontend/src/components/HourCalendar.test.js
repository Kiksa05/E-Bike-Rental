import React, { useState } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import HourCalendar from './HourCalendar';
import { pickHour } from '../services/rental';

const now = new Date(2099, 11, 1, 8, 30);
const bookings = [{ start: new Date(2099, 11, 1, 18), end: new Date(2099, 11, 1, 20) }];

const Harness = () => {
    const [selection, setSelection] = useState(null);
    return <HourCalendar bookings={bookings} selection={selection} now={now}
                         onPick={(slot) => setSelection(pickHour(selection, slot, bookings))} />;
};

const hour = (label) => screen.getByRole('button', { name: new RegExp(`^${label} to`) });
const selected = () => screen.getAllByRole('button', { pressed: true })
    .filter((b) => b.classList.contains('calendar-hour')).map((b) => b.textContent);

test('clicking 13, 14 and 15 selects all three hours', () => {
    render(<Harness />);
    ['13:00', '14:00', '15:00'].forEach((label) => fireEvent.click(hour(label)));
    expect(selected()).toEqual(['13:00', '14:00', '15:00']);
});

test('clicking a first and a last hour fills the ones between', () => {
    render(<Harness />);
    fireEvent.click(hour('10:00'));
    fireEvent.click(hour('13:00'));
    expect(selected()).toEqual(['10:00', '11:00', '12:00', '13:00']);
});

test('taken and past hours cannot be clicked', () => {
    render(<Harness />);
    expect(hour('18:00')).toBeDisabled();
    expect(hour('19:00')).toBeDisabled();
    expect(hour('07:00')).toBeDisabled();
    expect(hour('08:00')).toBeDisabled(); // has already started, so it cannot be booked
    expect(hour('09:00')).toBeEnabled();
    expect(hour('20:00')).toBeEnabled();
});
