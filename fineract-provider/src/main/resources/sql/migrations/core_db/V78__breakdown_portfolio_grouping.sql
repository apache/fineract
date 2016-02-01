/* grouping is misspelt but also should be under accounting */

update m_permission
set grouping = 'accounting'
where grouping = 'organistion';