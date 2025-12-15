-- Tables pour le système de Quiz avec IA

-- Table des Quiz
CREATE TABLE IF NOT EXISTS quizzes (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(1000) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    passing_score INTEGER NOT NULL DEFAULT 70,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by BIGINT,
    generated_by_ai BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_quiz_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Table des Questions de Quiz
CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    correct_answer TEXT NOT NULL,
    points INTEGER DEFAULT 1,
    explanation TEXT,
    question_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_question_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Table des Options de Questions
CREATE TABLE IF NOT EXISTS quiz_question_options (
    question_id BIGINT NOT NULL,
    option TEXT,
    option_order INTEGER NOT NULL,
    CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE
);

-- Table des Tentatives de Quiz
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    score INTEGER,
    total_points INTEGER,
    earned_points INTEGER,
    passed BOOLEAN NOT NULL DEFAULT false,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_attempt_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Table des Réponses aux Tentatives
CREATE TABLE IF NOT EXISTS quiz_attempt_answers (
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer TEXT,
    CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    PRIMARY KEY (attempt_id, question_id)
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_quiz_course ON quizzes(course_id);
CREATE INDEX IF NOT EXISTS idx_quiz_status ON quizzes(status);
CREATE INDEX IF NOT EXISTS idx_question_quiz ON quiz_questions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_attempt_user_course ON quiz_attempts(user_id, course_id);
CREATE INDEX IF NOT EXISTS idx_attempt_quiz ON quiz_attempts(quiz_id);
CREATE INDEX IF NOT EXISTS idx_attempt_passed ON quiz_attempts(passed);

