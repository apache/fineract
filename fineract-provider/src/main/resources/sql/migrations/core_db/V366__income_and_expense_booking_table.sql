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

CREATE TABLE `acc_income_and_expense_bookings` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`gl_closure_id` BIGINT(20) NOT NULL,
	`journal_entry_transaction_id` VARCHAR(60) NOT NULL,
	`office_id` BIGINT(20) NOT NULL,
  `is_reversed` tinyint(1) NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`),
	 FOREIGN KEY (`gl_closure_id`) REFERENCES `acc_gl_closure` (`id`),
   FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
   UNIQUE (journal_entry_transaction_id)
);

ALTER TABLE `acc_gl_closure`
DROP INDEX office_id_closing_date;