UPDATE m_permission
SET action_name = CONCAT(action_name,'_CHECKER')
WHERE code LIKE "%_CHECKER";