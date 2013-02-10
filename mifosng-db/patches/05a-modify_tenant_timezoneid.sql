-- modified patch to update column (as nullable first), populate all tenants with indian timezone as default and then set timezone as not null for tenants
USE `mifosplatform-tenants`;
-- timezone are refferd with names rather than numerics
ALTER TABLE `mifosplatform-tenants`.`tenants` CHANGE COLUMN `timezone_id` `timezone_id` VARCHAR(100) NULL DEFAULT NULL;
-- setting Indian time zone
UPDATE `tenants` SET `timezone_id`='Asia/Kolkata' WHERE `id`>0;
ALTER TABLE `mifosplatform-tenants`.`tenants` CHANGE COLUMN `timezone_id` `timezone_id` VARCHAR(100) NOT NULL;