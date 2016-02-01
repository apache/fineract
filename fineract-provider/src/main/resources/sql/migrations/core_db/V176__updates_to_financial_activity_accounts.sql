ALTER TABLE acc_gl_office_mapping DROP FOREIGN KEY `FK_office_mapping_office`;

ALTER TABLE acc_gl_office_mapping DROP column office_id;

ALTER TABLE `acc_gl_office_mapping`
	ALTER `financial_account_type` DROP DEFAULT;

ALTER TABLE `acc_gl_office_mapping`
	CHANGE COLUMN `financial_account_type` `financial_activity_type` SMALLINT(5) NOT NULL;

ALTER TABLE `acc_gl_office_mapping`
	ADD UNIQUE INDEX `financial_activity_type` (`financial_activity_type`);


RENAME TABLE `acc_gl_office_mapping` TO `acc_gl_financial_activity_account`;