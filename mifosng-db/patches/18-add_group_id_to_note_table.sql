ALTER TABLE `m_note`
	ADD COLUMN `group_id` BIGINT(20) NULL AFTER `client_id`,
	ADD CONSTRAINT `FK_m_note_m_group` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`);
	
ALTER TABLE `m_note`
	ALTER `client_id` DROP DEFAULT;
ALTER TABLE `m_note`
	CHANGE COLUMN `client_id` `client_id` BIGINT(20) NULL AFTER `id`;	