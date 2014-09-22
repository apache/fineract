ALTER TABLE `acc_product_mapping`
	ADD COLUMN `charge_id` BIGINT(20) NULL DEFAULT NULL AFTER `payment_type`,
	ADD CONSTRAINT `FK_acc_product_mapping_m_charge` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`);