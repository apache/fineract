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

RENAME TABLE `x_table_cloumn_code_mappings` TO `x_table_column_code_mappings`;

ALTER TABLE `acc_gl_journal_entry`
	CHANGE COLUMN `is_running_balance_caculated` `is_running_balance_calculated` TINYINT(4) NOT NULL DEFAULT '0' AFTER `lastmodified_date`;