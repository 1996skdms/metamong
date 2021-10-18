# Metamong Mysql v8.0.25

CREATE SCHEMA if not exists metamong;

USE metamong;

CREATE TABLE if not exists Room(
id INT PRIMARY KEY AUTO_INCREMENT,
NAME VARCHAR(40) NOT NULL,
max_population INT DEFAULT 0
) COLLATE='utf8mb4_0900_ai_ci' 
ENGINE=INNODB;


CREATE TABLE if not exists User(
id INT PRIMARY KEY AUTO_INCREMENT,
room_id INT DEFAULT NULL,
email VARCHAR(50) UNIQUE,
password VARCHAR(100) NOT NULL,
file_url VARCHAR(200) DEFAULT NULL,
auth TINYINT DEFAULT 0,
nickname VARCHAR(20) UNIQUE NOT NULL,
NAME VARCHAR(40) NOT NULL,
state TINYINT DEFAULT 0,
FOREIGN KEY(room_id) REFERENCES Room(id) ON UPDATE CASCADE ON DELETE CASCADE
) 
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=INNODB;


CREATE TABLE if NOT EXISTS Message(
id INT PRIMARY KEY AUTO_INCREMENT,
sent_user_id INT,
recv_user_id INT,
content VARCHAR(500) DEFAULT NULL,
create_at TIMESTAMP DEFAULT NULL,
FOREIGN KEY(sent_user_id) REFERENCES User(id) ON UPDATE CASCADE ON DELETE CASCADE,
FOREIGN KEY(recv_user_id) REFERENCES User(id) ON UPDATE CASCADE ON DELETE CASCADE
) COLLATE='utf8mb4_0900_ai_ci'
ENGINE=INNODB;


CREATE TABLE if not exists Guest_Book(
id INT PRIMARY KEY AUTO_INCREMENT,
user_id INT,
create_at TIMESTAMP DEFAULT NULL,
content VARCHAR(500) DEFAULT NULL,
FOREIGN KEY(user_id) REFERENCES User(id) ON UPDATE CASCADE ON DELETE CASCADE
) COLLATE='utf8mb4_0900_ai_ci'
ENGINE=INNODB;


CREATE TABLE if NOT EXISTS Education(
id INT PRIMARY KEY AUTO_INCREMENT,
duration TIMESTAMP DEFAULT NULL,
education VARCHAR(50) DEFAULT NULL
) COLLATE='utf8mb4_0900_ai_ci'
ENGINE=INNODB;


CREATE TABLE if NOT EXISTS Certificate(
id INT PRIMARY KEY AUTO_INCREMENT,
user_id INT,
education_id INT,
create_at TIMESTAMP DEFAULT NULL,
pass_time TIMESTAMP DEFAULT NULL,
is_educated TINYINT DEFAULT 0,
is_authenticated TINYINT DEFAULT 0,
FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY (education_id) REFERENCES Education(id) ON DELETE CASCADE ON UPDATE CASCADE
) COLLATE='utf8mb4_0900_ai_ci'
ENGINE=INNODB;


CREATE TABLE if NOT EXISTS Mission(
id INT PRIMARY KEY AUTO_INCREMENT,
education_id INT,
ordering INT,
description VARCHAR(1000),
FOREIGN KEY (education_id) REFERENCES Education(id) ON DELETE CASCADE ON UPDATE CASCADE
) COLLATE='utf8mb4_0900_ai_ci'
ENGINE=INNODB;


CREATE TABLE if NOT EXISTS Firebase_Token(
id INT PRIMARY KEY AUTO_INCREMENT,
user_id INT,
token VARCHAR(300),
create_at TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE ON UPDATE CASCADE
) COLLATE='utf8mb4_0900_ai_ci'
ENGINE=INNODB;


CREATE TABLE if not exists Oauth_Provider(
id INT PRIMARY KEY AUTO_INCREMENT,
user_id INT,
provider_name VARCHAR(40),
FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE ON UPDATE CASCADE
) COLLATE='utf8mb4_0900_ai_ci'
ENGINE=INNODB;

COMMIT;

# 이메일 인증, jwt 등 Redis DB 사용 
