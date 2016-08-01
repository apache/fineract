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

ALTER TABLE m_product_loan
 ADD COLUMN `include_in_borrower_cycle` TINYINT(1) NOT NULL DEFAULT '0';

CREATE TABLE `m_client_loan_counter` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL,
	`loan_product_id` BIGINT(20) NOT NULL,
	`loan_id` BIGINT(20) NOT NULL,
	`running_count` SMALLINT(4) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_client_id_loan_counter` (`client_id`),
	INDEX `FK_m_loan_product_loan_counter` (`loan_product_id`),
	INDEX `FK_m_client_loan_counter` (`loan_id`),
	CONSTRAINT `FK_m_client_id_loan_counter` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
	CONSTRAINT `FK_m_loan_product_loan_counter` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`),
	CONSTRAINT `FK_m_client_loan_counter` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;