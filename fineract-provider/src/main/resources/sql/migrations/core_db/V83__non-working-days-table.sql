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

CREATE TABLE `m_working_days` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`recurrence` VARCHAR(100) NULL DEFAULT NULL,
	`repayment_rescheduling_enum` SMALLINT(5) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1;

INSERT INTO `m_working_days` (`recurrence`, `repayment_rescheduling_enum`) VALUES ('FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA', 2);