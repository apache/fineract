ALTER TABLE `m_loan` 
 ADD COLUMN `repayment_frequency_nth_day_enum` SMALLINT(5) NOT NULL DEFAULT '0' AFTER `repayment_period_frequency_enum`,
 ADD COLUMN `repayment_frequency_day_of_week_enum` SMALLINT(5) NOT NULL DEFAULT '0' AFTER `repayment_frequency_nth_day_enum`;
