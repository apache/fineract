INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`)
VALUES
('transaction_savings', 'READ_ACCOUNTTRANSFER', 'ACCOUNTTRANSFER', 'READ', 0),
('transaction_savings', 'CREATE_ACCOUNTTRANSFER', 'ACCOUNTTRANSFER', 'CREATE', 1),
('transaction_savings', 'CREATE_ACCOUNTTRANSFER_CHECKER', 'ACCOUNTTRANSFER', 'CREATE', 0);


DROP TABLE IF EXISTS `m_savings_account_transfer`;

CREATE TABLE `m_savings_account_transfer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_office_id` bigint(20) NOT NULL,
  `to_office_id` bigint(20) NOT NULL,
  `from_client_id` bigint(20) NOT NULL,
  `to_client_id` bigint(20) NOT NULL,
  `from_savings_account_id` bigint(20) DEFAULT NULL,
  `to_savings_account_id` bigint(20) DEFAULT NULL,
  `from_loan_account_id` bigint(20) DEFAULT NULL,
  `to_loan_account_id` bigint(20) DEFAULT NULL,
  `from_savings_transaction_id` bigint(20) DEFAULT NULL,
  `from_loan_transaction_id` bigint(20) DEFAULT NULL,
  `to_savings_transaction_id` bigint(20) DEFAULT NULL,
  `to_loan_transaction_id` bigint(20) DEFAULT NULL,
  `is_reversed` tinyint(1) NOT NULL,
  `transaction_date` date NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `description` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKTRAN000000001` (`from_office_id`),
  KEY `FKTRAN000000002` (`from_client_id`),
  KEY `FKTRAN000000003` (`from_savings_account_id`),
  KEY `FKTRAN000000004` (`to_office_id`),
  KEY `FKTRAN000000005` (`to_client_id`),
  KEY `FKTRAN000000006` (`to_savings_account_id`),
  KEY `FKTRAN000000007` (`to_loan_account_id`),
  KEY `FKTRAN000000008` (`from_savings_transaction_id`),
  KEY `FKTRAN000000009` (`to_savings_transaction_id`),
  KEY `FKTRAN000000010` (`to_loan_transaction_id`),
  KEY `FKTRAN000000011` (`from_loan_account_id`),
  KEY `FKTRAN000000012` (`from_loan_transaction_id`),
  CONSTRAINT `FKTRAN000000001` FOREIGN KEY (`from_office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FKTRAN000000002` FOREIGN KEY (`from_client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKTRAN000000003` FOREIGN KEY (`from_savings_account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `FKTRAN000000004` FOREIGN KEY (`to_office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FKTRAN000000005` FOREIGN KEY (`to_client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKTRAN000000006` FOREIGN KEY (`to_savings_account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `FKTRAN000000007` FOREIGN KEY (`to_loan_account_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FKTRAN000000008` FOREIGN KEY (`from_savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`),
  CONSTRAINT `FKTRAN000000009` FOREIGN KEY (`to_savings_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`),
  CONSTRAINT `FKTRAN000000010` FOREIGN KEY (`to_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FKTRAN000000011` FOREIGN KEY (`from_loan_account_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FKTRAN000000012` FOREIGN KEY (`from_loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;