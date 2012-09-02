
insert into m_permission (group_enum, code, default_description,  default_name)
select 3, concat("CAN_RUN_", report_name), concat("CAN_RUN_", report_name), concat("CAN_RUN_", report_name)
from stretchy_report r
where not exists
(select 'f' from m_permission x
where x.code = concat("CAN_RUN_", r.report_name));

insert into m_permission (group_enum, code, default_description,  default_name)
select 3, concat("CAN_READ_", t.`name`, "_x", d.`name`), concat("CAN_READ_", t.`name`, "_x", d.`name`), concat("CAN_READ_", t.`name`, "_x", d.`name`)
from stretchydata_dataset d 
join stretchydata_datasettype t on t.id = d.datasettype_id
where not exists
(select 'f' from m_permission x
where x.code = concat("CAN_READ_", t.`name`, "_x", d.`name`));

insert into m_permission (group_enum, code, default_description,  default_name)
select 3, concat("CAN_UPDATE_", t.`name`, "_x", d.`name`), concat("CAN_UPDATE_", t.`name`, "_x", d.`name`), concat("CAN_UPDATE_", t.`name`, "_x", d.`name`)
from stretchydata_dataset d 
join stretchydata_datasettype t on t.id = d.datasettype_id
where not exists
(select 'f' from m_permission x
where x.code = concat("CAN_UPDATE_", t.`name`, "_x", d.`name`));
