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

// Each click joins the hour to the selection (before or after it), filling the hours between.
// Clicking inside a longer selection ends it there; clicking the only selected hour clears it.
// If a taken hour is in the way, the click starts a new selection instead.
export const pickHour = (selection, slot, bookings) => {
    const slotEnd = nextHour(slot);
    if (!selection) {
        return { start: slot, end: slotEnd };
    }
    const single = selection.end - selection.start === HOUR_MS;
    if (single && slot.getTime() === selection.start.getTime()) {
        return null;
    }
    const start = slot < selection.start ? slot : selection.start;
    const end = slot < selection.start ? selection.end : slotEnd;
    return isFree(start, end, bookings) ? { start, end } : { start: slot, end: slotEnd };
};
