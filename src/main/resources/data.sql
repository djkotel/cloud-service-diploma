INSERT INTO users (login, password) 
VALUES ('ivan', '$2a$10$YHVRGo4bXUfuC2l5L1AakOqTFqjJiQdZNL7EJV6rOYvtO0Q.zOgP2')
ON CONFLICT (login) DO NOTHING;
