ALTER TABLE `portfolio_product_savings` 
ADD COLUMN `currency_code` VARCHAR(3) NULL  AFTER `description` , 
ADD COLUMN `currency_digits` SMALLINT(5) NULL  AFTER `currency_code` , 
ADD COLUMN `interest_rate` DECIMAL(19,6) NULL  AFTER `currency_digits` ;