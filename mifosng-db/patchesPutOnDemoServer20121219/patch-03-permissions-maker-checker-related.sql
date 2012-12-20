
ALTER TABLE m_permission
 CHANGE COLUMN `is_maker_checker` `can_maker_checker` TINYINT(1) NOT NULL DEFAULT '1'  ;

/*
add all checker permissions
*/
insert into m_permission(grouping, code, entity_name, action_name, can_maker_checker)
select grouping, concat(code, '_CHECKER'), entity_name, action_name, false  
from m_permission
where code not like 'READ_%'
and grouping != 'special';


/* 5k not enough for command json */
ALTER TABLE m_portfolio_command_source
 CHANGE COLUMN `command_as_json` `command_as_json` TEXT NOT NULL  ;


