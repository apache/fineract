
DROP TABLE IF EXISTS `m_loan_arrears_aging`;
CREATE TABLE `m_loan_arrears_aging` (
  `loan_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `principal_overdue_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  `interest_overdue_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  `fee_charges_overdue_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  `penalty_charges_overdue_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  `total_overdue_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  `overdue_since_date_derived` date DEFAULT NULL,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `m_loan_arrears_aging_ibfk_1` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE m_loan
 DROP COLUMN `total_overdue_derived` , 
DROP COLUMN `overdue_since_date_derived` , 
DROP COLUMN `penalty_charges_overdue_derived` , 
DROP COLUMN `fee_charges_overdue_derived` , 
DROP COLUMN `interest_overdue_derived` , 
DROP COLUMN `principal_overdue_derived` ;
