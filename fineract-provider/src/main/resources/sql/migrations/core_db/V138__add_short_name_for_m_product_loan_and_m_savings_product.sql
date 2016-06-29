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

ALTER TABLE `m_product_loan`
	ADD COLUMN `short_name` VARCHAR(4) NULL DEFAULT NULL AFTER `id`;
update m_product_loan set short_name=concat(LEFT(name,2), RIGHT(id,2)) where short_name is null;
ALTER TABLE `m_product_loan`
	ALTER `short_name` DROP DEFAULT;
ALTER TABLE `m_product_loan`
	CHANGE COLUMN `short_name` `short_name` VARCHAR(4) NOT NULL AFTER `id`;
ALTER TABLE `m_product_loan`
	ADD UNIQUE INDEX `unq_short_name` (`short_name`);

ALTER TABLE `m_savings_product`
	ADD COLUMN `short_name` VARCHAR(4) NULL DEFAULT NULL AFTER `name`;
update m_savings_product set short_name=concat(LEFT(name,2), RIGHT(id,2)) where short_name is null;
ALTER TABLE `m_savings_product`
	ALTER `short_name` DROP DEFAULT;
ALTER TABLE `m_savings_product`
	CHANGE COLUMN `short_name` `short_name` VARCHAR(4) NOT NULL AFTER `name`,
	ADD UNIQUE INDEX `sp_unq_short_name` (`short_name`);
