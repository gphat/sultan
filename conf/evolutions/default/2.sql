# --- !Ups

ALTER TABLE users ADD timezone VARCHAR(64) NOT NULL;

UPDATE users SET timezone='America/Los_Angeles';

# --- !Downs

#ALTER TABLE users DROP timezone;