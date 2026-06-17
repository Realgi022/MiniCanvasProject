ALTER TABLE assignment_submissions
    ADD COLUMN grade DECIMAL(4,2) NULL,
    ADD COLUMN feedback TEXT NULL,
    ADD COLUMN graded_at TIMESTAMP NULL,
    ADD COLUMN graded_by_user_id BIGINT NULL;

ALTER TABLE assignment_submissions
    ADD CONSTRAINT fk_assignment_submissions_graded_by_user
        FOREIGN KEY (graded_by_user_id)
            REFERENCES users(id);