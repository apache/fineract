ALTER TABLE `m_note` 
DROP FOREIGN KEY `FK_m_note_m_saving_account`,
DROP FOREIGN KEY `FK_m_note_m_deposit_account`;

ALTER TABLE `m_note` 
DROP COLUMN `saving_account_id`,
DROP COLUMN `deposit_account_id`, 
DROP INDEX `FK_m_note_m_saving_account`,
DROP INDEX `FK_m_note_m_deposit_account`;


drop table `m_saving_account_transaction`;
drop table `m_saving_schedule`;
drop table `m_saving_account`;

drop table `m_deposit_account_transaction`;
drop table `m_deposit_account`;

drop table `m_product_savings`;
drop table `m_product_deposit`;



DROP TABLE IF EXISTS `m_savings_account`;
DROP TABLE IF EXISTS `m_savings_product`;

CREATE TABLE `m_savings_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `nominal_interest_rate_per_period` decimal(19,6) NOT NULL,
  `nominal_interest_rate_period_frequency_enum` smallint(5) NOT NULL,
  `min_required_opening_balance` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency_enum` smallint(5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sp_unq_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `m_savings_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_no` varchar(20) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `nominal_interest_rate_per_period` decimal(19,6) NOT NULL,
  `nominal_interest_rate_period_frequency_enum` smallint(5) NOT NULL,
  `annual_nominal_interest_rate` decimal(19,6) NOT NULL,
  `min_required_opening_balance` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency` decimal(19,6) DEFAULT NULL,
  `lockin_period_frequency_enum` smallint(5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sa_account_no_UNIQUE` (`account_no`),
  UNIQUE KEY `sa_externalid_UNIQUE` (`external_id`),
  KEY `FKSA00000000000001` (`client_id`),
  KEY `FKSA00000000000002` (`group_id`),
  KEY `FKSA00000000000003` (`product_id`),
  CONSTRAINT `FKSA00000000000001` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKSA00000000000002` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `FKSA00000000000003` FOREIGN KEY (`product_id`) REFERENCES `m_savings_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;