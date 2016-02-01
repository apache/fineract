ALTER TABLE `m_charge`
	ADD COLUMN `fee_frequency` SMALLINT(5) NULL DEFAULT NULL AFTER `max_cap`;

CREATE TABLE `m_loan_overdue_installment_charge` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_charge_id` BIGINT(20) NOT NULL,
	`loan_schedule_id` BIGINT(20) NOT NULL,
	`frequency_number` INT(10) NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_m_loan_overdue_installment_charge_m_loan_charge` FOREIGN KEY (`loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
	CONSTRAINT `FK_m_loan_overdue_installment_charge_m_loan_repayment_schedule` FOREIGN KEY (`loan_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

INSERT INTO `c_configuration` (`name`, `value`, `enabled`) VALUES ('grace-on-penalty-posting', 0, 1);

INSERT INTO m_loan_overdue_installment_charge (`loan_charge_id`, `loan_schedule_id`, `frequency_number`) SELECT mlc.id ,ls.id ,1  from m_loan_charge as mlc inner join m_charge mc on mc.id = mlc.charge_id inner join m_loan_repayment_schedule ls on ls.loan_id = mlc.loan_id and ls.duedate = mlc.due_for_collection_as_of_date where mc.charge_time_enum = 9 and mlc.is_active = 1;