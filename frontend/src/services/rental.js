const HOUR_MS = 60 * 60 * 1000;

// datetime-local inputs expect local time, but toISOString() gives UTC.
export const toLocalInput = (date) =>
    new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16);

export const nextHour = (date) => new Date(date.getTime() + HOUR_MS);

// Each started hour costs 1/24 of the daily price. Returns 0 when the times are not in order.
export const rentalPrice = (start, end, pricePerDay) => {
    const ms = new Date(end) - new Date(start);
    if (!(ms > 0)) {
        return 0;
    }
    return (Math.ceil(ms / HOUR_MS) * pricePerDay) / 24;
};

// Calendar helpers. Bookings are { start: Date, end: Date }; every slot is one whole hour.

export const hourSlots = (day) =>
    Array.from({ length: 24 }, (_, hour) => new Date(day.getFullYear(), day.getMonth(), day.getDate(), hour));

export const upcomingDays = (from, count) =>
    Array.from({ length: count }, (_, i) => new Date(from.getFullYear(), from.getMonth(), from.getDate() + i));

// Two ranges overlap when each starts before the other ends, so back-to-back bookings are free.
export const isFree = (start, end, bookings) => bookings.every((b) => b.end <= start || b.start >= end);

// First click picks a start hour, the next click on a later free hour sets the end, the click after that starts over.
export const pickHour = (selection, slot, bookings) => {
    const slotEnd = nextHour(slot);
    if (selection && !selection.complete && slot >= selection.start && isFree(selection.start, slotEnd, bookings)) {
        return { start: selection.start, end: slotEnd, complete: true };
    }
    return { start: slot, end: slotEnd, complete: false };
};
