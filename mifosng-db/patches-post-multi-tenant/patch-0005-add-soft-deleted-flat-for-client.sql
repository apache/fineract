-- add soft delete flag for clients

ALTER TABLE `portfolio_client` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0;
