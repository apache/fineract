ALTER TABLE `m_holiday`
	ADD COLUMN `status_enum` INT(5) NOT NULL DEFAULT '100' AFTER `repayments_rescheduled_to`;

ALTER TABLE `m_holiday`
	DROP INDEX `holiday_name`,
	ADD UNIQUE INDEX `holiday_name` (`name`, `from_date`);

UPDATE `m_holiday` set `status_enum`=300;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'CREATE_HOLIDAY_CHECKER', 'HOLIDAY', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'ACTIVATE_HOLIDAY', 'HOLIDAY', 'ACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'ACTIVATE_HOLIDAY_CHECKER', 'HOLIDAY', 'ACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'UPDATE_HOLIDAY', 'HOLIDAY', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'UPDATE_HOLIDAY_CHECKER', 'HOLIDAY', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'DELETE_HOLIDAY', 'HOLIDAY', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'DELETE_HOLIDAY_CHECKER', 'HOLIDAY', 'DELETE', 0);
