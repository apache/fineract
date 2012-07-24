-- This script is the core DDL for mifos platform
-- Its includes tables related to lookup/reference data, user adminsitration, organisation modeling, client and loans
-- and infrastructure tables needed for 'additional data' and 'screen based reporting' functionality.

-- It does not include DDL customisations needed for specific 'tenants' for 'additional data' functionality around clients/loans.

-- This script drops all tables and re-creates from scratch.

-- drop tables in base-schema

-- portfolio related tables
DROP TABLE IF EXISTS `portfolio_note`;
DROP TABLE IF EXISTS `portfolio_loan_transaction`;
DROP TABLE IF EXISTS `portfolio_loan_repayment_schedule`;
DROP TABLE IF EXISTS `portfolio_loan`;
DROP TABLE IF EXISTS `portfolio_client`;
DROP TABLE IF EXISTS `portfolio_product_loan`;
DROP TABLE IF EXISTS `portfolio_product_savings`;
-- organisation related tables
DROP TABLE IF EXISTS `org_organisation_currency`;
DROP TABLE IF EXISTS `org_fund`;
DROP TABLE IF EXISTS `portfolio_office_transaction`;
SET foreign_key_checks = 0;
DROP TABLE IF EXISTS `org_office`;
SET foreign_key_checks = 1;
-- admin tables
DROP TABLE IF EXISTS `admin_role_permission`;
DROP TABLE IF EXISTS `admin_appuser_role`;
DROP TABLE IF EXISTS `admin_appuser`;
DROP TABLE IF EXISTS `admin_permission`;
DROP TABLE IF EXISTS `admin_role`;

-- drop reference/lookup tables
DROP TABLE IF EXISTS `ref_currency`;
DROP TABLE IF EXISTS `ref_loan_status`;

-- additional data and report tables
DROP TABLE IF EXISTS `stretchydata_dataset_fields`;
DROP TABLE IF EXISTS `stretchydata_dataset`;
DROP TABLE IF EXISTS `stretchydata_allowed_value`;
DROP TABLE IF EXISTS `stretchydata_allowed_list`;
DROP TABLE IF EXISTS `stretchydata_datasettype`;
DROP TABLE IF EXISTS `stretchy_parameter`;
DROP TABLE IF EXISTS `stretchy_report_parameter`;
DROP TABLE IF EXISTS `stretchy_report`;
DROP TABLE IF EXISTS `rpt_sequence`;

-- DDL for reference/lookup tables
CREATE TABLE `ref_currency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(3) NOT NULL,
  `decimal_places` smallint(5) NOT NULL,
  `display_symbol` varchar(10) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `internationalized_name_code` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `ref_loan_status` (
  `id` smallint(5) NOT NULL,
  `display_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- DDL for organisation wide related concepts
CREATE TABLE `org_fund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fund_name_org` (`name`),
  UNIQUE KEY `fund_externalid_org` (`external_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `org_office` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL,
  `hierarchy` varchar(100) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `opening_date` date NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_org` (`name`),
  UNIQUE KEY `externalid_org` (`external_id`),
  KEY `FK2291C477E2551DCC` (`parent_id`),
  CONSTRAINT `FK2291C477E2551DCC` FOREIGN KEY (`parent_id`) REFERENCES `org_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `portfolio_office_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_office_id` bigint(20) DEFAULT NULL,
  `to_office_id` bigint(20) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` int(11) NOT NULL,
  `transaction_amount` decimal(19,6) NOT NULL,
  `transaction_date` date NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1E37728B93C6C1B6` (`to_office_id`),
  KEY `FK1E37728B783C5C25` (`from_office_id`),
  CONSTRAINT `FK1E37728B783C5C25` FOREIGN KEY (`from_office_id`) REFERENCES `org_office` (`id`),
  CONSTRAINT `FK1E37728B93C6C1B6` FOREIGN KEY (`to_office_id`) REFERENCES `org_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `org_organisation_currency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(3) NOT NULL,
  `decimal_places` smallint(5) NOT NULL,
  `name` varchar(50) NOT NULL,
  `display_symbol` varchar(10) DEFAULT NULL,
  `internationalized_name_code` varchar(50) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- DDL for admin tables
CREATE TABLE `admin_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_enum` smallint(5) NOT NULL,
  `code` varchar(100) NOT NULL,
  `default_description` varchar(500) NOT NULL,
  `default_name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `admin_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `admin_appuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  `firstname` varchar(100) NOT NULL,
  `lastname` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `firsttime_login_remaining` bit(1) NOT NULL,
  `nonexpired` bit(1) NOT NULL,
  `nonlocked` bit(1) NOT NULL,
  `nonexpired_credentials` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_org` (`username`),
  KEY `FKB3D587CE0DD567A` (`office_id`),
  CONSTRAINT `FKB3D587CE0DD567A` FOREIGN KEY (`office_id`) REFERENCES `org_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `admin_role_permission` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FK8DEDB04815CEC7AB` (`role_id`),
  KEY `FK8DEDB048103B544B` (`permission_id`),
  CONSTRAINT `FK8DEDB048103B544B` FOREIGN KEY (`permission_id`) REFERENCES `admin_permission` (`id`),
  CONSTRAINT `FK8DEDB04815CEC7AB` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `admin_appuser_role` (
  `appuser_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`appuser_id`,`role_id`),
  KEY `FK7662CE59B4100309` (`appuser_id`),
  KEY `FK7662CE5915CEC7AB` (`role_id`),
  CONSTRAINT `FK7662CE5915CEC7AB` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`),
  CONSTRAINT `FK7662CE59B4100309` FOREIGN KEY (`appuser_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- DDL for portfolio tables
CREATE TABLE `portfolio_client` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `firstname` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `joining_date` date DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FKCE00CAB3E0DD567A` (`office_id`),
  KEY `FKAUD0000000000001` (`createdby_id`),
  KEY `FKAUD0000000000002` (`lastmodifiedby_id`),
  CONSTRAINT `FKAUD0000000000001` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FKAUD0000000000002` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FKCE00CAB3E0DD567A` FOREIGN KEY (`office_id`) REFERENCES `org_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `portfolio_product_loan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `principal_amount` decimal(19,6) NOT NULL,
  `arrearstolerance_amount` decimal(19,6) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `fund_id` bigint(20) DEFAULT NULL,
  `nominal_interest_rate_per_period` decimal(19,6) NOT NULL,
  `interest_period_frequency_enum` smallint(5) NOT NULL,
  `annual_nominal_interest_rate` decimal(19,6) NOT NULL,
  `interest_method_enum` smallint(5) NOT NULL,
  `interest_calculated_in_period_enum` smallint(5) NOT NULL DEFAULT '1',
  `repay_every` smallint(5) NOT NULL,
  `repayment_period_frequency_enum` smallint(5) NOT NULL,
  `number_of_repayments` smallint(5) NOT NULL,
  `amortization_method_enum` smallint(5) NOT NULL,
  `flexible_repayment_schedule` bit(1) NOT NULL,
  `interest_rebate` bit(1) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKAUD0000000000003` (`createdby_id`),
  KEY `FKAUD0000000000004` (`lastmodifiedby_id`),
  KEY `FKA6A8A7D77240145` (`fund_id`),
  CONSTRAINT `FKA6A8A7D77240145` FOREIGN KEY (`fund_id`) REFERENCES `org_fund` (`id`),
  CONSTRAINT `FKAUD0000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FKAUD0000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `portfolio_product_savings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKJPW0000000000003` (`createdby_id`),
  KEY `FKJPW0000000000004` (`lastmodifiedby_id`),
  CONSTRAINT `FKJPW0000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FKJPW0000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE `portfolio_loan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `external_id` varchar(100) DEFAULT NULL,
  `client_id` bigint(20) NOT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `fund_id` bigint(20) DEFAULT NULL,
  `loan_status_id` smallint(5) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `principal_amount` decimal(19,6) NOT NULL,
  `arrearstolerance_amount` decimal(19,6) NOT NULL,
  `nominal_interest_rate_per_period` decimal(19,6) NOT NULL,
  `interest_period_frequency_enum` smallint(5) NOT NULL,
  `annual_nominal_interest_rate` decimal(19,6) NOT NULL,
  `interest_method_enum` smallint(5) NOT NULL,
  `interest_calculated_in_period_enum` smallint(5) NOT NULL DEFAULT '1',
  `repay_every` smallint(5) NOT NULL,
  `repayment_period_frequency_enum` smallint(5) NOT NULL,
  `number_of_repayments` smallint(5) NOT NULL,
  `amortization_method_enum` smallint(5) NOT NULL,
  `flexible_repayment_schedule` bit(1) NOT NULL,
  `interest_rebate` bit(1) NOT NULL,
  `interest_rebate_amount` decimal(19,6) DEFAULT NULL,
  `submittedon_date` datetime DEFAULT NULL,
  `approvedon_date` datetime DEFAULT NULL,
  `expected_disbursedon_date` date DEFAULT NULL,
  `expected_firstrepaymenton_date` date DEFAULT NULL,
  `interest_calculated_from_date` date DEFAULT NULL,
  `disbursedon_date` date DEFAULT NULL,
  `expected_maturedon_date` date DEFAULT NULL,
  `maturedon_date` date DEFAULT NULL,
  `closedon_date` datetime DEFAULT NULL,
  `rejectedon_date` datetime DEFAULT NULL,
  `rescheduledon_date` datetime DEFAULT NULL,
  `withdrawnon_date` datetime DEFAULT NULL,
  `writtenoffon_date` datetime DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `org_id` (`external_id`),
  KEY `FKB6F935D87179A0CB` (`client_id`),
  KEY `FKB6F935D8C8D4B434` (`product_id`),
  KEY `FK7C885878B1147D1` (`loan_status_id`),
  KEY `FK7C885877240145` (`fund_id`),
  CONSTRAINT `FK7C885877240145` FOREIGN KEY (`fund_id`) REFERENCES `org_fund` (`id`),
  CONSTRAINT `FK7C885878B1147D1` FOREIGN KEY (`loan_status_id`) REFERENCES `ref_loan_status` (`id`),
  CONSTRAINT `FKB6F935D87179A0CB` FOREIGN KEY (`client_id`) REFERENCES `portfolio_client` (`id`),
  CONSTRAINT `FKB6F935D8C8D4B434` FOREIGN KEY (`product_id`) REFERENCES `portfolio_product_loan` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `portfolio_loan_repayment_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `duedate` date DEFAULT NULL,
  `installment` smallint(5) DEFAULT NULL,
  `principal_amount` decimal(19,6) DEFAULT NULL,
  `principal_completed_derived` decimal(19,6) DEFAULT NULL,
  `interest_amount` decimal(19,6) DEFAULT NULL,
  `interest_completed_derived` decimal(19,6) DEFAULT NULL,
  `completed_derived` bit(1) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK488B92AA40BE0710` (`loan_id`),
  CONSTRAINT `FK488B92AA40BE0710` FOREIGN KEY (`loan_id`) REFERENCES `portfolio_loan` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `portfolio_loan_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `transaction_type_enum` smallint(5) NOT NULL,
  `contra_id` bigint(20) DEFAULT NULL,
  `transaction_date` date NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKCFCEA42640BE0710` (`loan_id`),
  KEY `FKCFCEA426FC69F3F1` (`contra_id`),
  CONSTRAINT `FKCFCEA42640BE0710` FOREIGN KEY (`loan_id`) REFERENCES `portfolio_loan` (`id`),
  CONSTRAINT `FKCFCEA426FC69F3F1` FOREIGN KEY (`contra_id`) REFERENCES `portfolio_loan_transaction` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `portfolio_note` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `loan_transaction_id` bigint(20) DEFAULT NULL,
  `note_type_enum` smallint(5) NOT NULL,
  `note` varchar(1000) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7C9708924D26803` (`loan_transaction_id`),
  KEY `FK7C97089541F0A56` (`createdby_id`),
  KEY `FK7C970897179A0CB` (`client_id`),
  KEY `FK7C970898F889C3F` (`lastmodifiedby_id`),
  KEY `FK7C9708940BE0710` (`loan_id`),
  CONSTRAINT `FK7C9708924D26803` FOREIGN KEY (`loan_transaction_id`) REFERENCES `portfolio_loan_transaction` (`id`),
  CONSTRAINT `FK7C9708940BE0710` FOREIGN KEY (`loan_id`) REFERENCES `portfolio_loan` (`id`),
  CONSTRAINT `FK7C97089541F0A56` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FK7C970897179A0CB` FOREIGN KEY (`client_id`) REFERENCES `portfolio_client` (`id`),
  CONSTRAINT `FK7C970898F889C3F` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- DDL for additional data (aka stretchy data) tables
CREATE TABLE `rpt_sequence` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=483 DEFAULT CHARSET=utf8;

CREATE TABLE `stretchydata_datasettype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `stretchy_parameter` (
  `parameter_id` int(11) NOT NULL AUTO_INCREMENT,
  `parameter_name` varchar(45) NOT NULL,
  `parameter_variable` varchar(45) DEFAULT NULL,
  `parameter_label` varchar(45) NOT NULL,
  `parameter_displayType` varchar(45) NOT NULL,
  `parameter_FormatType` varchar(10) NOT NULL,
  `parameter_default` varchar(45) NOT NULL,
  `special` varchar(1) DEFAULT NULL,
  `selectOne` varchar(1) DEFAULT NULL,
  `selectAll` varchar(1) DEFAULT NULL,
  `parameter_sql` text,
  PRIMARY KEY (`parameter_id`),
  UNIQUE KEY `name_UNIQUE` (`parameter_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `stretchydata_allowed_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `stretchydata_allowed_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `allowed_list_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `stretchydata_allowed_value_fk1` (`allowed_list_id`,`name`),
  CONSTRAINT `stretchydata_allowed_value_fk1` FOREIGN KEY (`allowed_list_id`) REFERENCES `stretchydata_allowed_list` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `stretchydata_dataset` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `datasettype_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `stretchydata_dataset_fk1` (`datasettype_id`,`name`),
  CONSTRAINT `stretchydata_dataset` FOREIGN KEY (`datasettype_id`) REFERENCES `stretchydata_datasettype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `stretchydata_dataset_fields` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `data_type` varchar(15) NOT NULL,
  `data_length` int(11) DEFAULT NULL,
  `display_type` varchar(45) DEFAULT NULL,
  `dataset_id` int(11) NOT NULL,
  `allowed_list_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `stretchydata_dataset_fields_fk1` (`dataset_id`,`name`),
  KEY `stretchydata_dataset_fields_fk2` (`allowed_list_id`),
  CONSTRAINT `stretchydata_dataset_fields_fk1` FOREIGN KEY (`dataset_id`) REFERENCES `stretchydata_dataset` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `stretchydata_dataset_fields_fk2` FOREIGN KEY (`allowed_list_id`) REFERENCES `stretchydata_allowed_list` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `stretchy_report` (
  `report_id` int(11) NOT NULL AUTO_INCREMENT,
  `report_name` varchar(100) NOT NULL,
  `report_type` varchar(20) NOT NULL,
  `report_subtype` varchar(20) DEFAULT NULL,
  `report_category` varchar(45) DEFAULT NULL,
  `report_sql` text,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `report_name_UNIQUE` (`report_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `stretchy_report_parameter` (
  `report_id` int(11) NOT NULL,
  `parameter_id` int(11) NOT NULL,
  `report_parameter_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`report_id`,`parameter_id`),
  UNIQUE KEY `report_id_name_UNIQUE` (`report_id`,`report_parameter_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;