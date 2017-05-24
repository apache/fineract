ALTER TABLE `m_provisioning_criteria`
	ADD COLUMN `provisioning_amount_type` INT NOT NULL DEFAULT '2' AFTER `lastmodified_date`;