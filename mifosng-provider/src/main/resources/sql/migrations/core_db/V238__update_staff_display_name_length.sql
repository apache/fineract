ALTER TABLE `m_staff`
	ALTER `display_name` DROP DEFAULT;
ALTER TABLE `m_staff`
	CHANGE COLUMN `display_name` `display_name` VARCHAR(102) NOT NULL AFTER `lastname`;
