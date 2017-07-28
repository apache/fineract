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


update stretchy_report
set report_sql =
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
ifnull(lpa.principal_in_advance_derived,0.0) as LoanPaidInAdvance,

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

"
where report_name = 'ProgramDetails';
