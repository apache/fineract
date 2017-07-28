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

create table `acc_rule_tags` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`acc_rule_id` BIGINT(20) NOT NULL,
	`tag_id` INT(11) NOT NULL,
	`acc_type_enum` SMALLINT(5) NOT NULL,
	primary key(`id`),
	INDEX `FK_acc_accounting_rule_id` (`acc_rule_id`),
	INDEX `FK_m_code_value_id` (`tag_id`),
	CONSTRAINT `FK_acc_accounting_rule_id` FOREIGN KEY (`acc_rule_id`) REFERENCES `acc_accounting_rule` (`id`),
	CONSTRAINT `FK_m_code_value_id` FOREIGN KEY (`tag_id`) REFERENCES `m_code_value` (`id`)
);