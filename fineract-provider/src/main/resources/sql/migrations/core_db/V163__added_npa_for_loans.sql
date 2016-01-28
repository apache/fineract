ALTER TABLE `m_product_loan`
	ADD COLUMN `overdue_days_for_npa` SMALLINT(5) NULL DEFAULT NULL AFTER `grace_on_arrears_ageing`;

ALTER TABLE `m_loan`
	ADD COLUMN `is_npa` TINYINT(1) NOT NULL DEFAULT '0' AFTER `grace_on_arrears_ageing`;

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`) VALUES ('Update Non Performing Assets', 'Update Non Performing Assets', '0 0 0 1/1 * ? *', now());