ALTER TABLE m_group ADD COLUMN `loan_officer_id` BIGINT(20) NULL DEFAULT NULL AFTER `office_id`;
ALTER TABLE `m_group` ADD CONSTRAINT `FK_m_group_m_staff` FOREIGN KEY (`loan_officer_id`) REFERENCES `m_staff` (`id`);
