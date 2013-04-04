ALTER TABLE `m_savings_product` 
DROP COLUMN `nominal_interest_rate_period_frequency_enum`,
CHANGE COLUMN `nominal_interest_rate_per_period` `nominal_annual_interest_rate` DECIMAL(19,6) NOT NULL, 
CHANGE COLUMN `interest_period_enum` `interest_compounding_period_enum` SMALLINT(5) NOT NULL;


ALTER TABLE `m_savings_account` 
DROP COLUMN `annual_nominal_interest_rate`, 
DROP COLUMN `nominal_interest_rate_period_frequency_enum`, 
CHANGE COLUMN `nominal_interest_rate_per_period` `nominal_annual_interest_rate` DECIMAL(19,6) NOT NULL, 
CHANGE COLUMN `interest_period_enum` `interest_compounding_period_enum` SMALLINT(5) NOT NULL;