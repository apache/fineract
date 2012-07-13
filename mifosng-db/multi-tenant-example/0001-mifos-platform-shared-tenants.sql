-- create tenants table
DROP TABLE IF EXISTS `mifosplatform-tenants`.`tenants`;
CREATE TABLE `mifosplatform-tenants`.`tenants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `schema_name` varchar(100) NOT NULL,
  `timezone_id` int(11) DEFAULT NULL,
  `country_id` int(11) DEFAULT NULL,
  `joined_date` date DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `mifosplatform-tenants`.`tenants`
(`id`,`name`, `identifier`,`schema_name`)
VALUES
(1, 'Demo IL MFI', 'demo-il-mfi' ,'mifostenant-creocore'),
(2, 'Demo micro-bank MFI', 'demo-micro-bank-mfi', 'mifostenant-heavansfamilydotorg');