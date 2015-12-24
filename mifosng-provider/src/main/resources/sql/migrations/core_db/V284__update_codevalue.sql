ALTER TABLE `m_code_value`
	ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT '1' AFTER `code_score`;