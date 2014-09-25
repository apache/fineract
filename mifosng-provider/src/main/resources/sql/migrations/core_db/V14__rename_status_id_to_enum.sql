
ALTER TABLE m_group CHANGE COLUMN `status_id` `status_enum` INT(5) NOT NULL DEFAULT '300'  ;

ALTER TABLE m_client CHANGE COLUMN `status_id` `status_enum` INT(5) NOT NULL DEFAULT '300'  ;

update r_enum_value
set enum_name = 'status_enum'
where enum_name = 'status_id';