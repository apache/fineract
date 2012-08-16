CREATE TABLE `o_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `amount` decimal(19,6) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `portfolio_product_loan` 
ADD COLUMN `charge_id` BIGINT(20) NULL DEFAULT NULL  AFTER `fund_id` ,    
ADD CONSTRAINT `FK_o_charge`   FOREIGN KEY (`charge_id` )   
REFERENCES `mifostenant_testddl`.`o_charge` (`id` )   ON DELETE NO ACTION   ON UPDATE NO ACTION , 
ADD INDEX `FK_o_charge_idx` (`charge_id` ASC) ;

