UPDATE `report`.`stretchy_parameter` SET `parameter_sql`='(select lo.id, lo.display_name as `Name` 
from m_office o 
join m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')
join m_staff lo on lo.office_id = ounder.id
where lo.is_loan_officer = true
and o.id in ( ${officeId}))
union all
(select -10, \'-\')
order by 2' 
WHERE  `parameter_name`='loanOfficerIdSelectAll';