ALTER TABLE `m_product_loan`
	ADD COLUMN `can_define_fixed_emi_amount` TINYINT(1) NOT NULL DEFAULT '0',
	ADD COLUMN `instalment_amount_in_multiples_of` DECIMAL(19,6) NULL DEFAULT NULL;