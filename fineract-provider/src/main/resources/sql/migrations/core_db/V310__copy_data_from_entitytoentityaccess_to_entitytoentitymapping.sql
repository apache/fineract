insert ignore into m_entity_to_entity_mapping (from_id,to_id,rel_id)  
select  mea.entity_id as fromId,
mea.second_entity_id as toid,
case mea.access_type_code_value_id
when 10 then 1
when 11 then 2
when 12 then 3
end as reId
from m_entity_to_entity_access mea