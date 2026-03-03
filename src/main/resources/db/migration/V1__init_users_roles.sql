CREATE TABLE dbo.users (
                           id BIGINT IDENTITY(1,1) PRIMARY KEY,
                           email NVARCHAR(255) NOT NULL UNIQUE,
                           password_hash NVARCHAR(255) NOT NULL,
                           full_name NVARCHAR(255) NULL,
                           enabled BIT NOT NULL DEFAULT 1,
                           created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE dbo.roles (
                           id INT IDENTITY(1,1) PRIMARY KEY,
                           name NVARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE dbo.user_roles (
                                user_id BIGINT NOT NULL,
                                role_id INT NOT NULL,
                                PRIMARY KEY (user_id, role_id),
                                CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
                                CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES dbo.roles(id)
);

INSERT INTO dbo.roles(name) VALUES ('ADMIN'), ('TEACHER'), ('STUDENT')