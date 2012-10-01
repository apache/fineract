UPDATE `mifostenant-heavensfamily`.`m_loan`
SET
`loan_transaction_strategy_id` = 2
WHERE id > 0;

UPDATE `mifostenant-heavensfamily`.`m_loan`
SET
`term_frequency` = `repay_every` * `number_of_repayments`
WHERE `term_frequency` = 0;
