/* remove column org_id */

ALTER TABLE `org_office` DROP FOREIGN KEY `FK2291C47754DF2770`;
ALTER TABLE `org_office` DROP COLUMN `org_id`,
DROP INDEX `name_org`, 
ADD UNIQUE INDEX `name_org` (`name` ASC), 
DROP INDEX `externalid_org`, 
ADD UNIQUE INDEX `externalid_org` (`external_id` ASC), 
DROP INDEX `FK2291C47754DF2770`;

ALTER TABLE `portfolio_client` DROP FOREIGN KEY `FKCE00CAB354DF2770`;
ALTER TABLE `portfolio_client` 
	DROP COLUMN `org_id` , 
	DROP INDEX `org_id` , 
	ADD UNIQUE INDEX `external_id` (`external_id` ASC) , 
	DROP INDEX `FKCE00CAB354DF2770`;

ALTER TABLE `portfolio_client` 
  DROP INDEX `external_id`,
	ADD UNIQUE INDEX `external_id` (`external_id` ASC);


ALTER TABLE `org_fund` DROP FOREIGN KEY `FK4E56DB4054DF2770` ;
ALTER TABLE `org_fund` DROP COLUMN `org_id` , DROP INDEX `fund_name_org` , ADD UNIQUE INDEX `fund_name_org` (`name` ASC) , DROP INDEX `fund_externalid_org` , ADD UNIQUE INDEX `fund_externalid_org` (`external_id` ASC) , DROP INDEX `FK4E56DB4054DF2770` ;

ALTER TABLE `org_organisation_currency` DROP FOREIGN KEY `FK308062BB54DF2770` ;ALTER TABLE `org_organisation_currency` DROP COLUMN `org_id` , DROP INDEX `FK308062BB54DF2770` ;

ALTER TABLE `admin_appuser` DROP FOREIGN KEY `FKB3D587C54DF2770` ;ALTER TABLE `admin_appuser` DROP COLUMN `org_id` , DROP INDEX `username_org` , ADD UNIQUE INDEX `username_org` (`username` ASC) , DROP INDEX `FKB3D587C54DF2770` ;

ALTER TABLE `admin_permission` DROP FOREIGN KEY `FK18040BF54DF2770` ;ALTER TABLE `admin_permission` DROP COLUMN `org_id` , DROP INDEX `FK18040BF54DF2770` ;

ALTER TABLE `admin_role` DROP FOREIGN KEY `FK2902EF6654DF2770` ;ALTER TABLE `admin_role` DROP COLUMN `org_id` , DROP INDEX `FK2902EF6654DF2770`;

ALTER TABLE `portfolio_loan` DROP FOREIGN KEY `FKB6F935D854DF2770` ;ALTER TABLE `portfolio_loan` DROP COLUMN `org_id` , DROP INDEX `org_id` , ADD UNIQUE INDEX `org_id` (`external_id` ASC) , DROP INDEX `FKB6F935D854DF2770` ;

ALTER TABLE `portfolio_loan_repayment_schedule` DROP COLUMN `org_id`;

ALTER TABLE `portfolio_loan_transaction` DROP COLUMN `org_id`;

ALTER TABLE `portfolio_note` DROP FOREIGN KEY `FK7C9708954DF2770` ;ALTER TABLE `portfolio_note` DROP COLUMN `org_id` , DROP INDEX `FK7C9708954DF2770`;

ALTER TABLE `portfolio_product_loan` DROP FOREIGN KEY `FK1BD0772854DF2770` ;ALTER TABLE `portfolio_product_loan` DROP COLUMN `org_id` , DROP INDEX `FK1BD0772854DF2770`;

drop table `admin_oauth_consumer_application`;

drop table `org_organisation`;









