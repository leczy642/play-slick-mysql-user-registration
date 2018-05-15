# --- !Ups

create table users (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(100) NOT NULL,
      phone VARCHAR(100) NOT NULL,
      email VARCHAR(100) NOT NULL,
      age INT(3) NOT NULL
);

# --- !Downs

drop table "users" if exists;
