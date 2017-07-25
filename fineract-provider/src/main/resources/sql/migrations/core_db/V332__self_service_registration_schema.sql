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

CREATE TABLE `request_audit_table` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`lastname` VARCHAR(100) NOT NULL,
	`username` VARCHAR(100) NOT NULL,
	`mobile_number` VARCHAR(50) NULL DEFAULT NULL,
	`firstname` VARCHAR(100) NOT NULL,
	`authentication_token` VARCHAR(100) NULL DEFAULT NULL,
	`password` VARCHAR(250) NOT NULL,
	`email` VARCHAR(100) NOT NULL,
	`client_id` BIGINT(20) NOT NULL,
	`created_date` DATE NOT NULL,
	`account_number` VARCHAR(100) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK1_request_audit_table_m_client` (`client_id`),
	CONSTRAINT `FK1_request_audit_table_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;
