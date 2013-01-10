ALTER TABLE `m_product_loan`
	ADD COLUMN `accounting_type` SMALLINT(5) NOT NULL AFTER `amortization_method_enum`;

update m_product_loan set accounting_type=1;

DROP TABLE if exists `acc_product_mapping`;

CREATE TABLE `acc_product_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`gl_account_id` BIGINT(20) NULL DEFAULT NULL,
	`product_id` BIGINT(20) NULL DEFAULT NULL,
	`product_type` SMALLINT(5) NULL DEFAULT NULL,
	`financial_account_type` SMALLINT(5) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;


	
	