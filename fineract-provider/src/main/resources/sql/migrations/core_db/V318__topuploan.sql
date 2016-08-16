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

ALTER TABLE `m_product_loan`
	ADD COLUMN `can_use_for_topup` TINYINT(1) NOT NULL DEFAULT 0 AFTER `instalment_amount_in_multiples_of`;

ALTER TABLE `m_loan`
	ADD COLUMN `is_topup` TINYINT(1) NOT NULL DEFAULT 0 AFTER `loan_sub_status_id`;

CREATE TABLE `m_loan_topup` (
	`id` BIGINT NOT NULL AUTO_INCREMENT,
	`loan_id` BIGINT NOT NULL,
	`closure_loan_id` BIGINT NOT NULL,
	`account_transfer_details_id` BIGINT NULL,
	`topup_amount` DECIMAL(19,6) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `m_loan_topup_FK_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
	CONSTRAINT `m_loan_topup_FK_closure_loan_id` FOREIGN KEY (`closure_loan_id`) REFERENCES `m_loan` (`id`),
	CONSTRAINT `m_loan_topup_FK_account_transfer_details_id` FOREIGN KEY (`account_transfer_details_id`) REFERENCES `m_account_transfer_details` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

