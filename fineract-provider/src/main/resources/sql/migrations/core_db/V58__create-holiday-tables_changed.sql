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


create table `m_holiday`(
`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`name` varchar(100) NOT NULL,
`from_date` DATETIME NOT NULL,
`to_date` DATETIME NOT NULL,
`description` varchar(100)  NULL DEFAULT NULL,
PRIMARY KEY (`id`),
UNIQUE INDEX `holiday_name` (`name`)
);

CREATE TABLE `m_holiday_office` (
	`holiday_id` BIGINT(20) NOT NULL,
	`office_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`holiday_id`, `office_id`),
	INDEX `m_holiday_id_ibfk_1` (`holiday_id`),
	INDEX `m_office_id_ibfk_2` (`office_id`),
	CONSTRAINT `m_holiday_id_ibfk_1` FOREIGN KEY (`holiday_id`) REFERENCES `m_holiday` (`id`),
	CONSTRAINT `m_office_id_ibfk_2` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'CREATE_HOLIDAY', 'HOLIDAY', 'CREATE', 0);