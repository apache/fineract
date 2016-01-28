ALTER TABLE `m_staff`
	CHANGE COLUMN `is_active` `is_active` TINYINT(1) NOT NULL DEFAULT '1' AFTER `organisational_role_parent_staff_id`;
UPDATE `m_staff` SET `is_active`=1;