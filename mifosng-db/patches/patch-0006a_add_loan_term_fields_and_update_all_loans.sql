ALTER TABLE `portfolio_loan` ADD COLUMN `term_frequency` smallint(5) NOT NULL DEFAULT 0 AFTER `interest_calculated_in_period_enum`;
ALTER TABLE `portfolio_loan` ADD COLUMN `term_period_frequency_enum` smallint(5) NOT NULL DEFAULT 2 AFTER `term_frequency`;


UPDATE `mifostenant-default`.`portfolio_loan`
SET
`term_frequency` = `repay_every` * `number_of_repayments`,
`term_period_frequency_enum` = `repayment_period_frequency_enum`
WHERE id > 0;