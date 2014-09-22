CREATE TABLE `m_working_days` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`recurrence` VARCHAR(100) NULL DEFAULT NULL,
	`repayment_rescheduling_enum` SMALLINT(5) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1;

INSERT INTO `m_working_days` (`recurrence`, `repayment_rescheduling_enum`) VALUES ('FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA', 2);