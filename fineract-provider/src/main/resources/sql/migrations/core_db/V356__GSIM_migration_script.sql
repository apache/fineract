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


-- permissions added


INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_GSIMACCOUNT', 'GSIMACCOUNT', 'CREATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'APPROVE_GSIMACCOUNT', 'GSIMACCOUNT', 'APPROVE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'ACTIVATE_GSIMACCOUNT', 'GSIMACCOUNT', 'ACTIVATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'APPROVALUNDO_GSIMACCOUNT', 'GSIMACCOUNT', 'APPROVALUNDO', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_GSIMACCOUNT', 'GSIMACCOUNT', 'UPDATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'REJECT_GSIMACCOUNT', 'GSIMACCOUNT', 'REJECT', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DEPOSIT_GSIMACCOUNT', 'GSIMACCOUNT', 'DEPOSIT', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CLOSE_GSIMACCOUNT', 'GSIMACCOUNT', 'CLOSE', 0);


-- new gsim table
CREATE TABLE `gsim_accounts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_id` BIGINT NOT NULL DEFAULT '0',
    `account_number` VARCHAR(50) NOT NULL,
    `parent_deposit` DECIMAL(19,6) NOT NULL DEFAULT '0.000000',
    `child_accounts_count` INT NOT NULL,
    `accepting_child` TINYINT NOT NULL DEFAULT '0',
    `savings_status_id` SMALLINT NOT NULL DEFAULT '0',
    `application_id` DECIMAL(10,0) NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `gsim_account_no_UNIQUE` (`account_number`),
    INDEX `FK_gsim_group_id` (`group_id`),
    CONSTRAINT `FK_gsim_group_id` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`)
);

 -- changes to savings
 ALTER TABLE `m_savings_account` ADD COLUMN `gsim_id` BIGINT DEFAULT NULL AFTER `group_id`;

 ALTER TABLE `m_savings_account` ADD CONSTRAINT `FK_gsim_id` FOREIGN KEY  (`gsim_id`) REFERENCES `gsim_accounts` (`id`);

 -- changes to savings_Transaction
 ALTER TABLE `m_savings_account_transaction` ADD COLUMN `is_loan_disbursement` BIGINT;
