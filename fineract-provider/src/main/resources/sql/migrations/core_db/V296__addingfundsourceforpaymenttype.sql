ALTER TABLE `m_payment_type`
	ADD COLUMN `fundsourceaccountid` BIGINT NULL AFTER `order_position`,
	ADD CONSTRAINT `FK_m_payment_type_acc_gl_account` FOREIGN KEY (`fundsourceaccountid`) REFERENCES `acc_gl_account` (`id`);