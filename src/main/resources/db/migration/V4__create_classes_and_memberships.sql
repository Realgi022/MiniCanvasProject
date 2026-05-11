CREATE TABLE dbo.classes (
                             id BIGINT IDENTITY(1,1) PRIMARY KEY,
                             name NVARCHAR(100) NOT NULL UNIQUE,
                             created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE dbo.class_memberships (
                                       id BIGINT IDENTITY(1,1) PRIMARY KEY,

                                       class_id BIGINT NOT NULL,
                                       user_id BIGINT NOT NULL,

                                       class_role NVARCHAR(50) NOT NULL,

                                       assigned_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

                                       CONSTRAINT fk_class_memberships_class
                                           FOREIGN KEY (class_id)
                                               REFERENCES dbo.classes(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT fk_class_memberships_user
                                           FOREIGN KEY (user_id)
                                               REFERENCES dbo.users(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT uq_class_membership_user_class
                                           UNIQUE (class_id, user_id),

                                       CONSTRAINT chk_class_membership_role
                                           CHECK (class_role IN ('STUDENT', 'TEACHER'))
);

INSERT INTO dbo.classes(name)
VALUES
    ('MA-FSD-MA-FSD3'),
    ('MA-FSD-MA-FSD2'),
    ('MA-FSD-MA-FSD1');