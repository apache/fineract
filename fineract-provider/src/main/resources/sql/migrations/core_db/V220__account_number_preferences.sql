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

CREATE TABLE `c_account_number_format` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`account_type_enum` SMALLINT(1) NOT NULL,
	`prefix_type_enum` SMALLINT(2) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `account_type_enum` (`account_type_enum`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


/*permissions*/

insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'CREATE_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'CREATE', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'READ_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'READ', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'UPDATE_ACCOUNTNUMBERFORMAT', 'ACCOUNTNUMBERFORMAT', 'UPDATE', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'DELETE_ACCOUNTNUMBERFORMAT', 'HOOK', 'DELETE', 0);