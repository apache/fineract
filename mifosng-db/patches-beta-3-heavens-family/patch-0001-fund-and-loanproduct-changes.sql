-- create fund table
DROP TABLE IF EXISTS `mifosngprovider`.`org_fund`;
CREATE TABLE `org_fund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `org_id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fund_name_org` (`org_id`,`name`),
  UNIQUE KEY `fund_externalid_org` (`org_id`,`external_id`),
  KEY `FK4E56DB40541F0A56` (`createdby_id`),
  KEY `FK4E56DB4054DF2770` (`org_id`),
  KEY `FK4E56DB408F889C3F` (`lastmodifiedby_id`),
  CONSTRAINT `FK4E56DB408F889C3F` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FK4E56DB40541F0A56` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FK4E56DB4054DF2770` FOREIGN KEY (`org_id`) REFERENCES `org_organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- example of dropping fund column/index/foreign key 
-- alter table `mifosngprovider`.`portfolio_product_loan` 
-- DROP FOREIGN KEY `FKA6A8A7D77240145`,
-- DROP INDEX `FKA6A8A7D77240145`,
-- drop column `fund_id`;

-- associate fund table with loan products making it fund optional at database level
alter table `mifosngprovider`.`portfolio_product_loan`
add column `fund_id` bigint(20) DEFAULT NULL,
add KEY `FKA6A8A7D77240145` (`fund_id`),
add CONSTRAINT `FKA6A8A7D77240145` FOREIGN KEY (`fund_id`) REFERENCES `org_fund` (`id`);