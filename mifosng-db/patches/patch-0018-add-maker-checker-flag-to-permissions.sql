
ALTER TABLE `m_permission` 
 DROP COLUMN `default_name` , 
 DROP COLUMN `default_description` , 
 ADD COLUMN `is_maker_checker` TINYINT(1) NOT NULL AFTER `action_name` ;

update m_permission
set is_maker_checker = false;

update m_permission
set is_maker_checker = true
where grouping not in ('special', 'report')
and action_name != 'READ';