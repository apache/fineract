ALTER TABLE m_holiday
MODIFY COLUMN repayments_rescheduled_to DATETIME NULL DEFAULT NULL,
ADD COLUMN `rescheduling_type` INT(5) NOT NULL DEFAULT '2';