/**Add currency code**/
ALTER TABLE `acc_gl_journal_entry`
		ADD COLUMN `currency_code` VARCHAR(3) NULL DEFAULT NULL AFTER `reversal_id`;


/**Update currency codes for loans**/
UPDATE acc_gl_journal_entry journal_entry SET currency_code = (
SELECT m_loan.currency_code
FROM m_loan, m_loan_transaction
WHERE m_loan.id = m_loan_transaction.loan_id AND m_loan_transaction.id=journal_entry.transaction_id)
WHERE journal_entry.entity_type_enum=1;


/**Update currency codes for savings**/
UPDATE acc_gl_journal_entry journal_entry SET currency_code = (
SELECT m_savings_account.currency_code
FROM m_savings_account, m_savings_account_transaction
WHERE m_savings_account.id = m_savings_account_transaction.savings_account_id AND m_savings_account_transaction.id=journal_entry.transaction_id)
WHERE journal_entry.entity_type_enum=2;

/**Update currency codes for manual journal entries***/
update acc_gl_journal_entry set currency_code = (select code from m_organisation_currency limit 1)
where acc_gl_journal_entry.currency_code is NULL;

/**Make currency code not null**/
ALTER TABLE `acc_gl_journal_entry`
	ALTER `currency_code` DROP DEFAULT;
ALTER TABLE `acc_gl_journal_entry`
	CHANGE COLUMN `currency_code` `currency_code` VARCHAR(3) NOT NULL AFTER `reversal_id`;