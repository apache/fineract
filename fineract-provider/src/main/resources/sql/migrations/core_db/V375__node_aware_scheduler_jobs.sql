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

ALTER TABLE job ADD COLUMN node_id int;

UPDATE job SET node_id = 1;

ALTER TABLE job ADD COLUMN is_mismatched_job TINYINT(1) DEFAULT 1;

INSERT INTO `job` (`node_id`, `name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `group_name`, `previous_run_start_time`, `next_run_time`, `job_key`, `initializing_errorlog`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`,`is_mismatched_job`) VALUES (0,'Execute All Dirty Jobs', 'Execute All Dirty Jobs', '0 1 0 1/1 * ? *', now(), 5, NULL, NULL, NULL, 'Execute All Dirty JobsJobDetail1 _ DEFAULT', NULL, 1, 0, 1, 0, 0,0);
