CREATE TABLE `ai_user_daily_usage` (
    `user_id` BIGINT NOT NULL,
    `usage_date` DATE NOT NULL,
    `request_count` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `usage_date`),
    CONSTRAINT `fk_ai_user_daily_usage_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_ai_user_daily_usage_count`
        CHECK (`request_count` >= 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ai_global_daily_usage` (
    `usage_date` DATE NOT NULL,
    `request_count` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`usage_date`),
    CONSTRAINT `chk_ai_global_daily_usage_count`
        CHECK (`request_count` >= 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
