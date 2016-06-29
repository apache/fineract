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

UPDATE m_permission  SET
action_name="UPDATE" 
WHERE
code = "UPDATE_TELLER";

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`) VALUES ('cash_mgmt', 'DELETE_TELLER', 'TELLER', 'DELETE');

	
ALTER TABLE `m_cashier_transactions`
	DROP FOREIGN KEY `FK_m_teller_transactions_m_cashiers`;
	
ALTER TABLE `m_cashier_transactions`
	ADD CONSTRAINT `FK_m_teller_transactions_m_cashiers` FOREIGN KEY (`cashier_id`) REFERENCES `m_cashiers` (`id`) ON UPDATE CASCADE ON DELETE CASCADE;