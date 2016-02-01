/*add accounting type field to savings product*/
ALTER TABLE `m_savings_product`
	ADD COLUMN `accounting_type` SMALLINT(5) NOT NULL AFTER `lockin_period_frequency_enum`;

/*update existing savings products to have "No" accounting*/
update m_savings_product set accounting_type=1;

/*track payment details for savings transactions*/
ALTER TABLE `m_savings_account_transaction`
ADD COLUMN `payment_detail_id` BIGINT(20) NULL DEFAULT NULL AFTER `savings_account_id`,
ADD CONSTRAINT `FK_m_savings_account_transaction_m_payment_detail` FOREIGN KEY (`payment_detail_id`) REFERENCES `m_payment_detail` (`id`);
