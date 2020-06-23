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

ALTER TABLE `tenants`
  ADD COLUMN `pool_initial_size` INT DEFAULT 5 AFTER `auto_update`,
  ADD COLUMN `pool_validation_interval` INT DEFAULT 30000,
  ADD COLUMN `pool_remove_abandoned` tinyint DEFAULT 1,
  ADD COLUMN `pool_remove_abandoned_timeout` INT DEFAULT 60,
  ADD COLUMN `pool_log_abandoned` tinyint DEFAULT 1,
  ADD COLUMN `pool_abandon_when_percentage_full` INT DEFAULT 50,
  ADD COLUMN `pool_test_on_borrow` tinyint DEFAULT 1,
  ADD COLUMN `pool_max_active` INT DEFAULT 40,
  ADD COLUMN `pool_min_idle` INT DEFAULT 20,
  ADD COLUMN `pool_max_idle` INT DEFAULT 10,
  ADD COLUMN `pool_suspect_timeout` INT DEFAULT 60,
  ADD COLUMN `pool_time_between_eviction_runs_millis` INT DEFAULT 34000,
  ADD COLUMN `pool_min_evictable_idle_time_millis` INT DEFAULT 60000;
