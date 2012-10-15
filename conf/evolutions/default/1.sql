# --- !Ups

CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    realname VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(username)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO users (username, password, realname, email, date_created) VALUES ('admin', '$2a$10$uSwONYkaPNm1fUFX5JSH5Oh1hRGBu70FTXNQL5bSfLV/sS3CrxWwa', 'admin', 'admin@admin.com', UTC_TIMESTAMP());

CREATE TABLE systems (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE change_types (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(6) NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE changes (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    owner_id INT UNSIGNED NOT NULL,
    change_type_id INT UNSIGNED NOT NULL,
    duration INT UNSIGNED NOT NULL,
    summary VARCHAR(255) NOT NULL,
    description TEXT,
    notes TEXT,
    date_begun DATETIME,
    date_closed DATETIME,
    date_completed DATETIME,
    date_created DATETIME NOT NULL,
    date_scheduled DATETIME NOT NULL,
    risk TINYINT NOT NULL,
    success TINYINT(1) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (change_type_id) REFERENCES change_types(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE change_systems (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    change_id INT UNSIGNED NOT NULL,
    system_id INT UNSIGNED NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (change_id) REFERENCES changes(id),
    FOREIGN KEY (system_id) REFERENCES systems(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin

# --- !Downs

DROP TABLE change_systems;
DROP TABLE change_types;
DROP TABLE changes;
DROP TABLE systems;
DROP TABLE users;
