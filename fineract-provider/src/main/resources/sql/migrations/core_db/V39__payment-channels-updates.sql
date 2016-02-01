INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES
('PaymentType',1);

/*Update payment detail to add foreign key relationship to user defined Code Value*/
update m_loan_transaction set payment_detail_id=null;
ALTER TABLE `m_payment_detail`
	ALTER `payment_type_enum` DROP DEFAULT;
ALTER TABLE `m_payment_detail`
	CHANGE COLUMN `payment_type_enum` `payment_type_cv_id` INT(11) NULL AFTER `id`;
delete from m_payment_detail;
ALTER TABLE `m_payment_detail`
	ADD CONSTRAINT `FK_m_payment_detail_m_code_value` FOREIGN KEY (`payment_type_cv_id`) REFERENCES `m_code_value` (`id`);

/*Map Different Payment Channels to payment Types*/
ALTER TABLE `acc_product_mapping`
	ADD COLUMN `payment_type` INT(11) NULL DEFAULT NULL AFTER `product_type`,
	ADD CONSTRAINT `FK_acc_product_mapping_m_code_value` FOREIGN KEY (`payment_type`) REFERENCES `m_code_value` (`id`);
