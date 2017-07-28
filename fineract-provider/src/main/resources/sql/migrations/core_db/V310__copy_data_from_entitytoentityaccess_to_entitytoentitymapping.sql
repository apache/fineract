--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

insert ignore into m_entity_to_entity_mapping (from_id,to_id,rel_id)  
select  mea.entity_id as fromId,
mea.second_entity_id as toid,
case mea.access_type_code_value_id
when (select mcv.id from m_code_value mcv where mcv.code_value like'Office Access to Loan Products') then (select mer.id from m_entity_relation mer where mer.code_name like 'office_access_to_loan_products')
when (select mcv.id from m_code_value mcv where mcv.code_value like'Office Access to Savings Products') then (select mer.id from m_entity_relation mer where mer.code_name like 'office_access_to_savings_products')
when (select mcv.id from m_code_value mcv where mcv.code_value like'Office Access to Fees/Charges') then (select mer.id from m_entity_relation mer where mer.code_name like 'office_access_to_fees/charges')
end as reId
from m_entity_to_entity_access mea