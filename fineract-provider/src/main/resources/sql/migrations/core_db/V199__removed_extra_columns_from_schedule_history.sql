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

ALTER TABLE `m_loan_repayment_schedule_history`
	DROP COLUMN `principal_completed_derived`,
	DROP COLUMN `principal_writtenoff_derived`,
	DROP COLUMN `interest_completed_derived`,
	DROP COLUMN `interest_writtenoff_derived`,
	DROP COLUMN `interest_waived_derived`,
	DROP COLUMN `accrual_interest_derived`,
	DROP COLUMN `fee_charges_completed_derived`,
	DROP COLUMN `fee_charges_writtenoff_derived`,
	DROP COLUMN `fee_charges_waived_derived`,
	DROP COLUMN `accrual_fee_charges_derived`,
	DROP COLUMN `penalty_charges_completed_derived`,
	DROP COLUMN `penalty_charges_writtenoff_derived`,
	DROP COLUMN `penalty_charges_waived_derived`,
	DROP COLUMN `accrual_penalty_charges_derived`,
	DROP COLUMN `total_paid_in_advance_derived`,
	DROP COLUMN `total_paid_late_derived`,
	DROP COLUMN `completed_derived`,
	DROP COLUMN `obligations_met_on_date`,
	ADD COLUMN `version` INT(5) NOT NULL;
