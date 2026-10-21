
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255),
    `password` VARCHAR(255),
    `role` VARCHAR(255),
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_user_email` UNIQUE (`email`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `quote` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `author` VARCHAR(255),
    `quote` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `task_entry` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `completed` BIT(1) NOT NULL,
    `date` DATE NOT NULL,
    `task_description` VARCHAR(500) NOT NULL,
    `user_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_task_entry_user_id` (`user_id`),
    CONSTRAINT `fk_task_entry_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `daily_quote` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `date` DATE NOT NULL,
    `quote` VARCHAR(255),
    `quote_id` BIGINT,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_daily_quote_date` UNIQUE (`date`),
    KEY `idx_daily_quote_quote_id` (`quote_id`),
    CONSTRAINT `fk_daily_quote_quote`
        FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
