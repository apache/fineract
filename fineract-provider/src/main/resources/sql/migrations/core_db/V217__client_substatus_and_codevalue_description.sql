
ALTER TABLE `m_client`
	ADD COLUMN `sub_status` INT(11) NULL DEFAULT NULL AFTER `status_enum`,
	ADD CONSTRAINT `FK_m_client_substatus_m_code_value` FOREIGN KEY (`sub_status`) REFERENCES `m_code_value` (`id`);
	
	
INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('ClientSubStatus', 1);


ALTER TABLE `m_code_value`
	ADD COLUMN `code_description` VARCHAR(500) NULL DEFAULT NULL AFTER `code_value`;

