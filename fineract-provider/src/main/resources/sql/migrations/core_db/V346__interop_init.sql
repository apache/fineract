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

DROP TABLE IF EXISTS `interop_identifier`;
CREATE TABLE `interop_identifier` (
	`id`                BIGINT(20)   NOT NULL AUTO_INCREMENT,
	`account_id`        BIGINT(20)   NOT NULL,
	`type`              VARCHAR(32)  NOT NULL,
	`a_value`           VARCHAR(128) NOT NULL,
	`sub_value_or_type` VARCHAR(128) NULL,
	`created_by`        VARCHAR(32)  NOT NULL,
	`created_on`        TIMESTAMP    NOT NULL,
	`modified_by`       VARCHAR(32)  NULL,
	`modified_on`       TIMESTAMP    NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `uk_interop_identifier_account` (`account_id`, `type`),
	UNIQUE INDEX `uk_interop_identifier_value` (`type`, `a_value`, `sub_value_or_type`),
	INDEX `fk_interop_identifier_account` (`account_id`),
	CONSTRAINT `fk_interop_identifier_account` FOREIGN KEY (`account_id`) REFERENCES `m_savings_account` (`id`)
)
	COLLATE = 'utf8_general_ci'
	ENGINE = InnoDB;

-- user+roles

SET @interop_username = 'interopUser';
INSERT INTO `m_appuser`
VALUES (NULL, 0, 1, NULL, @interop_username, 'Interop', 'User', '5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a',
							'email@email.com', b'0', b'1', b'1', b'1', b'1', CURDATE(), 0, b'0');

INSERT INTO `m_appuser_role` VALUES ((SELECT id FROM m_appuser WHERE username = @interop_username), 1);

-- Interoperation permissions

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'READ_INTERID', 'INTERID', 'READ', 0);
INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'READ_INTERREQUEST', 'INTERREQUEST', 'READ', 0);
INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'READ_INTERQUOTE', 'INTERQUOTE', 'READ', 0);
INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'READ_INTERTRANSFER', 'INTERTRANSFER', 'READ', 0);

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'PREPARE_INTERTRANSFER', 'INTERTRANSFER', 'PREPARE', 0);

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'CREATE_INTERID', 'INTERID', 'CREATE', 0);
INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'CREATE_INTERREQUEST', 'INTERREQUEST', 'CREATE', 0);
INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'CREATE_INTERQUOTE', 'INTERQUOTE', 'CREATE', 0);
INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'CREATE_INTERTRANSFER', 'INTERTRANSFER', 'CREATE', 0);

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUES ('interop', 'DELETE_INTERID', 'INTERID', 'DELETE', 0);


INSERT IGNORE INTO m_code (code_name, is_system_defined) VALUES ('PaymentType', 1);

SET @code_id = -1;
SELECT id INTO @code_id FROM m_code WHERE code_name = 'PaymentType';

INSERT IGNORE INTO m_code_value (code_id, code_value, order_position) VALUES (@code_id, 'Money Transfer', 1);

INSERT IGNORE INTO m_payment_type (value, description, order_position) VALUES ('Money Transfer', 'Money Transfer', 1);