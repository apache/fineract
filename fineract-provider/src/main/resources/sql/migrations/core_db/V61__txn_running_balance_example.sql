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


/* Leaving as out-of-box (but non-core report) for now
could be removed later if people think too many non-generic
reports are being added
similar example used in small MFI Elevate Africa */


delete from stretchy_report_parameter
where report_id = (select r.id from stretchy_report r where r.report_name = 'TxnRunningBalances');

DELETE FROM `stretchy_report` where report_name = 'TxnRunningBalances';

DELETE FROM `m_permission` where entity_name = 'TxnRunningBalances';


INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `core_report`, `use_report`, `description`, `report_sql`)
VALUES ('TxnRunningBalances', 'Table', 'Transaction', false, false,
"Running Balance Txn report for Individual Lending.
Suitable for small MFI's.  Larger could use it using the branch or other parameters.
Basically, suck it and see if its quick enough for you out-of-te box or whether it needs performance work in your situation.
",

"
select date('${startDate}') as 'Transaction Date', 'Opening Balance' as `Transaction Type`, null as Office,
	null as 'Loan Officer', null as `Loan Account No`, null as `Loan Product`, null as `Currency`,
	null as `Client Account No`, null as Client,
	null as Amount, null as Principal, null as Interest,
@totalOutstandingPrincipal :=
ifnull(round(sum(
	if (txn.transaction_type_enum = 1 /* disbursement */,
		ifnull(txn.amount,0.00),
		ifnull(txn.principal_portion_derived,0.00) * -1))
			,2),0.00)  as 'Outstanding Principal',

@totalInterestIncome :=
ifnull(round(sum(
	if (txn.transaction_type_enum in (2,5,8) /* repayment, repayment at disbursal, recovery repayment */,
		ifnull(txn.interest_portion_derived,0.00),
		0))
			,2),0.00) as 'Interest Income',

@totalWriteOff :=
ifnull(round(sum(
	if (txn.transaction_type_enum = 6 /* write-off */,
		ifnull(txn.principal_portion_derived,0.00),
		0))
			,2),0.00) as 'Principal Write Off'
from m_office o
join m_office ounder on ounder.hierarchy like concat(o.hierarchy, '%')
                          and ounder.hierarchy like concat('${currentUserHierarchy}', '%')
join m_client c on c.office_id = ounder.id
join m_loan l on l.client_id = c.id
join m_product_loan lp on lp.id = l.product_id
join m_loan_transaction txn on txn.loan_id = l.id
left join m_currency cur on cur.code = l.currency_code
where txn.is_reversed = false
and txn.transaction_type_enum not in (10,11)
and o.id = ${officeId}
and txn.transaction_date < date('${startDate}')

union all

select x.`Transaction Date`, x.`Transaction Type`, x.Office, x.`Loan Officer`, x.`Loan Account No`, x.`Loan Product`, x.`Currency`,
	x.`Client Account No`, x.Client, x.Amount, x.Principal, x.Interest,
cast(round(
	if (x.transaction_type_enum = 1 /* disbursement */,
		@totalOutstandingPrincipal := @totalOutstandingPrincipal + x.`Amount`,
		@totalOutstandingPrincipal := @totalOutstandingPrincipal - x.`Principal`)
			,2) as decimal(19,2)) as 'Outstanding Principal',
cast(round(
	if (x.transaction_type_enum in (2,5,8) /* repayment, repayment at disbursal, recovery repayment */,
		@totalInterestIncome := @totalInterestIncome + x.`Interest`,
		@totalInterestIncome)
			,2) as decimal(19,2)) as 'Interest Income',
cast(round(
	if (x.transaction_type_enum = 6 /* write-off */,
		@totalWriteOff := @totalWriteOff + x.`Principal`,
		@totalWriteOff)
			,2) as decimal(19,2)) as 'Principal Write Off'
from
(select txn.transaction_type_enum, txn.id as txn_id, txn.transaction_date as 'Transaction Date',
cast(
	ifnull(re.enum_message_property, concat('Unknown Transaction Type Value: ' , txn.transaction_type_enum))
	as char) as 'Transaction Type',
ounder.`name` as Office, lo.display_name as 'Loan Officer',
l.account_no  as 'Loan Account No', lp.`name` as 'Loan Product',
ifnull(cur.display_symbol, l.currency_code) as Currency,
c.account_no as 'Client Account No', c.display_name as 'Client',
ifnull(txn.amount,0.00) as Amount,
ifnull(txn.principal_portion_derived,0.00) as Principal,
ifnull(txn.interest_portion_derived,0.00) as Interest
from m_office o
join m_office ounder on ounder.hierarchy like concat(o.hierarchy, '%')
                          and ounder.hierarchy like concat('${currentUserHierarchy}', '%')
join m_client c on c.office_id = ounder.id
join m_loan l on l.client_id = c.id
left join m_staff lo on lo.id = l.loan_officer_id
join m_product_loan lp on lp.id = l.product_id
join m_loan_transaction txn on txn.loan_id = l.id
left join m_currency cur on cur.code = l.currency_code
left join r_enum_value re on re.enum_name = 'transaction_type_enum'
						and re.enum_id = txn.transaction_type_enum
where txn.is_reversed = false
and txn.transaction_type_enum not in (10,11)
and (ifnull(l.loan_officer_id, -10) = '${loanOfficerId}' or '-1' = '${loanOfficerId}')
and o.id = ${officeId}
and txn.transaction_date >= date('${startDate}')
and txn.transaction_date <= date('${endDate}')
order by txn.transaction_date, txn.id) x
");



INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('report', 'READ_TxnRunningBalances', 'TxnRunningBalances', 'READ', 0);


insert into stretchy_report_parameter (report_id, parameter_id)
select r.id, p.id
from stretchy_report r,
stretchy_parameter p
where r.report_name = 'TxnRunningBalances'
and p.parameter_name in ('startDateSelect', 'endDateSelect', 'OfficeIdSelectOne',
			'loanOfficerIdSelectAll');