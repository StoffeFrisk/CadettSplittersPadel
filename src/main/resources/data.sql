
INSERT INTO padel_court (court_number, hourly_rate_sek, active) VALUES
                                                                    (1,200,1),(2,200,1),(3,200,1),(4,200,1),
                                                                    (5,200,1),(6,200,1),(7,200,1),(8,250,1);

-- CUSTOMERS
INSERT INTO padel_customer (name, email, phone, user_id) VALUES
                                                             ('Niklas Einasson','niklaseinasson@test.local','0700000001','niklaseinasson'),
                                                             ('Benjamin Portsmouth','benjaminportsmouth@test.local','0700000002','benjaminportsmouth'),
                                                             ('Christoffer Frisk','christofferfrisk@test.local','0700000003','christofferfrisk');

-- BOOKINGS (framtid)
INSERT INTO padel_booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
SELECT c.id, cu.id, '2026-02-15','18:00:00','20:00:00',2,400,'ACTIVE'
FROM padel_court c JOIN padel_customer cu ON cu.user_id='christofferfrisk'
WHERE c.court_number=1;

INSERT INTO padel_booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
SELECT c.id, cu.id, '2026-05-20','07:00:00','08:00:00',2,250,'ACTIVE'
FROM padel_court c JOIN padel_customer cu ON cu.user_id='benjaminportsmouth'
WHERE c.court_number=8;

INSERT INTO padel_booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
SELECT c.id, cu.id, '2026-10-01','17:00:00','19:00:00',4,400,'ACTIVE'
FROM padel_court c JOIN padel_customer cu ON cu.user_id='niklaseinasson'
WHERE c.court_number=2;

INSERT INTO padel_booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
SELECT c.id, cu.id, '2027-01-10','12:00:00','14:00:00',2,400,'ACTIVE'
FROM padel_court c JOIN padel_customer cu ON cu.user_id='christofferfrisk'
WHERE c.court_number=5;

-- BOOKING (förfluten)
INSERT INTO padel_booking (court_id, customer_id, booking_date, start_time, end_time, number_of_players, price_sek, status)
SELECT c.id, cu.id, '2025-09-01','10:00:00','12:00:00',2,400,'ACTIVE'
FROM padel_court c JOIN padel_customer cu ON cu.user_id='benjaminportsmouth'
WHERE c.court_number=3;