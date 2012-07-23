-- add extra columns to externalise hard-coded tenant information

ALTER TABLE `mifosplatform-tenants`.`tenants` 
ADD COLUMN `schema_server` varchar(100) NOT NULL DEFAULT 'localhost';

ALTER TABLE `mifosplatform-tenants`.`tenants` 
ADD COLUMN `schema_server_port` varchar(10) NOT NULL DEFAULT '3306';

ALTER TABLE `mifosplatform-tenants`.`tenants` 
ADD COLUMN `schema_username` varchar(100) NOT NULL DEFAULT 'root';

ALTER TABLE `mifosplatform-tenants`.`tenants` 
ADD COLUMN `schema_password` varchar(100) NOT NULL DEFAULT 'mysql';
