ALTER TABLE `acc_gl_account`
ADD COLUMN `tag_id` INT(11) NULL DEFAULT NULL AFTER `classification_enum`,
ADD INDEX `FKGLACC000000002` (`tag_id`),
ADD CONSTRAINT `FKGLACC000000002` FOREIGN KEY (`tag_id`) REFERENCES `m_code_value` (`id`);
INSERT into m_code(`code_name`,`is_system_defined`) values ('AssetAccountTags',1),('LiabilityAccountTags',1),('EquityAccountTags',1),('IncomeAccountTags',1),('ExpenseAccountTags',1);