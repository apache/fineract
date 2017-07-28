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

CREATE TABLE `m_loan_transaction_repayment_schedule_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_transaction_id` BIGINT(20) NOT NULL,
	`loan_repayment_schedule_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`principal_portion_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`interest_portion_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`fee_charges_portion_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`penalty_charges_portion_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_mappings_m_loan_transaction` (`loan_transaction_id`),
	INDEX `FK_mappings_m_loan_repayment_schedule` (`loan_repayment_schedule_id`),
	CONSTRAINT `FK_mappings_m_loan_repayment_schedule` FOREIGN KEY (`loan_repayment_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`),
	CONSTRAINT `FK_mappings_m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
);