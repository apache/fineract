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


ALTER TABLE m_group
	ADD UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC) ;

ALTER TABLE m_product_loan
	ADD COLUMN `external_id` VARCHAR(100) NULL  AFTER `loan_transaction_strategy_id`  ,
	ADD UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC) ;

ALTER TABLE m_staff
	ADD COLUMN `external_id` VARCHAR(100) NULL  AFTER `display_name`  ,
	ADD UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC) ;


/* status_id values for client and group
0 - Invalid
100 - Pending
300 - Active
600 - Closed( or Exited)
*/

ALTER TABLE m_client
	ADD COLUMN `status_id` INT(5) NOT NULL  DEFAULT 300 AFTER `is_deleted` ;


ALTER TABLE m_group
	ADD COLUMN `status_id` INT(5) NOT NULL  DEFAULT 300 AFTER `is_deleted` ;
