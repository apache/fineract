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


ALTER TABLE `m_client`
	ADD COLUMN `sub_status` INT(11) NULL DEFAULT NULL AFTER `status_enum`,
	ADD CONSTRAINT `FK_m_client_substatus_m_code_value` FOREIGN KEY (`sub_status`) REFERENCES `m_code_value` (`id`);
	
	
INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('ClientSubStatus', 1);


ALTER TABLE `m_code_value`
	ADD COLUMN `code_description` VARCHAR(500) NULL DEFAULT NULL AFTER `code_value`;

