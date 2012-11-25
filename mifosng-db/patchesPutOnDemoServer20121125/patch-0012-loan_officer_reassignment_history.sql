CREATE TABLE `m_loan_officer_assignment_history` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`loan_id` bigint(20) NOT NULL ,
`loan_officer_id` bigint(20) DEFAULT NULL,
`start_date` date NOT NULL,
`end_date` date DEFAULT NULL,
`createdby_id` bigint(20) DEFAULT NULL,
`created_date` datetime DEFAULT NULL,
`lastmodified_date` datetime DEFAULT NULL,
`lastmodifiedby_id` bigint(20) DEFAULT NULL,
PRIMARY KEY (`id`),
CONSTRAINT `fk_m_loan_officer_assignment_history_0001` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
CONSTRAINT `fk_m_loan_officer_assignment_history_0002` FOREIGN KEY (`loan_officer_id`) REFERENCES `m_staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
