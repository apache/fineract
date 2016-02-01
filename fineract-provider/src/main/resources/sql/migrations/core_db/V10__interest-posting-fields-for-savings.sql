INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES
('transaction_savings', 'POSTINTEREST_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'POSTINTEREST', '1'),
('transaction_savings', 'POSTINTEREST_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'POSTINTEREST', '0');


ALTER TABLE `m_savings_product`
ADD COLUMN `interest_posting_period_enum` SMALLINT(5) NOT NULL DEFAULT 4 AFTER `interest_compounding_period_enum`;


ALTER TABLE `m_savings_account`
ADD COLUMN `interest_posting_period_enum` SMALLINT(5) NOT NULL DEFAULT 4 AFTER `interest_compounding_period_enum`;