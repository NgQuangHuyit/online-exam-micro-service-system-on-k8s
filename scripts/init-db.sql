-- Tạo các database 
CREATE DATABASE IF NOT EXISTS exam_session_db;
CREATE DATABASE IF NOT EXISTS result_db;
CREATE DATABASE IF NOT EXISTS notification_db;
CREATE DATABASE IF NOT EXISTS student_db;

-- Cấp quyền cho user root
GRANT ALL PRIVILEGES ON quiz_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON exam_session_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON result_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON student_db.* TO 'root'@'%';

FLUSH PRIVILEGES;