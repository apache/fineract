ALTER TABLE `m_staff`
	ADD COLUMN `office_id` BIGINT(20) NULL AFTER `id`,
	ADD CONSTRAINT `FK_m_staff_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`);
	
ALTER TABLE `m_staff`
	ADD COLUMN `is_loan_officer` TINYINT(1) NOT NULL DEFAULT 0 AFTER `id`;
	
update m_staff set office_id=1;
	
