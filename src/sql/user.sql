CREATE DATABASE mytest;

CREATE TABLE t_user(
  user_id INT NOT NULL PRIMARY KEY,
  user_name VARCHAR(255) NOT NULL ,
  password VARCHAR(255) NOT NULL ,
  phone VARCHAR(255) NOT NULL
) ;