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


INSERT INTO `m_code` (`code_name`, `is_system_defined`) values 
	("Constitution", true);
	
INSERT INTO `m_code` (`code_name`, `is_system_defined`) values 
	("Main Business Line", true);
	
ALTER TABLE `m_client` ADD `legal_form_enum` INT(5);
	
CREATE TABLE `m_client_non_person` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL UNIQUE,
	`constitution_cv_id` INT(11) NOT NULL,
	`incorp_no` varchar(50),
	`incorp_validity_till` DATETIME,
	`main_business_line_cv_id` INT(11),
	`remarks` varchar(150),
	PRIMARY KEY (`id`),
	INDEX `FK_client_id` (`client_id`),	
	CONSTRAINT `FK_client_id` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
);

