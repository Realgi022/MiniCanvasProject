CREATE TABLE dbo.assignments (
                                 id BIGINT IDENTITY(1,1) PRIMARY KEY,

                                 class_id BIGINT NOT NULL,
                                 created_by_user_id BIGINT NOT NULL,

                                 title NVARCHAR(255) NOT NULL,
                                 description NVARCHAR(MAX) NULL,
                                 due_at DATETIME2 NULL,

                                 created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                                 updated_at DATETIME2 NULL,

                                 CONSTRAINT fk_assignments_class
                                     FOREIGN KEY (class_id)
                                         REFERENCES dbo.classes(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_assignments_created_by_user
                                     FOREIGN KEY (created_by_user_id)
                                         REFERENCES dbo.users(id)
);

CREATE TABLE dbo.assignment_submissions (
                                            id BIGINT IDENTITY(1,1) PRIMARY KEY,

                                            assignment_id BIGINT NOT NULL,
                                            student_user_id BIGINT NOT NULL,

                                            original_file_name NVARCHAR(255) NOT NULL,
                                            stored_file_name NVARCHAR(255) NOT NULL,
                                            file_path NVARCHAR(500) NOT NULL,
                                            content_type NVARCHAR(100) NULL,
                                            file_size BIGINT NOT NULL,

                                            comment NVARCHAR(MAX) NULL,

                                            submitted_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                                            updated_at DATETIME2 NULL,

                                            CONSTRAINT fk_assignment_submissions_assignment
                                                FOREIGN KEY (assignment_id)
                                                    REFERENCES dbo.assignments(id)
                                                    ON DELETE CASCADE,

                                            CONSTRAINT fk_assignment_submissions_student
                                                FOREIGN KEY (student_user_id)
                                                    REFERENCES dbo.users(id),

                                            CONSTRAINT uq_assignment_student_submission
                                                UNIQUE (assignment_id, student_user_id)
);