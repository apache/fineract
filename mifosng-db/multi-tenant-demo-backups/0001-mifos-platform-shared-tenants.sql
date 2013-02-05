-- create tenants table
DROP TABLE IF EXISTS `mifosplatform-tenants`.`tenants`;
CREATE TABLE `mifosplatform-tenants`.`tenants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `schema_name` varchar(100) NOT NULL,
  `schema_server` varchar(100) NOT NULL DEFAULT 'localhost',
  `schema_server_port` varchar(10) NOT NULL DEFAULT '3306',
  `schema_username` varchar(100) NOT NULL DEFAULT 'root',
  `schema_password` varchar(100) NOT NULL DEFAULT 'mysql',
  `timezone_id` int(11) DEFAULT NULL,
  `country_id` int(11) DEFAULT NULL,
  `joined_date` date DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `mifosplatform-tenants`.`tenants`
(`name`, `identifier`,`schema_name`)
VALUES
('Default Demo Tenant', 'default' ,'mifostenant-default'),
('API Docs Demo', 'apidocs' ,'mifostenant-apidocs'),
('GK Maarg', 'gk-maarg' ,'mifostenant-gk-maarg'),
('Latam Demo', 'latam' ,'mifostenant-latam'),
('Bare bones Demo', 'barebones' ,'mifostenant-barebones');