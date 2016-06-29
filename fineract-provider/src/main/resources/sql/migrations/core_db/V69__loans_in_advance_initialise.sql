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

/* initialises m_loan_paid_in_advance table... same sql is run in daily batch job */

truncate m_loan_paid_in_advance;

INSERT INTO m_loan_paid_in_advance(loan_id, principal_in_advance_derived, interest_in_advance_derived,
fee_charges_in_advance_derived, penalty_charges_in_advance_derived, total_in_advance_derived)
select ml.id as loanId,SUM(ifnull(mr.principal_completed_derived, 0)) as principal_in_advance_derived,
SUM(ifnull(mr.interest_completed_derived, 0)) as interest_in_advance_derived,
SUM(ifnull(mr.fee_charges_completed_derived, 0)) as fee_charges_in_advance_derived,
SUM(ifnull(mr.penalty_charges_completed_derived, 0)) as penalty_charges_in_advance_derived,

(SUM(ifnull(mr.principal_completed_derived, 0)) + SUM(ifnull(mr.interest_completed_derived, 0)) +
SUM(ifnull(mr.fee_charges_completed_derived, 0)) + SUM(ifnull(mr.penalty_charges_completed_derived, 0))) as total_in_advance_derived
FROM m_loan ml
INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id
WHERE ml.loan_status_id = 300 and mr.duedate >= CURDATE()
GROUP BY ml.id
HAVING (SUM(ifnull(mr.principal_completed_derived, 0)) + SUM(ifnull(mr.interest_completed_derived, 0)) +
SUM(ifnull(mr.fee_charges_completed_derived, 0)) + SUM(ifnull(mr.penalty_charges_completed_derived, 0))) > 0.0