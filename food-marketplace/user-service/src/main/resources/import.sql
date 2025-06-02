-- Sample data for testing (will be loaded on startup)
INSERT INTO users (id, full_name, email, phone, role, status, address_line1, city, state, pincode, created_at) 
VALUES 
('11111111-1111-1111-1111-111111111111', 'Priya Sharma', 'priya.cook@gmail.com', '+919876543210', 'COOK', 'ACTIVE', '123 MG Road', 'Bangalore', 'Karnataka', '560001', '2024-01-15 10:00:00'),
('22222222-2222-2222-2222-222222222222', 'Rajesh Kumar', 'rajesh.customer@gmail.com', '+919876543211', 'CUSTOMER', 'ACTIVE', '456 Park Street', 'Mumbai', 'Maharashtra', '400001', '2024-01-16 11:00:00'),
('33333333-3333-3333-3333-333333333333', 'Sunita Devi', 'sunita.cook@gmail.com', '+919876543212', 'COOK', 'ACTIVE', '789 Civil Lines', 'Delhi', 'Delhi', '110001', '2024-01-17 12:00:00');

UPDATE users SET speciality_cuisine = 'South Indian', aadhaar_number = '123456789012', pan_number = 'ABCDE1234F' WHERE role = 'COOK';