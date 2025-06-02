-- Sample dishes for testing
INSERT INTO dishes (id, name, description, cook_id, cuisine_type, category, price, dietary_type, spice_level, preparation_time_minutes, daily_capacity, available_quantity, average_rating, total_reviews, total_orders, status, created_at) 
VALUES 
('44444444-4444-4444-4444-444444444444', 'Butter Chicken', 'Creamy tomato-based curry with tender chicken pieces', '11111111-1111-1111-1111-111111111111', 'NORTH_INDIAN', 'LUNCH', 250.00, 'NON_VEGETARIAN', 'MEDIUM', 45, 10, 8, 4.5, 25, 15, 'ACTIVE', '2024-01-15 10:00:00'),
('55555555-5555-5555-5555-555555555555', 'Masala Dosa', 'Crispy south Indian crepe with spiced potato filling', '33333333-3333-3333-3333-333333333333', 'SOUTH_INDIAN', 'BREAKFAST', 120.00, 'VEGETARIAN', 'MILD', 20, 15, 12, 4.7, 40, 30, 'ACTIVE', '2024-01-16 11:00:00'),
('66666666-6666-6666-6666-666666666666', 'Chole Bhature', 'Spicy chickpea curry with deep-fried bread', '11111111-1111-1111-1111-111111111111', 'PUNJABI', 'LUNCH', 180.00, 'VEGETARIAN', 'SPICY', 30, 8, 5, 4.3, 18, 12, 'ACTIVE', '2024-01-17 12:00:00');

-- Sample dish ingredients
INSERT INTO dish_ingredients (dish_id, ingredient) VALUES
('44444444-4444-4444-4444-444444444444', 'Chicken'),
('44444444-4444-4444-4444-444444444444', 'Tomatoes'),
('44444444-4444-4444-4444-444444444444', 'Cream'),
('44444444-4444-4444-4444-444444444444', 'Spices'),
('55555555-5555-5555-5555-555555555555', 'Rice'),
('55555555-5555-5555-5555-555555555555', 'Lentils'),
('55555555-5555-5555-5555-555555555555', 'Potatoes'),
('66666666-6666-6666-6666-666666666666', 'Chickpeas'),
('66666666-6666-6666-6666-666666666666', 'Flour'),
('66666666-6666-6666-6666-666666666666', 'Onions');
