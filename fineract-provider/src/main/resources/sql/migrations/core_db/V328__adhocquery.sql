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

CREATE TABLE `m_adhoc` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(100) NULL DEFAULT NULL,
	`query` VARCHAR(2000) NULL DEFAULT NULL,
	`table_name` VARCHAR(100) NULL DEFAULT NULL,
	`table_fields` VARCHAR(1000) NULL DEFAULT NULL,
	`email` VARCHAR(500) NOT NULL,
	`IsActive` TINYINT(1) NOT NULL DEFAULT '0',
	`created_date` DATETIME NULL DEFAULT NULL,
	`createdby_id` BIGINT NOT NULL,
	`lastmodifiedby_id` BIGINT(20) NOT NULL,
	`lastmodified_date` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `createdby_id` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `lastmodifiedby_id` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
;

INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES
('authorisation','UPDATE_ADHOC','ADHOC','UPDATE',1),
('authorisation','UPDATE_ADHOC_CHECKER','ADHOC','UPDATE',0),
('authorisation','DELETE_ADHOC','ADHOC','DELETE',1),
('authorisation','DELETE_ADHOC_CHECKER','ADHOC','DELETE',0),
('authorisation','CREATE_ADHOC','ADHOC','CREATE',1),
('authorisation','CREATE_ADHOC_CHECKER','ADHOC','CREATE',0);

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`) VALUES ('Generate AdhocClient Schedule', 'Generate AdhocClient Schedule', '0 0 12 1/1 * ? *', now());
