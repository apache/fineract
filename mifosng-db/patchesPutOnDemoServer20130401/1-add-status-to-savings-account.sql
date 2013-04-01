ALTER TABLE `m_savings_account` 
ADD COLUMN `status_enum` SMALLINT(5) NOT NULL DEFAULT 300 AFTER `product_id` ;

ALTER TABLE `m_savings_account` 
ADD COLUMN `activation_date` DATE DEFAULT NULL AFTER `status_enum`;

ALTER TABLE `m_savings_account` 
ADD COLUMN `lockedin_until_date_derived` DATE DEFAULT NULL AFTER `lockin_period_frequency_enum`;

ALTER TABLE `m_portfolio_command_source` 
ADD COLUMN `savings_id` BIGINT(20) NULL DEFAULT NULL  AFTER `loan_id` ;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES 
('transaction_savings', 'ACTIVATE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'ACTIVATE', '1'),
('transaction_savings', 'ACTIVATE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'ACTIVATE', '0');