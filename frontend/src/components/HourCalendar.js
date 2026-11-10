import React, { useState } from 'react';
import { hourSlots, upcomingDays, isFree, nextHour } from '../services/rental';
import './HourCalendar.css';

const DAYS_AHEAD = 14;
const pad = (n) => String(n).padStart(2, '0');
const hourLabel = (date) => `${pad(date.getHours())}:00`;
const sameDay = (a, b) => a.toDateString() === b.toDateString();

// Two weeks of days, and the 24 hours of the chosen day as free, taken or past.
const HourCalendar = ({ bookings, selection, onPick, now }) => {
    const days = upcomingDays(now, DAYS_AHEAD);
    const [day, setDay] = useState(selection?.start || days[0]);

    return (
        <div className="calendar">
            <div className="calendar-days" role="group" aria-label="Day">
                {days.map((d) => (
                    <button
                        type="button"
                        key={d.toDateString()}
                        className={`calendar-day${sameDay(d, day) ? ' active' : ''}`}
                        aria-pressed={sameDay(d, day)}
                        onClick={() => setDay(d)}>
                        <span>{sameDay(d, now) ? 'Today' : d.toLocaleDateString(undefined, { weekday: 'short' })}</span>
                        <strong>{d.getDate()}</strong>
                    </button>
                ))}
            </div>

            <p className="calendar-heading">
                {day.toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long' })}
            </p>

            <div className="calendar-hours" role="group" aria-label="Hours">
                {hourSlots(day).map((slot) => {
                    const end = nextHour(slot);
                    const past = slot <= now; // an hour that has begun can no longer be booked
                    const taken = !isFree(slot, end, bookings);
                    const chosen = selection && slot >= selection.start && slot < selection.end;
                    const state = past ? 'past' : taken ? 'taken' : chosen ? 'chosen' : 'free';
                    return (
                        <button
                            type="button"
                            key={slot.getTime()}
                            className={`calendar-hour ${state}`}
                            disabled={past || taken}
                            aria-pressed={Boolean(chosen)}
                            aria-label={`${hourLabel(slot)} to ${hourLabel(end)}, ${state === 'chosen' ? 'selected' : state}`}
                            onClick={() => onPick(slot)}>
                            {hourLabel(slot)}
                            {taken && !past && <small>Taken</small>}
                        </button>
                    );
                })}
            </div>

            <ul className="calendar-legend" aria-hidden="true">
                <li><i className="free" /> Free</li>
                <li><i className="chosen" /> Your selection</li>
                <li><i className="taken" /> Taken</li>
            </ul>
        </div>
    );
};

export default HourCalendar;
