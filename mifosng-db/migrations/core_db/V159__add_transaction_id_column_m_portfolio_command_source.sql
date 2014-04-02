ALTER TABLE `m_portfolio_command_source`
	ADD COLUMN `transaction_id` VARCHAR(100) NULL DEFAULT NULL AFTER `product_id`;