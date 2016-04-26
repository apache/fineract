ALTER TABLE `c_configuration`
	CHANGE COLUMN `name` `name` VARCHAR(100) NULL DEFAULT NULL AFTER `id`;
	
INSERT INTO `c_configuration` (`name`, `value`, `date_value`, `enabled`, `is_trap_door`, `description`) VALUES ( 'change-emi-if-repaymentdate-same-as-disbursementdate', 0, NULL, 1, 0, 'In tranche loans, if repayment date is same as tranche disbursement date then allow to change the emi amount');
