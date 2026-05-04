CREATE TABLE ai_chat_messages (
                                  id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                  user_id BIGINT NOT NULL,
                                  role NVARCHAR(20) NOT NULL,
                                  message NVARCHAR(MAX) NOT NULL,
                                  created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

                                  CONSTRAINT fk_ai_chat_messages_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
);