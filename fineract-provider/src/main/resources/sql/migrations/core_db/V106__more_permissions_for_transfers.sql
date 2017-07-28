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

DELETE FROM `m_permission` WHERE  `code`="TRANSFER_CLIENT";
DELETE FROM `m_permission` WHERE  `code`="TRANSFER_CLIENT_CHECKER";

/**Permissions for proposing a transfer**/
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'PROPOSETRANSFER_CLIENT', 'CLIENT', 'PROPOSETRANSFER', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'PROPOSETRANSFER_CLIENT_CHECKER', 'CLIENT', 'PROPOSETRANSFER', 0);

/**Permissions for accepting a transfer**/
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'ACCEPTTRANSFER_CLIENT', 'CLIENT', 'ACCEPTTRANSFER', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'ACCEPTTRANSFER_CLIENT_CHECKER', 'CLIENT', 'ACCEPTTRANSFER', 0);

/**Permissions for rejecting a transfer**/
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'REJECTTRANSFER_CLIENT', 'CLIENT', 'REJECTTRANSFER', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'REJECTTRANSFER_CLIENT_CHECKER', 'CLIENT', 'REJECTTRANSFER', 0);

/**Permissions for withdrawing a transfer proposal**/
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'WITHDRAWTRANSFER_CLIENT', 'CLIENT', 'WITHDRAWTRANSFER', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('portfolio', 'WITHDRAWTRANSFER_CLIENT_CHECKER', 'CLIENT', 'WITHDRAWTRANSFER', 0);