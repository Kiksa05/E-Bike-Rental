import { rentalPrice, toLocalInput } from './rental';

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
