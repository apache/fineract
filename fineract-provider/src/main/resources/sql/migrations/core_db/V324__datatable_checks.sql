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

CREATE TABLE `m_entity_datatable_check` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`application_table_name` VARCHAR(200) NOT NULL,
	`x_registered_table_name` VARCHAR(50) NOT NULL,
	`status_enum` INT(11) NOT NULL,
	`system_defined` TINYINT(4) NOT NULL DEFAULT '0',
	`product_id` BIGINT(10) NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `unique_entity_check` (`application_table_name`, `x_registered_table_name`, `status_enum`, `product_id`),
	INDEX `x_registered_table_name` (`x_registered_table_name`),
	INDEX `product_id` (`product_id`),
	CONSTRAINT `m_entity_datatable_check_ibfk_1` FOREIGN KEY (`x_registered_table_name`)
	    REFERENCES `x_registered_table` (`registered_table_name`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUE ("datatable","READ_ENTITY_DATATABLE_CHECK","ENTITY_DATATABLE_CHECK","READ",0);

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUE ("datatable","CREATE_ENTITY_DATATABLE_CHECK","ENTITY_DATATABLE_CHECK","CREATE",0);

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUE ("datatable","DELETE_ENTITY_DATATABLE_CHECK","ENTITY_DATATABLE_CHECK","DELETE",0);
