CREATE TABLE `m_client_identifier` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL,
	`document_type_id` INT(11) NOT NULL,
	`document_key` VARCHAR(500) NOT NULL,
	`description` VARCHAR(1000) NULL DEFAULT NULL,
	`createdby_id` BIGINT(20) NULL DEFAULT NULL,
	`lastmodifiedby_id` BIGINT(20) NULL DEFAULT NULL,
	`created_date` DATETIME NULL DEFAULT NULL,
	`lastmodified_date` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_client_document_m_client` (`client_id`),
	INDEX `FK_m_client_document_m_code_value` (`document_type_id`),
	CONSTRAINT `FK_m_client_document_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
	CONSTRAINT `FK_m_client_document_m_code_value` FOREIGN KEY (`document_type_id`) REFERENCES `m_code_value` (`id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

insert into m_code values(9,'Customer Identifier');

insert into m_code_value values (null,'9','Driving Licence','1');
insert into m_code_value values (null,'9','Passport','2');
insert into m_code_value values (null,'9','PAN Card','3');
insert into m_code_value values (null,'9','Ration Card','4');
insert into m_code_value values (null,'9','Other','5');