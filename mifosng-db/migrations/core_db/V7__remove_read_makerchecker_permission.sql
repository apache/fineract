
/*
Remove READ_MAKERCHECKER permission as no longer used.
*/

delete from m_role_permission 
where exists
(select 'f' from m_permission p 
where p.id = m_role_permission.permission_id and p.code = 'READ_MAKERCHECKER');


delete from m_permission 
where code = 'READ_MAKERCHECKER';

	