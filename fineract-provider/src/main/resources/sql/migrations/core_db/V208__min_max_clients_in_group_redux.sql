INSERT INTO `c_configuration` (`id`, `name`,`value`, `enabled`, `description`) 
VALUES (NULL, 'min-clients-in-group', '5', '0',"Minimum number of Clients that a Group should have");

INSERT INTO `c_configuration` (`id`, `name`, `value`, `enabled`, `description`)
VALUES (NULL, 'max-clients-in-group', '5', '0', "Maximum number of Clients that a Group can have");

ALTER TABLE `m_group_level`
	DROP COLUMN `min_clients`,
	DROP COLUMN `max_clients`;
