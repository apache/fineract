ALTER TABLE `m_appuser`
ADD COLUMN `staff_id` BIGINT(20) NULL DEFAULT NULL AFTER `office_id` ;

ALTER TABLE m_appuser
ADD CONSTRAINT `fk_m_appuser_002`
FOREIGN KEY (`staff_id`)
REFERENCES m_staff (`id`)
ON DELETE NO ACTION
ON UPDATE NO ACTION
,ADD INDEX `fk_m_appuser_002x` (`staff_id` ASC);

ALTER TABLE `m_staff`
ADD COLUMN `organisational_role_enum` SMALLINT NULL DEFAULT NULL AFTER `external_id`,
ADD COLUMN `organisational_role_parent_staff_id` BIGINT(20) NULL DEFAULT NULL AFTER `organisational_role_enum`;
