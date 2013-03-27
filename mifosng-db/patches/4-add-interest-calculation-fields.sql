ALTER TABLE `m_savings_product` 
ADD COLUMN `interest_period_enum` SMALLINT(5) NOT NULL AFTER `nominal_interest_rate_period_frequency_enum`, 
ADD COLUMN `interest_calculation_type_enum` SMALLINT(5) NOT NULL AFTER `interest_period_enum`;


ALTER TABLE `m_savings_account` 
ADD COLUMN `interest_period_enum` SMALLINT(5) NOT NULL AFTER `annual_nominal_interest_rate`, 
ADD COLUMN `interest_calculation_type_enum` SMALLINT(5) NOT NULL AFTER `interest_period_enum`;


INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES 
('transaction_savings', 'CALCULATEINTEREST_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'CALCULATEINTEREST', '1'),
('transaction_savings', 'CALCULATEINTEREST_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'CALCULATEINTEREST', '0');


ALTER TABLE `m_savings_account_transaction`
ADD COLUMN `running_balance_derived` DECIMAL(19,6) NULL AFTER `is_reversed`,
ADD COLUMN `balance_number_of_days_derived` INT NULL AFTER `running_balance_derived`,
ADD COLUMN `balance_end_date_derived` DATE NULL AFTER `balance_number_of_days_derived`,
ADD COLUMN `cumulative_balance_derived` DECIMAL(19,6) NULL AFTER `balance_end_date_derived`;

ALTER TABLE `m_savings_account` 
ADD COLUMN `total_interest_earned_derived` DECIMAL(19,6) DEFAULT NULL AFTER `total_withdrawals_derived`;


ALTER TABLE `m_savings_product` 
ADD COLUMN `interest_calculation_days_in_year_type_enum` SMALLINT(5) NOT NULL AFTER `interest_calculation_type_enum`;

ALTER TABLE `m_savings_account` 
ADD COLUMN `interest_calculation_days_in_year_type_enum` SMALLINT(5) NOT NULL AFTER `interest_calculation_type_enum`;