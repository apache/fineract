ALTER TABLE m_group ADD COLUMN `account_no` VARCHAR(20) NOT NULL;
UPDATE m_group set account_no = lpad(id,9,0);