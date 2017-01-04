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

CREATE TABLE `m_calendar_history` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`calendar_id` BIGINT(20) NOT NULL,
	`title` VARCHAR(50) NOT NULL,
	`description` VARCHAR(100) NULL DEFAULT NULL,
	`location` VARCHAR(50) NULL DEFAULT NULL,
	`start_date` DATE NOT NULL,
	`end_date` DATE NULL DEFAULT NULL,
	`duration` SMALLINT(6) NULL DEFAULT NULL,
	`calendar_type_enum` SMALLINT(5) NOT NULL,
	`repeating` TINYINT(1) NOT NULL DEFAULT '0',
	`recurrence` VARCHAR(100) NULL DEFAULT NULL,
	`remind_by_enum` SMALLINT(5) NULL DEFAULT NULL,
	`first_reminder` SMALLINT(11) NULL DEFAULT NULL,
	`second_reminder` SMALLINT(11) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_calendar_m_calendar_history` (`calendar_id`),
	CONSTRAINT `FK_m_calendar_m_calendar_history` FOREIGN KEY (`calendar_id`) REFERENCES `m_calendar` (`id`)
);