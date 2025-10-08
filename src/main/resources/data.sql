
INSERT INTO court (court_number, hourly_rate_sek, active) VALUES (1, 200, 1);
INSERT INTO court (court_number, hourly_rate_sek, active) VALUES (2, 200, 1);
INSERT INTO court (court_number, hourly_rate_sek, active) VALUES (3, 200, 1);
INSERT INTO court (court_number, hourly_rate_sek, active) VALUES (4, 200, 1);
INSERT INTO court (court_number, hourly_rate_sek, active) VALUES (5, 200, 1);
INSERT INTO court (court_number, hourly_rate_sek, active) VALUES (6, 200, 1);
INSERT INTO court (court_number, hourly_rate_sek, active) VALUES (7, 200, 1);
INSERT INTO court (court_number, hourly_rate_sek, active) VALUES (8, 250, 1);


INSERT INTO customers (name, email, phone, user_id) VALUES
('Niklas Einasson',     'niklaseinasson@test.local',     '0700000001', 'niklaseinasson'),
('Benjamin Portsmouth', 'benjaminportsmouth@test.local', '0700000002', 'benjaminportsmouth'),
('Christoffer Frisk',   'christofferfrisk@test.local',   '0700000003', 'christofferfrisk');

-- ===== BOOKINGS (framtid) =====
INSERT INTO booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
VALUES (
           (SELECT id FROM court WHERE court_number = 1),
           (SELECT id FROM customers WHERE user_id = 'christofferfrisk'),
           '2026-02-15', '18:00:00', '20:00:00', 2, 400, 'ACTIVE'
       );

INSERT INTO booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
VALUES (
           (SELECT id FROM court WHERE court_number = 8),
           (SELECT id FROM customers WHERE user_id = 'benjaminportsmouth'),
           '2026-05-20', '07:00:00', '08:00:00', 2, 250, 'ACTIVE'
       );

INSERT INTO booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
VALUES (
           (SELECT id FROM court WHERE court_number = 2),
           (SELECT id FROM customers WHERE user_id = 'niklaseinasson'),
           '2026-10-01', '17:00:00', '19:00:00', 4, 400, 'ACTIVE'
       );

INSERT INTO booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
VALUES (
           (SELECT id FROM court WHERE court_number = 5),
           (SELECT id FROM customers WHERE user_id = 'christofferfrisk'),
           '2027-01-10', '12:00:00', '14:00:00', 2, 400, 'ACTIVE'
       );

-- ===== BOOKING (förfluten) =====
INSERT INTO booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
VALUES (
           (SELECT id FROM court WHERE court_number = 3),
           (SELECT id FROM customers WHERE user_id = 'benjaminportsmouth'),
           '2025-09-01', '10:00:00', '12:00:00', 2, 400, 'ACTIVE'
       );