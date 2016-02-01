ALTER TABLE `m_loan`
	CHANGE COLUMN `repayment_frequency_nth_day_enum` `repayment_frequency_nth_day_enum` SMALLINT(5) NULL DEFAULT '0' AFTER `repayment_period_frequency_enum`,
	CHANGE COLUMN `repayment_frequency_day_of_week_enum` `repayment_frequency_day_of_week_enum` SMALLINT(5) NULL DEFAULT '0' AFTER `repayment_frequency_nth_day_enum`;
