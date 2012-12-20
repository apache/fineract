

DELETE FROM m_role_permission
where exists
(select 'f' from m_permission p
where p.id = m_role_permission.permission_id
and p.code in ("ORGANISATION_ADMINISTRATION_SUPER_USER", "PORTFOLIO_MANAGEMENT_SUPER_USER", "USER_ADMINISTRATION_SUPER_USER"));


DELETE from m_permission
where code in ("ORGANISATION_ADMINISTRATION_SUPER_USER", "PORTFOLIO_MANAGEMENT_SUPER_USER", "USER_ADMINISTRATION_SUPER_USER");
