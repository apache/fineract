ALTER TABLE `portfolio_group` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 AFTER `external_id`;
