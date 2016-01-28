ALTER TABLE `m_loan`
	ADD COLUMN `loan_type_enum` SMALLINT(5) NOT NULL AFTER `loan_status_id`;