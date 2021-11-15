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

INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_sql`, `description`, `core_report`, `use_report`, `self_service_user_report`) VALUES ('Loan Approved - Email', 'Email', 'Triggered', 'select  ml.id as loanId,  ifnull(mc.id,mc2.id) as id,  ifnull(mc.firstname,mc2.firstname) as firstname,  \nifnull(mc.middlename,ifnull(mc2.middlename,(\'\'))) as middlename,  ifnull(mc.lastname,mc2.lastname) as lastname,  \nifnull(mc.display_name,mc2.display_name) as display_name,  ifnull(mc.status_enum,mc2.status_enum) as status_enum, \nifnull(mc.mobile_no,mc2.mobile_no) as mobile_no, ifnull(mg.office_id,mc2.office_id) as office_id, ifnull(mg.staff_id,mc2.staff_id) as staff_id, \nmg.id as group_id, mg.display_name as group_name, ifnull(mc.email_address,mc2.email_address) as emailAddress\nfrom m_loan ml left join m_group mg on mg.id = ml.group_id \nleft join m_group_client mgc on mgc.group_id = mg.id \nleft join m_client mc on mc.id = mgc.client_id \nleft join m_client mc2 on mc2.id = ml.client_id\nWHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId}\n', 'Loan and client data of approved loan', '0', '1', '0');
INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_sql`, `description`, `core_report`, `use_report`, `self_service_user_report`) VALUES ('Loan Rejected - Email', 'Email', 'Triggered', 'select  ml.id as loanId,  ifnull(mc.id,mc2.id) as id,  ifnull(mc.firstname,mc2.firstname) as firstname,  \nifnull(mc.middlename,ifnull(mc2.middlename,(\'\'))) as middlename,  ifnull(mc.lastname,mc2.lastname) as lastname,  \nifnull(mc.display_name,mc2.display_name) as display_name,  ifnull(mc.status_enum,mc2.status_enum) as status_enum, \nifnull(mc.mobile_no,mc2.mobile_no) as mobile_no, ifnull(mg.office_id,mc2.office_id) as office_id, ifnull(mg.staff_id,mc2.staff_id) as staff_id, \nmg.id as group_id, mg.display_name as group_name, ifnull(mc.email_address,mc2.email_address) as emailAddress\nfrom m_loan ml left join m_group mg on mg.id = ml.group_id \nleft join m_group_client mgc on mgc.group_id = mg.id \nleft join m_client mc on mc.id = mgc.client_id \nleft join m_client mc2 on mc2.id = ml.client_id\nWHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId}\n', 'Loan and client data of rejected loan', '0', '1', '0');
INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_sql`, `description`, `core_report`, `use_report`, `self_service_user_report`) VALUES ('Loan Repayment - Email', 'Email', 'Triggered', 'select  ml.id as loanId,  ifnull(mc.id,mc2.id) as id,  ifnull(mc.firstname,mc2.firstname) as firstname,  \nifnull(mc.middlename,ifnull(mc2.middlename,(\'\'))) as middlename,  ifnull(mc.lastname,mc2.lastname) as lastname,  \nifnull(mc.display_name,mc2.display_name) as display_name,  ifnull(mc.status_enum,mc2.status_enum) as status_enum, \nifnull(mc.mobile_no,mc2.mobile_no) as mobile_no, ifnull(mg.office_id,mc2.office_id) as office_id, ifnull(mg.staff_id,mc2.staff_id) as staff_id, \nmg.id as group_id, mg.display_name as group_name, ifnull(mc.email_address,mc2.email_address) as emailAddress, lt.amount as repaymentAmount \nfrom m_loan_transaction lt join m_loan ml on ml.id=lt.loan_id left join m_group mg on mg.id = ml.group_id \nleft join m_group_client mgc on mgc.group_id = mg.id \nleft join m_client mc on mc.id = mgc.client_id \nleft join m_client mc2 on mc2.id = ml.client_id\nWHERE (mc.status_enum = 300 or mc2.status_enum = 300) and (mc.email_address is not null or mc2.email_address is not null) and ml.id = ${loanId} and lt.id = ${loanTransactionId}\n', 'Loan and client data of rejected loan', '0', '1', '0');

ALTER TABLE `scheduled_email_campaign`
CHANGE COLUMN `email_attachment_file_format` `email_attachment_file_format` VARCHAR(10) NULL ,
CHANGE COLUMN `stretchy_report_id` `stretchy_report_id` INT(11) NULL ;
