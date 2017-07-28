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

-- Example: INSERT INTO `acc_gl_account` 
--	(
-- `name`, `parent_id`, `hierarchy`, `gl_code`, `disabled`, `manual_journal_entries_allowed`,
--	`account_usage`, `classification_enum`, `tag_id`, `description`
--	)
--	VALUES 
--	(
--	'Opening Balances Contra Account', NULL, '.', 'OBCA', 0, 1, 
--	1, 3, NULL, NULL
--	);

INSERT INTO `c_configuration` 
	(
		`name`, 
		`value`, 
		`enabled`
	)
	VALUES 
	(
		'office-opening-balances-contra-account', 
		0, -- Or Example: (SELECT id FROM acc_gl_account WHERE gl_code = 'OBCA' ),
		1);

ALTER TABLE `c_configuration`
	ADD UNIQUE INDEX `name_UNIQUE` (`name`);
	
INSERT INTO `m_permission`
	(
		`grouping`, `code`, `entity_name`, `action_name`
	) 
	VALUES 
	(
		'accounting', 'DEFINEOPENINGBALANCE_JOURNALENTRY', 'JOURNALENTRY', 'DEFINEOPENINGBALANCE'
	);
