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

ALTER TABLE `ref_loan_transaction_processing_strategy`
DROP COLUMN `lastmodified_date` ,
DROP COLUMN `lastmodifiedby_id` ,
DROP COLUMN `created_date` ,
DROP COLUMN `createdby_id` ;


INSERT INTO `ref_loan_transaction_processing_strategy` (`id`, `code`, `name`) VALUES
(5,'principal-interest-penalties-fees-order-strategy', 'Principal Interest Penalties Fees Order');

INSERT INTO `ref_loan_transaction_processing_strategy` (`id`,`code`, `name`)
VALUES (6,'interest-principal-penalties-fees-order-strategy', 'Interest Principal Penalties Fees Order');