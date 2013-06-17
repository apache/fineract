INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UNASSIGNSTAFF_CLIENT', 'CLIENT', 'UNASSIGNSTAFF', 0);
ALTER TABLE m_client
  ADD COLUMN `staff_id` BIGINT(20) NULL DEFAULT NULL AFTER `office_id`,
  ADD INDEX `client_staff_id` (`staff_id`),
  ADD CONSTRAINT `FK_m_client_m_staff` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`);