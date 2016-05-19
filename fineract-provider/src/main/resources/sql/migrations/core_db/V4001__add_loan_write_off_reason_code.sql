INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('WriteOffReasons', 1);

ALTER TABLE `m_loan` 
add column `writeoff_reason_cv_id`  INT(11) NULL DEFAULT NULL,
add CONSTRAINT `FK_writeoffreason_m_loan_m_code_value` FOREIGN KEY (`writeoff_reason_cv_id`) REFERENCES `m_code_value` (`id`);
