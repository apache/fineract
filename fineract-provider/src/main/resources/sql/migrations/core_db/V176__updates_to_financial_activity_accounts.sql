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

ALTER TABLE acc_gl_office_mapping DROP FOREIGN KEY `FK_office_mapping_office`;

ALTER TABLE acc_gl_office_mapping DROP column office_id;

ALTER TABLE `acc_gl_office_mapping`
	ALTER `financial_account_type` DROP DEFAULT;

ALTER TABLE `acc_gl_office_mapping`
	CHANGE COLUMN `financial_account_type` `financial_activity_type` SMALLINT(5) NOT NULL;

ALTER TABLE `acc_gl_office_mapping`
	ADD UNIQUE INDEX `financial_activity_type` (`financial_activity_type`);


RENAME TABLE `acc_gl_office_mapping` TO `acc_gl_financial_activity_account`;