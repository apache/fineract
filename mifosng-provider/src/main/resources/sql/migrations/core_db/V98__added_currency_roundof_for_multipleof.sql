ALTER TABLE `m_savings_account`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `currency_digits`;
ALTER TABLE `m_savings_product`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `currency_digits`;
ALTER TABLE `m_loan`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `currency_digits`;
ALTER TABLE `m_product_loan`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `currency_digits`;
ALTER TABLE `m_currency`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `decimal_places`;
ALTER TABLE `m_organisation_currency`
	ADD COLUMN `currency_multiplesof` SMALLINT(5) NULL AFTER `decimal_places`;