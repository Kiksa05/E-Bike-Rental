import { rentalPrice, toLocalInput, hourSlots, isFree, pickHour } from './rental';

test('one hour costs 1/24 of the daily price', () => {
    expect(rentalPrice('2026-12-01T10:00', '2026-12-01T11:00', 24)).toBe(1);
});

test('a full day costs the daily price', () => {
    expect(rentalPrice('2026-12-01T10:00', '2026-12-02T10:00', 15)).toBe(15);
});

test('a started hour is charged in full', () => {
    expect(rentalPrice('2026-12-01T10:00', '2026-12-01T11:01', 24)).toBe(2);
});

test('times in the wrong order or missing cost nothing', () => {
    expect(rentalPrice('2026-12-01T11:00', '2026-12-01T10:00', 24)).toBe(0);
    expect(rentalPrice('2026-12-01T10:00', '2026-12-01T10:00', 24)).toBe(0);
    expect(rentalPrice('', '', 24)).toBe(0);
});

test('input values use local time', () => {
    const date = new Date(2026, 11, 1, 9, 5);
    expect(toLocalInput(date)).toBe('2026-12-01T09:05');
});

// Calendar: Dec 1 2099, local time.
const at = (hour, day = 1) => new Date(2099, 11, day, hour);
const booked = [{ start: at(13), end: at(15) }]; // 13:00-15:00 taken

test('a day has 24 one-hour slots starting at midnight', () => {
    const slots = hourSlots(at(0));
    expect(slots).toHaveLength(24);
    expect(slots[0]).toEqual(at(0));
    expect(slots[23]).toEqual(at(23));
});

test('a range is free unless it overlaps a booking; touching is fine', () => {
    expect(isFree(at(11), at(13), booked)).toBe(true);
    expect(isFree(at(15), at(16), booked)).toBe(true);
    expect(isFree(at(12), at(14), booked)).toBe(false);
    expect(isFree(at(14), at(15), booked)).toBe(false);
    expect(isFree(at(10), at(17), booked)).toBe(false);
});

test('a booking with minutes blocks every hour it touches', () => {
    const odd = [{ start: new Date(2099, 11, 1, 13, 47), end: new Date(2099, 11, 1, 15, 47) }];
    expect(isFree(at(13), at(14), odd)).toBe(false);
    expect(isFree(at(15), at(16), odd)).toBe(false);
    expect(isFree(at(12), at(13), odd)).toBe(true);
    expect(isFree(at(16), at(17), odd)).toBe(true);
});

const pickAll = (hours, bookings = booked) => hours.reduce((sel, slot) => pickHour(sel, slot, bookings), null);

test('the first click picks one hour', () => {
    expect(pickAll([at(9)])).toEqual({ start: at(9), end: at(10) });
});

test('clicking hours one by one keeps adding them', () => {
    expect(pickAll([at(9), at(10), at(11), at(12)])).toEqual({ start: at(9), end: at(13) });
});

test('clicking a later hour fills in the hours between', () => {
    expect(pickAll([at(9), at(12)])).toEqual({ start: at(9), end: at(13) });
});

test('clicking an earlier hour extends the selection backwards', () => {
    expect(pickAll([at(11), at(9)])).toEqual({ start: at(9), end: at(12) });
});

test('the end can be on a later day', () => {
    expect(pickAll([at(20), at(8, 2)])).toEqual({ start: at(20), end: at(9, 2) });
});

test('clicking inside a longer selection shortens it to end there', () => {
    expect(pickAll([at(8), at(11), at(9)])).toEqual({ start: at(8), end: at(10) });
});

test('clicking the only selected hour again clears it', () => {
    expect(pickAll([at(9), at(9)])).toBeNull();
});

test('a range across a taken hour starts a new selection instead', () => {
    // 13:00-15:00 is taken
    expect(pickAll([at(11), at(16)])).toEqual({ start: at(16), end: at(17) });
    expect(pickAll([at(16), at(11)])).toEqual({ start: at(11), end: at(12) });
});
