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
