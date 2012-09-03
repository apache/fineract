
insert into m_permission (group_enum, code, default_description, default_name)
values (0, "ALL_FUNCTIONS", "An application user will have permission to execute all tasks.", "ALL");


insert into m_permission (group_enum, code, default_description, default_name)
values (0, "ALL_FUNCTIONS_READ", "An application user will have permission to execute all read tasks.", "ALL READ");


/* give default super user role the all_functions permissions as well as current ones for now */
insert into m_role_permission (permission_id, role_id)
select p.id, 1
from m_permission p
where p.code = "ALL_FUNCTIONS";