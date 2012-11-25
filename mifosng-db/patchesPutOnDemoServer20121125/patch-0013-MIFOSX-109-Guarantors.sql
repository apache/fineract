CREATE TABLE `m_guarantor_external` (
	`loan_id` BIGINT(20) NOT NULL,
	`firstname` VARCHAR(50) NOT NULL,
	`lastname` VARCHAR(50) NOT NULL,
	`dob` DATE NULL,
	`address_line_1` VARCHAR(500) NULL,
	`address_line_2` VARCHAR(500) NULL,
	`city` VARCHAR(50) NULL,
	`state` VARCHAR(50) NULL,
	`country` VARCHAR(50) NULL,
	`zip` VARCHAR(20) NULL,
	`house_phone_number` VARCHAR(20) NULL,
	`mobile_number` VARCHAR(20) NULL,
	`comment` VARCHAR(20) NULL,
	PRIMARY KEY (`loan_id`),
	CONSTRAINT `FK_m_guarantor_m_loan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `m_loan`
	ADD COLUMN `guarantor_id` BIGINT(20) NULL DEFAULT NULL AFTER `loan_officer_id`,
	ADD CONSTRAINT `FK_m_loan_guarantor` FOREIGN KEY (`guarantor_id`) REFERENCES `m_client` (`id`);
	
insert into x_registered_table values ('m_guarantor_external','m_loan');