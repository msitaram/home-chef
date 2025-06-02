-- Sample orders for testing
INSERT INTO orders (id, customer_id, cook_id, status, total_amount, delivery_fee, tax_amount, delivery_type, delivery_address, delivery_city, delivery_pincode, payment_method, payment_status, estimated_delivery_time, created_at) 
VALUES 
('77777777-7777-7777-7777-777777777777', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'CONFIRMED', 315.00, 25.00, 15.75, 'DELIVERY', '456 Brigade Road', 'Bangalore', '560001', 'COD', 'PENDING', '2024-01-18 14:30:00', '2024-01-18 13:00:00'),
('88888888-8888-8888-8888-888888888888', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'DELIVERED', 126.00, 0.00, 6.00, 'PICKUP', null, null, null, 'COD', 'COMPLETED', '2024-01-17 12:30:00', '2024-01-17 12:00:00');

-- Sample order items
INSERT INTO order_items (id, order_id, dish_id, dish_name, unit_price, quantity, total_price) VALUES
('99999999-9999-9999-9999-999999999999', '77777777-7777-7777-7777-777777777777', '44444444-4444-4444-4444-444444444444', 'Butter Chicken', 250.00, 1, 250.00),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '77777777-7777-7777-7777-777777777777', '66666666-6666-6666-6666-666666666666', 'Chole Bhature', 180.00, 1, 180.00),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '88888888-8888-8888-8888-888888888888', '55555555-5555-5555-5555-555555555555', 'Masala Dosa', 120.00, 1, 120.00);
