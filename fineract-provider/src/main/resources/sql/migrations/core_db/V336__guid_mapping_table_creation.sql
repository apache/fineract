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

CREATE TABLE `m_guid_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`command_source_id` BIGINT(20) NOT NULL,
	`guid` VARCHAR(50) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_guid_command_source_id` (`command_source_id`),
	INDEX `guid_INDEX` (`guid`),
	CONSTRAINT `FK_guid_command_source_id` FOREIGN KEY (`command_source_id`) REFERENCES `m_portfolio_command_source` (`id`)
);
