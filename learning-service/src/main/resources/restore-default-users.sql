-- Script pour restaurer les utilisateurs par défaut après un TRUNCATE
-- Exécuter dans le conteneur Docker: docker exec -i learning-db psql -U postgres -d learning_db < restore-default-users.sql

-- Réinitialiser les séquences pour que les IDs commencent à 1
ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS roles_id_seq RESTART WITH 1;

-- Insérer les rôles (s'ils n'existent pas déjà)
INSERT INTO roles (name) VALUES
('ADMIN'),
('TEACHER'),
('ETUDIANT'),
('CONSULTANT')
ON CONFLICT (name) DO NOTHING;

-- Insérer les utilisateurs avec BCrypt encrypted passwords (password: "password123")
INSERT INTO users (id, email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at) VALUES
(1, 'admin@codingfactory.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW', 'Admin', 'User', true, true, true, true, CURRENT_TIMESTAMP),
(2, 'teacher@codingfactory.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW', 'John', 'Teacher', true, true, true, true, CURRENT_TIMESTAMP),
(3, 'consultant@codingfactory.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW', 'Jane', 'Consultant', true, true, true, true, CURRENT_TIMESTAMP),
(4, 'student@codingfactory.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNvtxJMKUqKnHdwlW', 'Alice', 'Student', true, true, true, true, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    enabled = EXCLUDED.enabled;

-- Assigner les rôles aux utilisateurs
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Admin user gets ADMIN role
(2, 2), -- Teacher user gets TEACHER role
(3, 4), -- Consultant user gets CONSULTANT role
(4, 3) -- Student user gets ETUDIANT role
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Mettre à jour la séquence pour qu'elle continue après 4
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

