create table `acc_rule_tags` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`acc_rule_id` BIGINT(20) NOT NULL,
	`tag_id` INT(11) NOT NULL,
	`acc_type_enum` SMALLINT(5) NOT NULL,
	primary key(`id`),
	INDEX `FK_acc_accounting_rule_id` (`acc_rule_id`),
	INDEX `FK_m_code_value_id` (`tag_id`),
	CONSTRAINT `FK_acc_accounting_rule_id` FOREIGN KEY (`acc_rule_id`) REFERENCES `acc_accounting_rule` (`id`),
	CONSTRAINT `FK_m_code_value_id` FOREIGN KEY (`tag_id`) REFERENCES `m_code_value` (`id`)
);