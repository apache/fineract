ALTER TABLE `m_interest_rate_slab`
	CHANGE COLUMN `from_period` `from_period` INT(11) NULL DEFAULT NULL,
	CHANGE COLUMN `period_type_enum` `period_type_enum` SMALLINT(5) NULL DEFAULT NULL ;
	
ALTER TABLE `m_interest_rate_chart`
	ADD COLUMN `is_primary_grouping_by_amount` TINYINT(1) NOT NULL DEFAULT '0';
	
ALTER TABLE `m_savings_account_interest_rate_chart`
	ADD COLUMN `is_primary_grouping_by_amount` TINYINT NOT NULL DEFAULT '0'	;
	
ALTER TABLE `m_savings_account_interest_rate_slab`
	CHANGE COLUMN `period_type_enum` `period_type_enum` SMALLINT(5) NULL DEFAULT NULL,
	CHANGE COLUMN `from_period` `from_period` INT(11) NULL DEFAULT NULL;	