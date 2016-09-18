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

insert into m_loan_charge_paid_by (`loan_transaction_id`,`loan_charge_id`,`amount`) select lt.id, lc.id,  if(lic.amount is null,lc.amount,lic.amount)  from m_loan_transaction lt join m_loan_repayment_schedule rs on rs.loan_id = lt.loan_id and rs.duedate = lt.transaction_date join m_loan_charge lc on lc.loan_id = rs.loan_id and ((lc.due_for_collection_as_of_date > rs.fromdate and lc.due_for_collection_as_of_date <= rs.duedate) or lc.charge_time_enum = 8) and lc.is_active=1 join m_loan loan on loan.id = lt.loan_id join m_product_loan lp on lp.id = loan.product_id and lp.accounting_type =3  left join m_loan_installment_charge lic on lic.loan_charge_id = lc.id and lic.loan_schedule_id = rs.id where  lt.transaction_type_enum = 10 and (lt.fee_charges_portion_derived is not null or lt.penalty_charges_portion_derived is not null) and lt.is_reversed = 0