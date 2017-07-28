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



INSERT INTO `stretchy_report` (`report_name`, `report_type`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('GroupSummaryDetails', 'Table', true, false, 'Utility query for getting group summary details for a group_id',

"/*
Active Client is a client linked to the 'group' via m_group_client
and with an active status_enum (not sure what it means if the group is inactive)

Active Borrowers - Borrow may be a client or a 'group'

Currency is an outstanding issue

Couldn't set up group savings for a client
(as opposed to opening a individual clients savings account)
*/
select x.*
from m_office o,
m_group g,

(select a.activeClients,
(b.activeClientLoans + c.activeGroupLoans) as activeLoans,
b.activeClientLoans, c.activeGroupLoans,
(b.activeClientBorrowers + c.activeGroupBorrowers) as activeBorrowers,
b.activeClientBorrowers, c.activeGroupBorrowers, d.*,
0 as activeSavings, 0 as totalSavingsAmount
from
(select count(*) as activeClients
from m_group topgroup
join m_group g on g.hierarchy like concat(topgroup.hierarchy, '%')
join m_group_client gc on gc.group_id = g.id
join m_client c on c.id = gc.client_id
where topgroup.id = ${groupId}
and c.status_enum = 300) a,

(select count(*) as activeClientLoans,
count(distinct(l.client_id)) as activeClientBorrowers
from m_group topgroup
join m_group g on g.hierarchy like concat(topgroup.hierarchy, '%')
join m_loan l on l.group_id = g.id and l.client_id is not null
where topgroup.id = ${groupId}
and l.loan_status_id = 300) b,

(select count(*) as activeGroupLoans,
count(distinct(l.group_id)) as activeGroupBorrowers
from m_group topgroup
join m_group g on g.hierarchy like concat(topgroup.hierarchy, '%')
join m_loan l on l.group_id = g.id and l.client_id is null
where topgroup.id = ${groupId}
and l.loan_status_id = 300) c,

(select ifnull(sum(l.principal_disbursed_derived),0) as totalDisbursedAmount,
ifnull(sum(l.principal_outstanding_derived),0) as totalLoanOutstandingAmount,
count(laa.loan_id) as overdueLoans, ifnull(sum(laa.total_overdue_derived), 0) as totalLoanOverdueAmount
from m_group topgroup
join m_group g on g.hierarchy like concat(topgroup.hierarchy, '%')
join m_loan l on l.group_id = g.id
left join m_loan_arrears_aging laa on laa.loan_id = l.id
where topgroup.id = ${groupId}
and l.disbursedon_date is not null) d) x

where g.id = ${groupId}
and o.id = g.office_id
and o.hierarchy like concat('${currentUserHierarchy}', '%')");




INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_GroupSummaryDetails', 'GroupSummaryDetails', 'READ', 0);
