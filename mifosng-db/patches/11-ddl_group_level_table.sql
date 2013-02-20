CREATE TABLE `m_group_level` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
`parent_id` INT(11) NULL DEFAULT NULL,
`is_super_parent` TINYINT(1) NOT NULL,
`level_name` VARCHAR(100) NOT NULL,
`recursable` TINYINT(1) NOT NULL,
`can_have_clients` TINYINT(1) NOT NULL,
PRIMARY KEY (`id`),
INDEX `Parent_levelId_reference` (`parent_id`),
CONSTRAINT `Parent_levelId_reference` FOREIGN KEY (`parent_id`) REFERENCES `m_group_level` (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `m_group_level` (`id`, `parent_id`, `is_super_parent`, `level_name`, `recursable`, `can_have_clients`) VALUES (1, NULL, 1, 'Center', 1, 0);
INSERT INTO `m_group_level` (`id`, `parent_id`, `is_super_parent`, `level_name`, `recursable`, `can_have_clients`) VALUES (2, 1, 0, 'Group', 0, 1);

ALTER TABLE m_group DROP FOREIGN KEY `FK_m_group_m_staff`;
ALTER TABLE m_group CHANGE `loan_officer_id` `staff_id` BIGINT(20) NULL DEFAULT NULL;
ALTER TABLE m_group ADD COLUMN `level_Id` INT(11) NOT NULL AFTER `office_id`;
ALTER TABLE m_group ADD COLUMN `parent_id` BIGINT(20) NULL DEFAULT NULL AFTER `level_Id`;
ALTER TABLE m_group ADD COLUMN `hierarchy` VARCHAR(100) NULL DEFAULT NULL AFTER `parent_id`;
ALTER TABLE m_group ADD CONSTRAINT `FK_m_group_m_staff` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`);
ALTER TABLE m_group ADD CONSTRAINT `Parent_Id_reference` FOREIGN KEY (`parent_id`) REFERENCES `m_group` (`id`);
UPDATE m_group mg SET mg.level_Id = 1;
ALTER TABLE m_group ADD CONSTRAINT `FK_m_group_level` FOREIGN KEY (`level_Id`) REFERENCES `m_group_level` (`id`);