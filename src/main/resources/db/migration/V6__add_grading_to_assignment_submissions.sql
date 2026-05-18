ALTER TABLE dbo.assignment_submissions
    ADD
        grade DECIMAL(4,2) NULL,
    feedback NVARCHAR(MAX) NULL,
    graded_at DATETIME2 NULL,
    graded_by_user_id BIGINT NULL;

ALTER TABLE dbo.assignment_submissions
    ADD CONSTRAINT fk_assignment_submissions_graded_by_user
        FOREIGN KEY (graded_by_user_id)
            REFERENCES dbo.users(id);