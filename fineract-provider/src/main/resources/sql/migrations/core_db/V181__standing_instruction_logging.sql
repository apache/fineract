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

CREATE TABLE `m_account_transfer_standing_instructions_history` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`standing_instruction_id` BIGINT(20) NOT NULL,
	`status` VARCHAR(20) NOT NULL,
	`execution_time` DATETIME NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL,
	`error_log` VARCHAR(500) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_account_transfer_standing_instructions_history` (`standing_instruction_id`),
	CONSTRAINT `FK_m_account_transfer_standing_instructions_m_history` FOREIGN KEY (`standing_instruction_id`) REFERENCES `m_account_transfer_standing_instructions` (`id`)
);
