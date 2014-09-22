ALTER TABLE `m_holiday`
	ADD COLUMN `repayments_rescheduled_to` DATETIME NOT NULL AFTER `to_date`,
	ADD COLUMN `processed` TINYINT(1) NOT NULL DEFAULT '0' AFTER `repayments_rescheduled_to`;