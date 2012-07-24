DROP TABLE IF EXISTS `portfolio_product_savings`;
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