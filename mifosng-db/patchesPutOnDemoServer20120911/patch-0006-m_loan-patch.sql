ALTER TABLE `m_loan`
	ADD COLUMN `loan_officer_id` BIGINT(20) NULL AFTER `fund_id`,
	ADD CONSTRAINT `FK_m_loan_m_staff` FOREIGN KEY (`loan_officer_id`) REFERENCES `m_staff` (`id`);