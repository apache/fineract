ALTER TABLE `acc_accounting_rule`
 ADD COLUMN `allow_multiple_debits` TINYINT(1) NOT NULL DEFAULT '0' AFTER `debit_account_id`,
 ADD COLUMN `allow_multiple_credits` TINYINT(1) NOT NULL DEFAULT '0' AFTER `credit_account_id`;

ALTER TABLE acc_rule_tags
 ADD UNIQUE KEY `UNIQUE_ACCOUNT_RULE_TAGS` (`acc_rule_id`,`tag_id`,`acc_type_enum`);