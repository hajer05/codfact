-- Insert seed users with BCrypt encrypted passwords (password: "password123")
INSERT INTO users (email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at) VALUES
('admin@codingfactory.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW', 'Admin', 'User', true, true, true, true, CURRENT_TIMESTAMP),
('teacher@codingfactory.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW', 'John', 'Teacher', true, true, true, true, CURRENT_TIMESTAMP),
('consultant@codingfactory.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW', 'Jane', 'Consultant', true, true, true, true, CURRENT_TIMESTAMP),
('student@codingfactory.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW', 'Alice', 'Student', true, true, true, true, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Admin user gets ADMIN role
(2, 2), -- Teacher user gets TEACHER role
(3, 4), -- Consultant user gets CONSULTANT role
(4, 3) -- Student user gets ETUDIANT role
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Insert sample courses
INSERT INTO courses (title, description, short_description, price, level, status, category, language, teacher_id, created_at) VALUES
('Complete Web Development Bootcamp', 
 'Learn web development from scratch with HTML, CSS, JavaScript, React, Node.js, and more. This comprehensive course covers everything you need to become a full-stack web developer.',
 'Master web development with this complete bootcamp covering frontend and backend technologies.',
 299.99, 'BEGINNER', 'PUBLISHED', 'Web Development', 'English', 2, CURRENT_TIMESTAMP),

('Advanced JavaScript Concepts', 
 'Deep dive into advanced JavaScript concepts including closures, prototypes, async/await, and modern ES6+ features. Perfect for developers looking to master JavaScript.',
 'Master advanced JavaScript concepts and become a JavaScript expert.',
 199.99, 'ADVANCED', 'PUBLISHED', 'Web Development', 'English', 2, CURRENT_TIMESTAMP),

('Introduction to Data Science', 
 'Learn the fundamentals of data science including Python, pandas, numpy, matplotlib, and basic machine learning concepts. No prior experience required.',
 'Get started with data science using Python and popular libraries.',
 249.99, 'BEGINNER', 'DRAFT', 'Data Science', 'French', 2, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample modules for the first course
INSERT INTO modules (title, description, order_index, course_id, created_at) VALUES
('HTML Fundamentals', 'Learn the basics of HTML and semantic markup', 1, 1, CURRENT_TIMESTAMP),
('CSS Styling', 'Master CSS for beautiful web designs', 2, 1, CURRENT_TIMESTAMP),
('JavaScript Basics', 'Introduction to JavaScript programming', 3, 1, CURRENT_TIMESTAMP),
('React Framework', 'Build modern web applications with React', 4, 1, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample lessons for the first module
INSERT INTO lessons (title, description, order_index, type, content, is_free, module_id, created_at) VALUES
('What is HTML?', 'Introduction to HTML and its role in web development', 1, 'TEXT', 'HTML (HyperText Markup Language) is the standard markup language for creating web pages...', true, 1, CURRENT_TIMESTAMP),
('HTML Document Structure', 'Learn about the basic structure of an HTML document', 2, 'VIDEO', 'Understanding the DOCTYPE, html, head, and body elements', false, 1, CURRENT_TIMESTAMP),
('Common HTML Elements', 'Explore the most commonly used HTML elements', 3, 'VIDEO', 'Learn about headings, paragraphs, links, images, and lists', false, 1, CURRENT_TIMESTAMP),
('HTML Forms', 'Create interactive forms with HTML', 4, 'VIDEO', 'Building forms with input fields, buttons, and validation', false, 1, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample enrollments
INSERT INTO enrollments (student_id, course_id, progress, status, enrolled_at) VALUES
(4, 1, 25.0, 'ACTIVE', CURRENT_TIMESTAMP),
(4, 2, 0.0, 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample lesson progress
INSERT INTO lesson_progress (enrollment_id, lesson_id, completed, completed_at, watched_duration, last_accessed_at) VALUES
(1, 1, true, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP),
(1, 2, false, null, 120, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
