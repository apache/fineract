ALTER TABLE `mifosngprovider`.`portfolio_product_loan` add column `interest_calculated_in_period_enum` smallint(5) NOT NULL DEFAULT 1;

ALTER TABLE `mifosngprovider`.`portfolio_loan` add column `interest_calculated_in_period_enum` smallint(5) NOT NULL DEFAULT 1;