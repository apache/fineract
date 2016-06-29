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

-- add permissions for Client Fees

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'READ_CLIENTCHARGE', 'CLIENTCHARGE', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_CLIENTCHARGE', 'CLIENTCHARGE', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_CLIENTCHARGE', 'CLIENTCHARGE', 'DELETE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'WAIVE_CLIENTCHARGE', 'CLIENTCHARGE', 'WAIVE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'PAY_CLIENTCHARGE', 'CLIENTCHARGE', 'PAY', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'INACTIVATE_CLIENTCHARGE', 'CLIENTCHARGE', 'INACTIVATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_CLIENTCHARGE', 'CLIENTCHARGE', 'UPDATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'CREATE_CHECKER', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'DELETE_CHECKER', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'WAIVE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'WAIVE_CHECKER', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'PAY_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'PAY_CHECKER', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'INACTIVATE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'INACTIVATE_CHECKER', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_CLIENTCHARGE_CHECKER', 'CLIENTCHARGE', 'UPDATE_CHECKER', 0);


-- new tables
CREATE TABLE `m_client_transaction` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL,
	`office_id` BIGINT(20) NOT NULL,
	`currency_code` VARCHAR(3) NOT NULL,
	`payment_detail_id` BIGINT(20) NULL DEFAULT NULL,
	`is_reversed` TINYINT(1) NOT NULL,
	`external_id` VARCHAR(50) NULL DEFAULT NULL,
	`transaction_date` DATE NOT NULL,
	`transaction_type_enum` SMALLINT(5) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`created_date` DATETIME NOT NULL,
	`appuser_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `external_id` (`external_id`),
	INDEX `FK_m_client_transaction_m_client` (`client_id`),
	INDEX `FK_m_client_transaction_m_appuser` (`appuser_id`),
	CONSTRAINT `FK_m_client_transaction_m_appuser` FOREIGN KEY (`appuser_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_m_client_transaction_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `m_client_charge` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL,
	`charge_id` BIGINT(20) NOT NULL,
	`is_penalty` TINYINT(1) NOT NULL,
	`charge_time_enum` SMALLINT(5) NOT NULL,
	`charge_due_date` DATE NULL DEFAULT NULL,
	`charge_calculation_enum` SMALLINT(5) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`amount_paid_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_waived_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_writtenoff_derived` DECIMAL(19,6) NULL DEFAULT NULL,
	`amount_outstanding_derived` DECIMAL(19,6) NOT NULL,
	`is_paid_derived` TINYINT(1) NULL DEFAULT NULL,
	`waived` TINYINT(1) NULL DEFAULT NULL,
	`is_active` TINYINT(1) NULL DEFAULT NULL,
	`inactivated_on_date` DATE NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_client_charge_m_client` (`client_id`),
	INDEX `FK_m_client_charge_m_charge` (`charge_id`),
	CONSTRAINT `FK_m_client_charge_m_charge` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
	CONSTRAINT `FK_m_client_charge_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `m_client_charge_paid_by` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_transaction_id` BIGINT(20) NOT NULL,
	`client_charge_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_client_charge_paid_by_m_client_transaction` (`client_transaction_id`),
	INDEX `FK_m_client_charge_paid_by_m_client_charge` (`client_charge_id`),
	CONSTRAINT `FK_m_client_charge_paid_by_m_client_charge` FOREIGN KEY (`client_charge_id`) REFERENCES `m_client_charge` (`id`),
	CONSTRAINT `FK_m_client_charge_paid_by_m_client_transaction` FOREIGN KEY (`client_transaction_id`) REFERENCES `m_client_transaction` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

