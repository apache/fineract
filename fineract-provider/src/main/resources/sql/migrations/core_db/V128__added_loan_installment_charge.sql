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

CREATE TABLE `m_loan_installment_charge` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_charge_id` BIGINT(20) NOT NULL,
	`loan_schedule_id` BIGINT(20) NOT NULL,
	`due_date` DATE NULL DEFAULT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`amount_paid_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_waived_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_writtenoff_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_outstanding_derived` DECIMAL(19,6) NOT NULL DEFAULT '0.000000',
	`is_paid_derived` TINYINT(1) NOT NULL DEFAULT '0',
	`waived` TINYINT(1) NOT NULL DEFAULT '0',
	`amount_through_charge_payment` DECIMAL(19,6) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK_loan_charge_id_charge_schedule` FOREIGN KEY (`loan_charge_id`) REFERENCES `m_loan_charge` (`id`),
	CONSTRAINT `FK_loan_schedule_id_charge_schedule` FOREIGN KEY (`loan_schedule_id`) REFERENCES `m_loan_repayment_schedule` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
