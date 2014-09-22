CREATE TABLE `x_table_cloumn_code_mappings` (
	`column_alias_name` VARCHAR(50) NOT NULL,
	`code_id` INT(10) NOT NULL,
	PRIMARY KEY (`column_alias_name`),
	INDEX `FK_x_code_id` (`code_id`),
	CONSTRAINT `FK_x_code_id` FOREIGN KEY (`code_id`) REFERENCES `m_code` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

INSERT INTO `c_configuration` (`name`, `enabled`) VALUES ('constraint_approach_for_datatables', 1);
