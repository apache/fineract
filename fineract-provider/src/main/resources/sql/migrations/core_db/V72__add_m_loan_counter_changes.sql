ALTER TABLE m_product_loan
 ADD COLUMN `include_in_borrower_cycle` TINYINT(1) NOT NULL DEFAULT '0';

CREATE TABLE `m_client_loan_counter` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL,
	`loan_product_id` BIGINT(20) NOT NULL,
	`loan_id` BIGINT(20) NOT NULL,
	`running_count` SMALLINT(4) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_client_id_loan_counter` (`client_id`),
	INDEX `FK_m_loan_product_loan_counter` (`loan_product_id`),
	INDEX `FK_m_client_loan_counter` (`loan_id`),
	CONSTRAINT `FK_m_client_id_loan_counter` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
	CONSTRAINT `FK_m_loan_product_loan_counter` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`),
	CONSTRAINT `FK_m_client_loan_counter` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;