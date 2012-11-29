# --- !Ups

ALTER TABLE users ADD timezone VARCHAR(64) NOT NULL;

UPDATE users SET timezone='America/Los_Angeles';

CREATE TABLE window_types (
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE windows (
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  owner_id INT UNSIGNED NOT NULL,
  window_type_id INT UNSIGNED NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  date_created DATETIME NOT NULL,
  date_begun DATETIME NOT NULL,
  date_ended DATETIME NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY (window_type_id) REFERENCES window_types(id)
);

# --- !Downs

ALTER TABLE users DROP timezone;
DROP TABLE windows;
DROP TABLE window_types;