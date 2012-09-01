
insert into m_code (code_name)
select name from stretchydata_allowed_list;

insert into m_code_value (code_id, code_value, order_position)
select c.id, sv.name, sv.id
from stretchydata_allowed_value sv
join stretchydata_allowed_list sl on sv.allowed_list_id = sl.id
join m_code c on c.code_name = sl.name;


SET foreign_key_checks = 0;

ALTER TABLE `stretchydata_dataset_fields` DROP FOREIGN KEY `stretchydata_dataset_fields_fk2` ;

ALTER TABLE `stretchydata_dataset_fields` 
CHANGE COLUMN `allowed_list_id` `code_id` INT(11) NULL DEFAULT NULL  , 
  ADD CONSTRAINT `stretchydata_dataset_fields_fk2`
  FOREIGN KEY (`code_id` )
  REFERENCES `m_code` (`id` )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

SET foreign_key_checks = 1;


update stretchydata_dataset_fields f
join stretchydata_allowed_list l on l.id = f.code_id
join m_code c on c.code_name = l.name
set code_id = c.id
where f.code_id is not null;


DROP TABLE `stretchydata_allowed_value`;
DROP TABLE `stretchydata_allowed_list`;