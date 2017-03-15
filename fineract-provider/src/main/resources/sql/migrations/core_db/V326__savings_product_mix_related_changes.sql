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

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_SAVINGSPRODUCTMIX', 'SAVINGSPRODUCTMIX', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_SAVINGSPRODUCTMIX', 'SAVINGSPRODUCTMIX', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_SAVINGSPRODUCTMIX', 'SAVINGSPRODUCTMIX', 'DELETE', 0);

CREATE TABLE `m_savings_product_mix` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_id` BIGINT(20) NOT NULL,
	`restricted_product_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_product_mix_product_id_to_m_savings_product` (`product_id`),
	INDEX `FK_m_product_mix_restricted_product_id_to_m_savings_product` (`restricted_product_id`),
	CONSTRAINT `FK_m_product_mix_restricted_product_id_to_m_savings_product` FOREIGN KEY (`restricted_product_id`) REFERENCES `m_savings_product` (`id`),
	CONSTRAINT `FK_m_product_mix_product_id_to_m_savings_product` FOREIGN KEY (`product_id`) REFERENCES `m_savings_product` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;