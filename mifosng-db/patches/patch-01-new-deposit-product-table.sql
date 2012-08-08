ALTER TABLE `portfolio_product_savings` 
DROP COLUMN `ft_pre_closure_interest_rate` , 
DROP COLUMN `ft_can_pre_close` , 
DROP COLUMN `ft_can_renew` , 
DROP COLUMN `ft_maturity_max_interest_rate` , 
DROP COLUMN `ft_maturity_min_interest_rate` , 
DROP COLUMN `ft_maturity_default_interest_rate` , 
DROP COLUMN `ft_tenure_months` , 
DROP COLUMN `savings_type_enum` ;


CREATE TABLE `portfolio_product_deposit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `minimum_balance` decimal(19,6) DEFAULT NULL,
  `maximum_balance` decimal(19,6) DEFAULT NULL,
  `tenure_months` int(11) NOT NULL,
  `maturity_default_interest_rate` decimal(19,6) NOT NULL,
  `maturity_min_interest_rate` decimal(19,6) NOT NULL,
  `maturity_max_interest_rate` decimal(19,6) NOT NULL,
  `can_renew` tinyint(1) NOT NULL DEFAULT '0',
  `can_pre_close` tinyint(1) NOT NULL DEFAULT '0',
  `pre_closure_interest_rate` decimal(19,6) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKJPW0000000000003` (`createdby_id`),
  KEY `FKJPW0000000000004` (`lastmodifiedby_id`),
  CONSTRAINT `FKJPX0000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FKJPX0000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
