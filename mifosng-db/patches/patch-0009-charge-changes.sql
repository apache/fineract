ALTER TABLE `m_charge` 
ADD COLUMN `currency_code` VARCHAR(3) NOT NULL  AFTER `name` , 
ADD COLUMN `charge_applies_to_enum` SMALLINT(5) NOT NULL  AFTER `currency_code` , 
ADD COLUMN `charge_time_enum` SMALLINT(5) NOT NULL  AFTER `charge_applies_to_enum` , 
ADD COLUMN `charge_calculation_enum` SMALLINT(5) NOT NULL  AFTER `charge_time_enum` , 
ADD COLUMN `is_active` TINYINT(1) NOT NULL  AFTER `amount` ;


CREATE TABLE `m_product_loan_charge` (
  `product_loan_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_loan_id`, `charge_id`),
  KEY `charge_id` (`charge_id`),
  CONSTRAINT `m_product_loan_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_product_loan_charge_ibfk_2` FOREIGN KEY (`product_loan_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;