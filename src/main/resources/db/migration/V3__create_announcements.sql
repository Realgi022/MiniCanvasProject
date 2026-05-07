CREATE TABLE dbo.announcements (
                                   id BIGINT IDENTITY(1,1) PRIMARY KEY,

                                   title NVARCHAR(255) NOT NULL,
                                   content NVARCHAR(MAX) NOT NULL,

                                   created_by_user_id BIGINT NOT NULL,

                                   created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                                   updated_at DATETIME2 NULL,

                                   CONSTRAINT fk_announcements_created_by_user
                                       FOREIGN KEY (created_by_user_id)
                                           REFERENCES dbo.users(id)
);