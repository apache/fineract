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
	ADD COLUMN `proposed_transfer_date` DATE NULL DEFAULT NULL AFTER `email_address`;

	CREATE TABLE `m_client_transfer_details` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL,
	`from_office_id` BIGINT(20) NOT NULL,
	`to_office_id` BIGINT(20) NOT NULL,
	`proposed_transfer_date` DATE NULL DEFAULT NULL,
	`transfer_type` TINYINT(2) NOT NULL,
	`submitted_on` DATE NOT NULL,
	`submitted_by` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_client_transfer_details_m_client` (`client_id`),
	INDEX `FK_m_client_transfer_details_m_office` (`from_office_id`),
	INDEX `FK_m_client_transfer_details_m_office_2` (`to_office_id`),
	INDEX `FK_m_client_transfer_details_m_appuser` (`submitted_by`),
	CONSTRAINT `FK_m_client_transfer_details_m_appuser` FOREIGN KEY (`submitted_by`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `FK_m_client_transfer_details_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
	CONSTRAINT `FK_m_client_transfer_details_m_office` FOREIGN KEY (`from_office_id`) REFERENCES `m_office` (`id`),
	CONSTRAINT `FK_m_client_transfer_details_m_office_2` FOREIGN KEY (`to_office_id`) REFERENCES `m_office` (`id`)
);