/* remove column org_id */
ALTER TABLE `portfolio_client` DROP FOREIGN KEY `FKCE00CAB354DF2770`;
ALTER TABLE `portfolio_client` 
	DROP COLUMN `org_id` , 
	DROP INDEX `org_id` , 
	ADD INDEX `external_id` (`external_id` ASC) , 
	DROP INDEX `FKCE00CAB354DF2770`;