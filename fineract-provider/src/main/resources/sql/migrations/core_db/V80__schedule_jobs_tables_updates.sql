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

RENAME TABLE `scheduled_jobs` TO `job`;
RENAME TABLE `scheduled_job_runhistory` TO `job_run_history`;

ALTER TABLE `job`
	ALTER `job_name` DROP DEFAULT,
	ALTER `job_display_name` DROP DEFAULT;
ALTER TABLE `job`
	CHANGE COLUMN `job_name` `name` VARCHAR(50) NOT NULL,
	CHANGE COLUMN `job_display_name` `display_name` VARCHAR(50) NOT NULL;

ALTER TABLE `job`
	CHANGE COLUMN `job_initializing_errorlog` `initializing_errorlog` TEXT NULL;


ALTER TABLE `job_run_history`
	ALTER `triggertype` DROP DEFAULT;
ALTER TABLE `job_run_history`
	CHANGE COLUMN `errormessage` `error_message` VARCHAR(500) NULL DEFAULT NULL,
	CHANGE COLUMN `triggertype` `trigger_type` VARCHAR(25) NOT NULL,
	CHANGE COLUMN `errorlog` `error_log` TEXT NULL;
