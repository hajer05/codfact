-- Update user passwords with correct BCrypt hash for "password123"
UPDATE users SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW' 
WHERE email IN ('admin@codingfactory.com', 'teacher@codingfactory.com', 'consultant@codingfactory.com', 'student@codingfactory.com');
