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

ALTER TABLE `m_portfolio_command_source`
	ADD COLUMN `product_id` BIGINT NULL DEFAULT NULL AFTER `processing_result_enum`;

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_PRODUCTMIX', 'PRODUCTMIX', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_PRODUCTMIX', 'PRODUCTMIX', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'DELETE_PRODUCTMIX', 'PRODUCTMIX', 'DELETE', 0);

CREATE TABLE `m_product_mix` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_id` BIGINT(20) NOT NULL,
	`restricted_product_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_product_mix_product_id_to_m_product_loan` (`product_id`),
	INDEX `FK_m_product_mix_restricted_product_id_to_m_product_loan` (`restricted_product_id`),
	CONSTRAINT `FK_m_product_mix_restricted_product_id_to_m_product_loan` FOREIGN KEY (`restricted_product_id`) REFERENCES `m_product_loan` (`id`),
	CONSTRAINT `FK_m_product_mix_product_id_to_m_product_loan` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;