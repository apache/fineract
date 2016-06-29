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

/* Leaving as out-of-box (but non-core reports) for now
could be removed later if people think too many non-generic
reports are being added
used in quipo TEVI dashboard (landing pages) */

DELETE FROM `stretchy_report` where report_name = 'FieldAgentStats';
DELETE FROM `stretchy_report` where report_name = 'FieldAgentPrograms';
DELETE FROM `stretchy_report` where report_name = 'ProgramDetails';
DELETE FROM `stretchy_report` where report_name = 'ChildrenStaffList';
DELETE FROM `stretchy_report` where report_name = 'CoordinatorStats';
DELETE FROM `stretchy_report` where report_name = 'BranchManagerStats';
DELETE FROM `stretchy_report` where report_name = 'ProgramDirectorStats';
DELETE FROM `stretchy_report` where report_name = 'ProgramStats';

DELETE FROM `m_permission` where entity_name = 'FieldAgentStats';
DELETE FROM `m_permission` where entity_name = 'FieldAgentPrograms';
DELETE FROM `m_permission` where entity_name = 'ProgramDetails';
DELETE FROM `m_permission` where entity_name = 'ChildrenStaffList';
DELETE FROM `m_permission` where entity_name = 'CoordinatorStats';
DELETE FROM `m_permission` where entity_name = 'BranchManagerStats';
DELETE FROM `m_permission` where entity_name = 'ProgramDirectorStats';
DELETE FROM `m_permission` where entity_name = 'ProgramStats';


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('FieldAgentStats', 'Table', 'Quipo', false, false, 'Field Agent Statistics',

"
select ifnull(cur.display_symbol, l.currency_code) as Currency,
/*This query will return more than one entry if more than one currency is used */
count(distinct(c.id)) as activeClients, count(*) as activeLoans,
sum(l.principal_disbursed_derived) as disbursedAmount,
sum(l.principal_outstanding_derived) as loanOutstandingAmount,
round((sum(l.principal_outstanding_derived) * 100) /  sum(l.principal_disbursed_derived),2) as loanOutstandingPC,
sum(ifnull(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance,
sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) as portfolioAtRisk,

round((sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC,

count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) as clientsInDefault,
round((count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) * 100) / count(distinct(c.id)),2) as clientsInDefaultPC,
(sum(l.principal_disbursed_derived) / count(*))  as averageLoanAmount
from m_staff fa
join m_office o on o.id = fa.office_id
			and o.hierarchy like concat('${currentUserHierarchy}', '%')
join m_group pgm on pgm.staff_id = fa.id
join m_loan l on l.group_id = pgm.id and l.client_id is not null
left join m_currency cur on cur.code = l.currency_code
left join m_loan_arrears_aging laa on laa.loan_id = l.id
left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id
join m_client c on c.id = l.client_id
where fa.id = ${staffId}
and l.loan_status_id = 300
group  by l.currency_code
");


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('FieldAgentPrograms', 'Table', 'Quipo', false, false, 'List of Field Agent Programs',

"
select pgm.id, pgm.display_name as `name`, sts.enum_message_property as status
 from m_group pgm
 join m_office o on o.id = pgm.office_id
			and o.hierarchy like concat('${currentUserHierarchy}', '%')
 left join r_enum_value sts on sts.enum_name = 'status_enum' and sts.enum_id = pgm.status_enum
 where pgm.staff_id = ${staffId}
");


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('ProgramDetails', 'Table', 'Quipo', false, false, 'List of Loans in a Program',

"
 select l.id as loanId, l.account_no as loanAccountNo, c.id as clientId, c.account_no as clientAccountNo,
 pgm.display_name as programName,

(select count(*)
from m_loan cy
where cy.group_id = pgm.id and cy.client_id =c.id
and cy.disbursedon_date <= l.disbursedon_date) as loanCycleNo,

c.display_name as clientDisplayName,
 ifnull(cur.display_symbol, l.currency_code) as Currency,
ifnull(l.principal_repaid_derived,0.0) as loanRepaidAmount,
ifnull(l.principal_outstanding_derived, 0.0) as loanOutstandingAmount,
sum(ifnull(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance,

ifnull(laa.principal_overdue_derived, 0.0) as loanInArrearsAmount,
if(ifnull(laa.principal_overdue_derived, 0.00) > 0, 'Yes', 'No') as inDefault,

if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)  as portfolioAtRisk

 from m_group pgm
 join m_office o on o.id = pgm.office_id
			and o.hierarchy like concat('${currentUserHierarchy}', '%')
 join m_loan l on l.group_id = pgm.id and l.client_id is not null
 left join m_currency cur on cur.code = l.currency_code
 join m_client c on c.id = l.client_id
 left join m_loan_arrears_aging laa on laa.loan_id = l.id
 left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id
 where pgm.id = ${programId}
 and l.loan_status_id = 300
order by c.display_name, l.account_no

");


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('ChildrenStaffList', 'Table', 'Quipo', false, false, 'Get Next Level Down Staff',

"
 select s.id, s.display_name,
s.firstname, s.lastname, s.organisational_role_enum,
s.organisational_role_parent_staff_id,
sp.display_name as `organisational_role_parent_staff_display_name`
from m_staff s
join m_staff sp on s.organisational_role_parent_staff_id = sp.id
where s.organisational_role_parent_staff_id = ${staffId}
");


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('CoordinatorStats', 'Table', 'Quipo', false, false, 'Coordinator Statistics',

"
select ifnull(cur.display_symbol, l.currency_code) as Currency,
/*This query will return more than one entry if more than one currency is used */
count(distinct(c.id)) as activeClients, count(*) as activeLoans,
sum(l.principal_disbursed_derived) as disbursedAmount,
sum(l.principal_outstanding_derived) as loanOutstandingAmount,
round((sum(l.principal_outstanding_derived) * 100) /  sum(l.principal_disbursed_derived),2) as loanOutstandingPC,
sum(ifnull(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance,
sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) as portfolioAtRisk,

round((sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC,

count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) as clientsInDefault,
round((count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) * 100) / count(distinct(c.id)),2) as clientsInDefaultPC,
(sum(l.principal_disbursed_derived) / count(*))  as averageLoanAmount
from m_staff coord
join m_staff fa on fa.organisational_role_parent_staff_id = coord.id
join m_office o on o.id = fa.office_id
			and o.hierarchy like concat('${currentUserHierarchy}', '%')
join m_group pgm on pgm.staff_id = fa.id
join m_loan l on l.group_id = pgm.id and l.client_id is not null
left join m_currency cur on cur.code = l.currency_code
left join m_loan_arrears_aging laa on laa.loan_id = l.id
left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id
join m_client c on c.id = l.client_id
where coord.id = ${staffId}
and l.loan_status_id = 300
group  by l.currency_code
");


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('BranchManagerStats', 'Table', 'Quipo', false, false, 'Branch Manager Statistics',

"
select ifnull(cur.display_symbol, l.currency_code) as Currency,
/*This query will return more than one entry if more than one currency is used */
count(distinct(c.id)) as activeClients, count(*) as activeLoans,
sum(l.principal_disbursed_derived) as disbursedAmount,
sum(l.principal_outstanding_derived) as loanOutstandingAmount,
round((sum(l.principal_outstanding_derived) * 100) /  sum(l.principal_disbursed_derived),2) as loanOutstandingPC,
sum(ifnull(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance,
sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) as portfolioAtRisk,

round((sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC,

count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) as clientsInDefault,
round((count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) * 100) / count(distinct(c.id)),2) as clientsInDefaultPC,
(sum(l.principal_disbursed_derived) / count(*))  as averageLoanAmount
from m_staff bm
join m_staff coord on coord.organisational_role_parent_staff_id = bm.id
join m_staff fa on fa.organisational_role_parent_staff_id = coord.id
join m_office o on o.id = fa.office_id
			and o.hierarchy like concat('${currentUserHierarchy}', '%')
join m_group pgm on pgm.staff_id = fa.id
join m_loan l on l.group_id = pgm.id and l.client_id is not null
left join m_currency cur on cur.code = l.currency_code
left join m_loan_arrears_aging laa on laa.loan_id = l.id
left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id
join m_client c on c.id = l.client_id
where bm.id = ${staffId}
and l.loan_status_id = 300
group  by l.currency_code
");


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('ProgramDirectorStats', 'Table', 'Quipo', false, false, 'Program DirectorStatistics',

"
select ifnull(cur.display_symbol, l.currency_code) as Currency,
/*This query will return more than one entry if more than one currency is used */
count(distinct(c.id)) as activeClients, count(*) as activeLoans,
sum(l.principal_disbursed_derived) as disbursedAmount,
sum(l.principal_outstanding_derived) as loanOutstandingAmount,
round((sum(l.principal_outstanding_derived) * 100) /  sum(l.principal_disbursed_derived),2) as loanOutstandingPC,
sum(ifnull(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance,
sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) as portfolioAtRisk,

round((sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC,

count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) as clientsInDefault,
round((count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) * 100) / count(distinct(c.id)),2) as clientsInDefaultPC,
(sum(l.principal_disbursed_derived) / count(*))  as averageLoanAmount
from m_staff pd
join m_staff bm on bm.organisational_role_parent_staff_id = pd.id
join m_staff coord on coord.organisational_role_parent_staff_id = bm.id
join m_staff fa on fa.organisational_role_parent_staff_id = coord.id
join m_office o on o.id = fa.office_id
			and o.hierarchy like concat('${currentUserHierarchy}', '%')
join m_group pgm on pgm.staff_id = fa.id
join m_loan l on l.group_id = pgm.id and l.client_id is not null
left join m_currency cur on cur.code = l.currency_code
left join m_loan_arrears_aging laa on laa.loan_id = l.id
left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id
join m_client c on c.id = l.client_id
where pd.id = ${staffId}
and l.loan_status_id = 300
group  by l.currency_code
");


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('ProgramStats', 'Table', 'Quipo', false, false, 'Program Statistics',

"
select ifnull(cur.display_symbol, l.currency_code) as Currency,
/*This query will return more than one entry if more than one currency is used */
count(distinct(c.id)) as activeClients, count(*) as activeLoans,
sum(l.principal_disbursed_derived) as disbursedAmount,
sum(l.principal_outstanding_derived) as loanOutstandingAmount,
round((sum(l.principal_outstanding_derived) * 100) /  sum(l.principal_disbursed_derived),2) as loanOutstandingPC,
sum(ifnull(lpa.principal_in_advance_derived,0.0)) as LoanPaidInAdvance,
sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) as portfolioAtRisk,

round((sum(
	if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
		l.principal_outstanding_derived,0)) * 100) / sum(l.principal_outstanding_derived), 2) as portfolioAtRiskPC,

count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) as clientsInDefault,
round((count(distinct(
		if(date_sub(curdate(), interval 28 day) > ifnull(laa.overdue_since_date_derived, curdate()),
			c.id,null))) * 100) / count(distinct(c.id)),2) as clientsInDefaultPC,
(sum(l.principal_disbursed_derived) / count(*))  as averageLoanAmount
from m_group pgm
join m_office o on o.id = pgm.office_id
			and o.hierarchy like concat('${currentUserHierarchy}', '%')
join m_loan l on l.group_id = pgm.id and l.client_id is not null
left join m_currency cur on cur.code = l.currency_code
left join m_loan_arrears_aging laa on laa.loan_id = l.id
left join m_loan_paid_in_advance lpa on lpa.loan_id = l.id
join m_client c on c.id = l.client_id
where pgm.id = ${programId}
and l.loan_status_id = 300
group  by l.currency_code
");



INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_FieldAgentStats', 'FieldAgentStats', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_FieldAgentPrograms', 'FieldAgentPrograms', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_ProgramDetails', 'ProgramDetails', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_ChildrenStaffList', 'ChildrenStaffList', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_CoordinatorStats', 'CoordinatorStats', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_BranchManagerStats', 'BranchManagerStats', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_ProgramDirectorStats', 'ProgramDirectorStats', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_ProgramStats', 'ProgramStats', 'READ', 0);
