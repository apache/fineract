DROP TABLE `portfolio_deposit_account`;
CREATE TABLE `portfolio_deposit_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `external_id` varchar(100) DEFAULT NULL,
  `client_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `deposit_amount` decimal(19,6) DEFAULT NULL,
  `maturity_interest_rate` decimal(19,6) NOT NULL,
  `tenure_months` int(11) NOT NULL,
  `can_renew` tinyint(1) NOT NULL DEFAULT '0',
  `can_pre_close` tinyint(1) NOT NULL DEFAULT '0',
  `pre_closure_interest_rate` decimal(19,6) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `deposit_acc_external_id` (`external_id`),
  KEY `FKKW0000000000001` (`client_id`),
  KEY `FKKW0000000000002` (`product_id`),
  CONSTRAINT `FKKW0000000000001` FOREIGN KEY (`client_id`) REFERENCES `portfolio_client` (`id`),
  CONSTRAINT `FKKW0000000000002` FOREIGN KEY (`product_id`) REFERENCES `portfolio_product_deposit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

