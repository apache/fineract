CREATE TABLE `m_account_transfer_standing_instructions_history` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`standing_instruction_id` BIGINT(20) NOT NULL,
	`status` VARCHAR(20) NOT NULL,
	`execution_time` DATETIME NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`error_log` VARCHAR(500) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_account_transfer_standing_instructions_history` (`standing_instruction_id`),
	CONSTRAINT `FK_m_account_transfer_standing_instructions_m_history` FOREIGN KEY (`standing_instruction_id`) REFERENCES `m_account_transfer_standing_instructions` (`id`)
);
