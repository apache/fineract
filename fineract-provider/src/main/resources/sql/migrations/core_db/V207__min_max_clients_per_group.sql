ALTER TABLE `m_group_level`
	ADD COLUMN `min_clients` INT(11) NOT NULL DEFAULT '0' AFTER `can_have_clients`,
	ADD COLUMN `max_clients` INT(11) NOT NULL DEFAULT '0' AFTER `min_clients`;