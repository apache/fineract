DROP TABLE IF EXISTS `m_loan_collateral`;
CREATE TABLE `m_loan_collateral` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `type_cv_id` int(11) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_collateral_m_loan` (`loan_id`),
  KEY `FK_collateral_code_value` (`type_cv_id`),
  CONSTRAINT `FK_collateral_m_loan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_collateral_code_value` FOREIGN KEY (`type_cv_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `m_loan` 
ADD COLUMN `loanpurpose_cv_id` INT(11) DEFAULT NULL AFTER `guarantor_id`,
ADD KEY `FK_m_loanpurpose_codevalue` (`loanpurpose_cv_id`),
ADD CONSTRAINT `FK_m_loanpurpose_codevalue` FOREIGN KEY (`loanpurpose_cv_id`) REFERENCES `m_code_value` (`id`);