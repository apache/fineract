CREATE TABLE `m_document` (
	`id` INT(20) NOT NULL AUTO_INCREMENT,
	`parent_entity_type` VARCHAR(50) NOT NULL,
	`parent_entity_id` INT(20) NOT NULL DEFAULT '0',
	`name` VARCHAR(250) NOT NULL,
	`file_name` VARCHAR(250) NOT NULL,
	`size` INT(20) NULL DEFAULT '0',
	`type` VARCHAR(50) NULL DEFAULT NULL,
	`description` VARCHAR(1000) NULL DEFAULT NULL,
	`location` VARCHAR(500) NOT NULL DEFAULT '0',
	`createdby_id` INT(20) NOT NULL,
	`lastmodifiedby_id` INT(20) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`lastmodified_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`)
)
ENGINE=InnoDB;
