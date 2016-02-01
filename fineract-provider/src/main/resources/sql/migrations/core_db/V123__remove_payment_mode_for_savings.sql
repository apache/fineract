ALTER TABLE `m_charge`
	CHANGE COLUMN `charge_payment_mode_enum` `charge_payment_mode_enum` SMALLINT(5) NULL DEFAULT NULL AFTER `charge_calculation_enum`;