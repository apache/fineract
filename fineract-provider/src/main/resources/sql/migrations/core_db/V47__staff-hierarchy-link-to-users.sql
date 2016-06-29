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

ALTER TABLE `m_appuser`
ADD COLUMN `staff_id` BIGINT(20) NULL DEFAULT NULL AFTER `office_id` ;

ALTER TABLE m_appuser
ADD CONSTRAINT `fk_m_appuser_002`
FOREIGN KEY (`staff_id`)
REFERENCES m_staff (`id`)
ON DELETE NO ACTION
ON UPDATE NO ACTION
,ADD INDEX `fk_m_appuser_002x` (`staff_id` ASC);

ALTER TABLE `m_staff`
ADD COLUMN `organisational_role_enum` SMALLINT NULL DEFAULT NULL AFTER `external_id`,
ADD COLUMN `organisational_role_parent_staff_id` BIGINT(20) NULL DEFAULT NULL AFTER `organisational_role_enum`;
