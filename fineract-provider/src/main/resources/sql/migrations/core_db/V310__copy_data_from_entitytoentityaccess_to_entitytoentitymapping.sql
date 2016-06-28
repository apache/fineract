insert ignore into m_entity_to_entity_mapping (from_id,to_id,rel_id)  
select  mea.entity_id as fromId,
mea.second_entity_id as toid,
case mea.access_type_code_value_id
when (select mcv.id from m_code_value mcv where mcv.code_value like'Office Access to Loan Products') then (select mer.id from m_entity_relation mer where mer.code_name like 'office_access_to_loan_products')
when (select mcv.id from m_code_value mcv where mcv.code_value like'Office Access to Savings Products') then (select mer.id from m_entity_relation mer where mer.code_name like 'office_access_to_savings_products')
when (select mcv.id from m_code_value mcv where mcv.code_value like'Office Access to Fees/Charges') then (select mer.id from m_entity_relation mer where mer.code_name like 'office_access_to_fees/charges')
end as reId
from m_entity_to_entity_access mea