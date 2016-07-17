ALTER TABLE `m_loan_reschedule_request`
	DROP COLUMN `grace_on_principal`,
	DROP COLUMN `grace_on_interest`,
	DROP COLUMN `extra_terms`,
	DROP COLUMN `interest_rate`,
	DROP COLUMN `adjusted_due_date`;

CREATE TABLE `m_loan_reschedule_request_term_variations_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_reschedule_request_id` BIGINT(20) NOT NULL,
	`loan_term_variations_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK__m_loan_reschedule_request` (`loan_reschedule_request_id`),
	INDEX `FK__m_loan_term_variations` (`loan_term_variations_id`),
	CONSTRAINT `FK__m_loan_reschedule_request` FOREIGN KEY (`loan_reschedule_request_id`) REFERENCES `m_loan_reschedule_request` (`id`),
	CONSTRAINT `FK__m_loan_term_variations` FOREIGN KEY (`loan_term_variations_id`) REFERENCES `m_loan_term_variations` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

ALTER TABLE `m_loan_term_variations`
	ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT '1' AFTER `applied_on_loan_status`;
	
ALTER TABLE `m_loan_term_variations`
	ADD COLUMN `parent_id` BIGINT(20) NULL DEFAULT NULL AFTER `is_active`;

