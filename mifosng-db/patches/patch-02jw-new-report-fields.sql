ALTER TABLE `stretchy_report` 
ADD COLUMN `description` TEXT NULL DEFAULT NULL  AFTER `report_sql` , 
ADD COLUMN `core_report` TINYINT(1) NULL DEFAULT '0'  AFTER `description` , 
ADD COLUMN `use_report` TINYINT(1) NULL DEFAULT '0'  AFTER `core_report` ;