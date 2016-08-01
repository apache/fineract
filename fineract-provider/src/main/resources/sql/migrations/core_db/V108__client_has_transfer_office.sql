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

/***Store destination office Id while client is pending transfers and effective joining date in a particular branch**/
ALTER TABLE `m_client`
	ADD COLUMN `office_joining_date` DATE NULL AFTER `activation_date`,
	ADD COLUMN `transfer_to_office_id` BIGINT(20) NULL AFTER `office_id`,
	ADD CONSTRAINT `FK_m_client_m_office` FOREIGN KEY (`transfer_to_office_id`) REFERENCES `m_office` (`id`);


/**For current Clients, set the office joining date to activation date**/
update m_client set office_joining_date=activation_date;
