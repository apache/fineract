ALTER TABLE m_permission
 DROP COLUMN `group_enum` , 
 ADD COLUMN `grouping` VARCHAR(45) NULL  AFTER `id` , 
 ADD COLUMN `order_in_grouping` INT NULL  AFTER `grouping`  , 
 ADD UNIQUE INDEX `code` (`code` ASC) ;