-- add loan transactions processing table

CREATE TABLE `ref_loan_transaction_processing_strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(100) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ltp_strategy_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8;


ALTER TABLE `portfolio_product_loan` 
ADD COLUMN `loan_transaction_strategy_id` bigint(20) DEFAULT NULL,
ADD KEY `FK_ltp_strategy` (`loan_transaction_strategy_id`),
ADD CONSTRAINT `FK_ltp_strategy` FOREIGN KEY (`loan_transaction_strategy_id`) REFERENCES `ref_loan_transaction_processing_strategy` (`id`);

ALTER TABLE `portfolio_loan` 
ADD COLUMN `loan_transaction_strategy_id` bigint(20) DEFAULT NULL,
ADD KEY `FK_loan_ltp_strategy` (`loan_transaction_strategy_id`),
ADD CONSTRAINT `FK_loan_ltp_strategy` FOREIGN KEY (`loan_transaction_strategy_id`) REFERENCES `ref_loan_transaction_processing_strategy` (`id`);

-- update ref table with out-of-the-box strategies

INSERT INTO `ref_loan_transaction_processing_strategy`
(`id`,`code`,`name`)
VALUES
(1, 'mifos-standard-strategy', 'Mifos style'),
(2, 'heavensfamily-strategy', 'Heavensfamily'),
(3, 'creocore-strategy', 'Creocore'),
(4, 'rbi-india-strategy', 'RBI (India)');