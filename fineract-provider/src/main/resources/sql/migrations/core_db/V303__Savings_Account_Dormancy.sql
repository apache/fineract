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

ALTER TABLE `m_savings_product`
ADD COLUMN `is_dormancy_tracking_active` SMALLINT(1) NULL,
ADD COLUMN `days_to_inactive` INT(11) NULL,
ADD COLUMN `days_to_dormancy` INT(11) NULL,
ADD COLUMN `days_to_escheat` INT(11) NULL;

ALTER TABLE `m_savings_account`
ADD COLUMN `sub_status_enum` SMALLINT(5) NOT NULL DEFAULT '0' AFTER `status_enum`;

INSERT INTO `job` (`name`, `display_name`, `cron_expression`, `create_time`) VALUES ('Update Savings Dormant Accounts', 'Update Savings Dormant Accounts', '0 0 0 1/1 * ? *', now());

INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('Savings Accounts Dormancy Report', 'Table', 'Savings', 'select cl.display_name as \'Client Display Name\',\r\nsa.account_no as \'Account Number\',\r\ncl.mobile_no as \'Mobile Number\',\r\n@lastdate:=(select IFNULL(max(sat.transaction_date),sa.activatedon_date) \r\n            from m_savings_account_transaction as sat \r\n            where sat.is_reversed = 0 \r\n            and sat.transaction_type_enum in (1,2) \r\n            and sat.savings_account_id = sa.id) as \'Date of Last Activity\',\r\nDATEDIFF(now(), @lastdate) as \'Days Since Last Activity\'\r\nfrom m_savings_account as sa \r\ninner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = 1) \r\nleft join m_client as cl on sa.client_id = cl.id \r\nwhere sa.sub_status_enum = ${subStatus}\r\nand cl.office_id = ${officeId}', NULL, 1, 1);

INSERT INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `parameter_sql`) VALUES ('SavingsAccountSubStatus', 'subStatus', 'SavingsAccountDormancyStatus', 'select', 'number', '100', 'select * from\r\n(select 100 as id, "Inactive" as name  union all\r\nselect 200 as id, "Dormant" as  name union all \r\nselect 300 as id, "Escheat" as name) x\r\norder by x.`id`');

INSERT INTO stretchy_report_parameter (report_id, parameter_id) VALUES ((select sr.id From stretchy_report sr where sr.report_name='Savings Accounts Dormancy Report'),(select sp.id from stretchy_parameter sp where sp.parameter_name='SavingsAccountSubStatus'));

INSERT INTO stretchy_report_parameter (report_id, parameter_id) VALUES ((select sr.id From stretchy_report sr where sr.report_name='Savings Accounts Dormancy Report'),(select sp.id from stretchy_parameter sp where sp.parameter_name='OfficeIdSelectOne'));
