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

test('first click picks one hour, second click sets the end, third starts over', () => {
    const first = pickHour(null, at(9), booked);
    expect(first).toEqual({ start: at(9), end: at(10), complete: false });

    const second = pickHour(first, at(11), booked);
    expect(second).toEqual({ start: at(9), end: at(12), complete: true });

    expect(pickHour(second, at(16), booked)).toEqual({ start: at(16), end: at(17), complete: false });
});

test('the end can be on a later day', () => {
    const first = pickHour(null, at(20), booked);
    expect(pickHour(first, at(8, 2), booked)).toEqual({ start: at(20), end: at(9, 2), complete: true });
});

test('an end before the start, or across a booking, starts over instead', () => {
    const first = pickHour(null, at(11), booked);
    expect(pickHour(first, at(10), booked)).toEqual({ start: at(10), end: at(11), complete: false });
    expect(pickHour(first, at(16), booked)).toEqual({ start: at(16), end: at(17), complete: false });
});
