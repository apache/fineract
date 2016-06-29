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

ALTER TABLE `c_configuration`
	CHANGE COLUMN `name` `name` VARCHAR(100) NULL DEFAULT NULL AFTER `id`;
	
INSERT INTO `c_configuration` (`name`, `value`, `date_value`, `enabled`, `is_trap_door`, `description`) VALUES ( 'change-emi-if-repaymentdate-same-as-disbursementdate', 0, NULL, 1, 0, 'In tranche loans, if repayment date is same as tranche disbursement date then allow to change the emi amount');
