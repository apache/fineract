ALTER TABLE `portfolio_product_savings`
ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT '0' AFTER `interest_rate`;