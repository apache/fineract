ALTER TABLE `m_client_loan_counter`
	ADD COLUMN `group_id` BIGINT(20) NOT NULL AFTER `client_id`,
	ADD CONSTRAINT `FK_m_group_loan_counter` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`);
	
RENAME TABLE `m_client_loan_counter` TO `m_loan_counter`;

ALTER TABLE `m_loan_counter`
	CHANGE COLUMN `client_id` `client_id` BIGINT(20) NULL DEFAULT NULL AFTER `id`,
	CHANGE COLUMN `group_id` `group_id` BIGINT(20) NULL DEFAULT NULL AFTER `client_id`;

ALTER TABLE `m_loan`
	ADD COLUMN `loan_counter` SMALLINT NULL DEFAULT NULL AFTER `sync_disbursement_with_meeting`,
	ADD COLUMN `loan_product_counter` SMALLINT NULL DEFAULT NULL AFTER `loan_counter`;

DROP TABLE `m_loan_counter`;
