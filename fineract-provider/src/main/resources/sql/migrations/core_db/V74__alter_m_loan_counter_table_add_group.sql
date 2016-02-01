DROP TABLE `m_client_loan_counter`;
ALTER TABLE `m_loan`
	ADD COLUMN `loan_counter` SMALLINT NULL DEFAULT NULL AFTER `sync_disbursement_with_meeting`,
	ADD COLUMN `loan_product_counter` SMALLINT NULL DEFAULT NULL AFTER `loan_counter`;
