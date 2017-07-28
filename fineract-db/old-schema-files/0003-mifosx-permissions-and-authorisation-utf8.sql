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

﻿
-- ========= roles and permissions =========

/*
this scripts removes all current m_role_permission and m_permission entries
and then inserts new m_permission entries and just one m_role_permission entry
which gives the role (id 1 - super user) an ALL_FUNCTIONS permission

If you had other roles set up with specific permissions you will have to set up their permissions again.
*/

-- truncate `m_role_permission`;
-- truncate `m_permission`;
-- truncate `x_registered_table`;

INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES 
('special','ALL_FUNCTIONS',NULL,NULL,0),
('special','ALL_FUNCTIONS_READ',NULL,NULL,0),
('special', 'CHECKER_SUPER_USER', NULL, NULL, '0'),
('special','REPORTING_SUPER_USER',NULL,NULL,0),
('authorisation','READ_PERMISSION','PERMISSION','READ',0),
('authorisation','PERMISSIONS_ROLE','ROLE','PERMISSIONS',1),
('authorisation','CREATE_ROLE','ROLE','CREATE',1),
('authorisation','CREATE_ROLE_CHECKER','ROLE','CREATE',0),
('authorisation','READ_ROLE','ROLE','READ',0),
('authorisation','UPDATE_ROLE','ROLE','UPDATE',1),
('authorisation','UPDATE_ROLE_CHECKER','ROLE','UPDATE',0),
('authorisation','DELETE_ROLE','ROLE','DELETE',1),
('authorisation','DELETE_ROLE_CHECKER','ROLE','DELETE',0),
('authorisation','CREATE_USER','USER','CREATE',1),
('authorisation','CREATE_USER_CHECKER','USER','CREATE',0),
('authorisation','READ_USER','USER','READ',0),
('authorisation','UPDATE_USER','USER','UPDATE',1),
('authorisation','UPDATE_USER_CHECKER','USER','UPDATE',0),
('authorisation','DELETE_USER','USER','DELETE',1),
('authorisation','DELETE_USER_CHECKER','USER','DELETE',0),
('configuration','READ_CONFIGURATION','CONFIGURATION','READ',1),
('configuration','UPDATE_CONFIGURATION','CONFIGURATION','UPDATE',1),
('configuration','UPDATE_CONFIGURATION_CHECKER','CONFIGURATION','UPDATE',0),
('configuration','READ_CODE','CODE','READ',0),
('configuration','CREATE_CODE','CODE','CREATE',1),
('configuration','CREATE_CODE_CHECKER','CODE','CREATE',0),
('configuration','UPDATE_CODE','CODE','UPDATE',1),
('configuration','UPDATE_CODE_CHECKER','CODE','UPDATE',0),
('configuration','DELETE_CODE','CODE','DELETE',1),
('configuration','DELETE_CODE_CHECKER','CODE','DELETE',0),
('configuration', 'READ_CODEVALUE', 'CODEVALUE', 'READ', '0'),
('configuration', 'CREATE_CODEVALUE', 'CODEVALUE', 'CREATE', '1'),
('configuration', 'CREATE_CODEVALUE_CHECKER', 'CODEVALUE', 'CREATE', '0'),
('configuration', 'UPDATE_CODEVALUE', 'CODEVALUE', 'UPDATE', '1'),
('configuration', 'UPDATE_CODEVALUE_CHECKER', 'CODEVALUE', 'UPDATE', '0'),
('configuration', 'DELETE_CODEVALUE', 'CODEVALUE', 'DELETE', '1'),
('configuration', 'DELETE_CODEVALUE_CHECKER', 'CODEVALUE', 'DELETE', '0'),
('configuration','READ_CURRENCY','CURRENCY','READ',0),
('configuration','UPDATE_CURRENCY','CURRENCY','UPDATE',1),
('configuration','UPDATE_CURRENCY_CHECKER','CURRENCY','UPDATE',0),
('configuration', 'UPDATE_PERMISSION', 'PERMISSION', 'UPDATE', '1'),
('configuration', 'UPDATE_PERMISSION_CHECKER', 'PERMISSION', 'UPDATE', '0'),
('configuration', 'READ_DATATABLE', 'DATATABLE', 'READ', '0'),
('configuration', 'REGISTER_DATATABLE', 'DATATABLE', 'REGISTER', '1'),
('configuration', 'REGISTER_DATATABLE_CHECKER', 'DATATABLE', 'REGISTER', '0'),
('configuration', 'DEREGISTER_DATATABLE', 'DATATABLE', 'DEREGISTER', '1'),
('configuration', 'DEREGISTER_DATATABLE_CHECKER', 'DATATABLE', 'DEREGISTER', '0'),
('configuration', 'READ_AUDIT', 'AUDIT', 'READ', '0'),
('configuration', 'CREATE_CALENDAR', 'CALENDAR', 'CREATE', '0'),
('configuration', 'READ_CALENDAR', 'CALENDAR', 'READ', '0'),
('configuration', 'UPDATE_CALENDAR', 'CALENDAR', 'UPDATE', '0'),
('configuration', 'DELETE_CALENDAR', 'CALENDAR', 'DELETE', '0'),
('configuration', 'CREATE_CALENDAR_CHECKER', 'CALENDAR', 'CREATE', '0'),
('configuration', 'UPDATE_CALENDAR_CHECKER', 'CALENDAR', 'UPDATE', '0'),
('configuration', 'DELETE_CALENDAR_CHECKER', 'CALENDAR', 'DELETE', '0'),
('organisation', 'READ_MAKERCHECKER', 'MAKERCHECKER', 'READ', '0'),
('organisation', 'READ_CHARGE', 'CHARGE', 'READ', '0'),
('organisation', 'CREATE_CHARGE', 'CHARGE', 'CREATE', '1'),
('organisation', 'CREATE_CHARGE_CHECKER', 'CHARGE', 'CREATE', '0'),
('organisation', 'UPDATE_CHARGE', 'CHARGE', 'UPDATE', '1'),
('organisation', 'UPDATE_CHARGE_CHECKER', 'CHARGE', 'UPDATE', '0'),
('organisation', 'DELETE_CHARGE', 'CHARGE', 'DELETE', '1'),
('organisation', 'DELETE_CHARGE_CHECKER', 'CHARGE', 'DELETE', '0'),
('organisation', 'READ_FUND', 'FUND', 'READ', '0'),
('organisation', 'CREATE_FUND', 'FUND', 'CREATE', '1'),
('organisation', 'CREATE_FUND_CHECKER', 'FUND', 'CREATE', '0'),
('organisation', 'UPDATE_FUND', 'FUND', 'UPDATE', '1'),
('organisation', 'UPDATE_FUND_CHECKER', 'FUND', 'UPDATE', '0'),
('organisation', 'DELETE_FUND', 'FUND', 'DELETE', '1'),
('organisation', 'DELETE_FUND_CHECKER', 'FUND', 'DELETE', '0'),
('organisation', 'READ_LOANPRODUCT', 'LOANPRODUCT', 'READ', '0'),
('organisation', 'CREATE_LOANPRODUCT', 'LOANPRODUCT', 'CREATE', '1'),
('organisation', 'CREATE_LOANPRODUCT_CHECKER', 'LOANPRODUCT', 'CREATE', '0'),
('organisation', 'UPDATE_LOANPRODUCT', 'LOANPRODUCT', 'UPDATE', '1'),
('organisation', 'UPDATE_LOANPRODUCT_CHECKER', 'LOANPRODUCT', 'UPDATE', '0'),
('organisation', 'DELETE_LOANPRODUCT', 'LOANPRODUCT', 'DELETE', '1'),
('organisation', 'DELETE_LOANPRODUCT_CHECKER', 'LOANPRODUCT', 'DELETE', '0'),
('organisation', 'READ_OFFICE', 'OFFICE', 'READ', '0'),
('organisation', 'CREATE_OFFICE', 'OFFICE', 'CREATE', '1'),
('organisation', 'CREATE_OFFICE_CHECKER', 'OFFICE', 'CREATE', '0'),
('organisation', 'UPDATE_OFFICE', 'OFFICE', 'UPDATE', '1'),
('organisation', 'UPDATE_OFFICE_CHECKER', 'OFFICE', 'UPDATE', '0'),
('organisation', 'READ_OFFICETRANSACTION', 'OFFICETRANSACTION', 'READ', '0'),
('organisation', 'DELETE_OFFICE_CHECKER', 'OFFICE', 'DELETE', '0'),
('organisation', 'CREATE_OFFICETRANSACTION', 'OFFICETRANSACTION', 'CREATE', '1'),
('organisation', 'CREATE_OFFICETRANSACTION_CHECKER', 'OFFICETRANSACTION', 'CREATE', '0'),
('organisation', 'DELETE_OFFICETRANSACTION', 'OFFICETRANSACTION', 'DELETE', 1),
('organisation', 'DELETE_OFFICETRANSACTION_CHECKER', 'OFFICETRANSACTION', 'DELETE', 0),
('organisation', 'READ_STAFF', 'STAFF', 'READ', '0'),
('organisation', 'CREATE_STAFF', 'STAFF', 'CREATE', '1'),
('organisation', 'CREATE_STAFF_CHECKER', 'STAFF', 'CREATE', '0'),
('organisation', 'UPDATE_STAFF', 'STAFF', 'UPDATE', '1'),
('organisation', 'UPDATE_STAFF_CHECKER', 'STAFF', 'UPDATE', '0'),
('organisation', 'DELETE_STAFF', 'STAFF', 'DELETE', '1'),
('organisation', 'DELETE_STAFF_CHECKER', 'STAFF', 'DELETE', '0'),
('organisation', 'READ_SAVINGSPRODUCT', 'SAVINGSPRODUCT', 'READ', '0'),
('organisation', 'CREATE_SAVINGSPRODUCT', 'SAVINGSPRODUCT', 'CREATE', '1'),
('organisation', 'CREATE_SAVINGSPRODUCT_CHECKER', 'SAVINGSPRODUCT', 'CREATE', '0'),
('organisation', 'UPDATE_SAVINGSPRODUCT', 'SAVINGSPRODUCT', 'UPDATE', '1'),
('organisation', 'UPDATE_SAVINGSPRODUCT_CHECKER', 'SAVINGSPRODUCT', 'UPDATE', '0'),
('organisation', 'DELETE_SAVINGSPRODUCT', 'SAVINGSPRODUCT', 'DELETE', '1'),
('organisation', 'DELETE_SAVINGSPRODUCT_CHECKER', 'SAVINGSPRODUCT', 'DELETE', '0'),
('portfolio', 'READ_LOAN', 'LOAN', 'READ', '0'),
('portfolio', 'CREATE_LOAN', 'LOAN', 'CREATE', '1'),
('portfolio', 'CREATE_LOAN_CHECKER', 'LOAN', 'CREATE', '0'),
('portfolio', 'UPDATE_LOAN', 'LOAN', 'UPDATE', '1'),
('portfolio', 'UPDATE_LOAN_CHECKER', 'LOAN', 'UPDATE', '0'),
('portfolio', 'DELETE_LOAN', 'LOAN', 'DELETE', '1'),
('portfolio', 'DELETE_LOAN_CHECKER', 'LOAN', 'DELETE', '0'),
-- ('portfolio', 'CREATEHISTORIC_LOAN', 'LOAN', 'CREATEHISTORIC', '1'),
-- ('portfolio', 'CREATEHISTORIC_LOAN_CHECKER', 'LOAN', 'CREATEHISTORIC', '0'),
-- ('portfolio', 'UPDATEHISTORIC_LOAN', 'LOAN', 'UPDATEHISTORIC', '1'),
-- ('portfolio', 'UPDATEHISTORIC_LOAN_CHECKER', 'LOAN', 'UPDATEHISTORIC', '0'),
('portfolio', 'READ_CLIENT', 'CLIENT', 'READ', '0'),
('portfolio', 'CREATE_CLIENT', 'CLIENT', 'CREATE', '1'),
('portfolio', 'CREATE_CLIENT_CHECKER', 'CLIENT', 'CREATE', '0'),
('portfolio', 'UPDATE_CLIENT', 'CLIENT', 'UPDATE', '1'),
('portfolio', 'UPDATE_CLIENT_CHECKER', 'CLIENT', 'UPDATE', '0'),
('portfolio', 'DELETE_CLIENT', 'CLIENT', 'DELETE', '1'),
('portfolio', 'DELETE_CLIENT_CHECKER', 'CLIENT', 'DELETE', '0'),
('portfolio', 'READ_CLIENTIMAGE', 'CLIENTIMAGE', 'READ', '0'),
('portfolio', 'CREATE_CLIENTIMAGE', 'CLIENTIMAGE', 'CREATE', '1'),
('portfolio', 'CREATE_CLIENTIMAGE_CHECKER', 'CLIENTIMAGE', 'CREATE', '0'),
('portfolio', 'DELETE_CLIENTIMAGE', 'CLIENTIMAGE', 'DELETE', '1'),
('portfolio', 'DELETE_CLIENTIMAGE_CHECKER', 'CLIENTIMAGE', 'DELETE', '0'),
('portfolio', 'READ_CLIENTNOTE', 'CLIENTNOTE', 'READ', '0'),
('portfolio', 'CREATE_CLIENTNOTE', 'CLIENTNOTE', 'CREATE', '1'),
('portfolio', 'CREATE_CLIENTNOTE_CHECKER', 'CLIENTNOTE', 'CREATE', '0'),
('portfolio', 'UPDATE_CLIENTNOTE', 'CLIENTNOTE', 'UPDATE', '1'),
('portfolio', 'UPDATE_CLIENTNOTE_CHECKER', 'CLIENTNOTE', 'UPDATE', '0'),
('portfolio', 'DELETE_CLIENTNOTE', 'CLIENTNOTE', 'DELETE', '1'),
('portfolio', 'DELETE_CLIENTNOTE_CHECKER', 'CLIENTNOTE', 'DELETE', '0'),
('portfolio', 'READ_GROUPNOTE', 'GROUPNOTE', 'READ', '0'),
('portfolio', 'CREATE_GROUPNOTE', 'GROUPNOTE', 'CREATE', '1'),
('portfolio', 'UPDATE_GROUPNOTE', 'GROUPNOTE', 'UPDATE', '1'),
('portfolio', 'DELETE_GROUPNOTE', 'GROUPNOTE', 'DELETE', '1'),
('portfolio', 'CREATE_GROUPNOTE_CHECKER', 'GROUPNOTE', 'CREATE', '0'),
('portfolio', 'UPDATE_GROUPNOTE_CHECKER', 'GROUPNOTE', 'UPDATE', '0'),
('portfolio', 'DELETE_GROUPNOTE_CHECKER', 'GROUPNOTE', 'DELETE', '0'),
('portfolio', 'READ_LOANNOTE', 'LOANNOTE', 'READ', '0'),
('portfolio', 'CREATE_LOANNOTE', 'LOANNOTE', 'CREATE', '1'),
('portfolio', 'UPDATE_LOANNOTE', 'LOANNOTE', 'UPDATE', '1'),
('portfolio', 'DELETE_LOANNOTE', 'LOANNOTE', 'DELETE', '1'),
('portfolio', 'CREATE_LOANNOTE_CHECKER', 'LOANNOTE', 'CREATE', '0'),
('portfolio', 'UPDATE_LOANNOTE_CHECKER', 'LOANNOTE', 'UPDATE', '0'),
('portfolio', 'DELETE_LOANNOTE_CHECKER', 'LOANNOTE', 'DELETE', '0'),
('portfolio', 'READ_LOANTRANSACTIONNOTE', 'LOANTRANSACTIONNOTE', 'READ', '0'),
('portfolio', 'CREATE_LOANTRANSACTIONNOTE', 'LOANTRANSACTIONNOTE', 'CREATE', '1'),
('portfolio', 'UPDATE_LOANTRANSACTIONNOTE', 'LOANTRANSACTIONNOTE', 'UPDATE', '1'),
('portfolio', 'DELETE_LOANTRANSACTIONNOTE', 'LOANTRANSACTIONNOTE', 'DELETE', '1'),
('portfolio', 'CREATE_LOANTRANSACTIONNOTE_CHECKER', 'LOANTRANSACTIONNOTE', 'CREATE', '0'),
('portfolio', 'UPDATE_LOANTRANSACTIONNOTE_CHECKER', 'LOANTRANSACTIONNOTE', 'UPDATE', '0'),
('portfolio', 'DELETE_LOANTRANSACTIONNOTE_CHECKER', 'LOANTRANSACTIONNOTE', 'DELETE', '0'),
('portfolio', 'READ_SAVINGNOTE', 'SAVINGNOTE', 'READ', '0'),
('portfolio', 'CREATE_SAVINGNOTE', 'SAVINGNOTE', 'CREATE', '1'),
('portfolio', 'UPDATE_SAVINGNOTE', 'SAVINGNOTE', 'UPDATE', '1'),
('portfolio', 'DELETE_SAVINGNOTE', 'SAVINGNOTE', 'DELETE', '1'),
('portfolio', 'CREATE_SAVINGNOTE_CHECKER', 'SAVINGNOTE', 'CREATE', '0'),
('portfolio', 'UPDATE_SAVINGNOTE_CHECKER', 'SAVINGNOTE', 'UPDATE', '0'),
('portfolio', 'DELETE_SAVINGNOTE_CHECKER', 'SAVINGNOTE', 'DELETE', '0'),
('portfolio', 'READ_CLIENTIDENTIFIER', 'CLIENTIDENTIFIER', 'READ', '0'),
('portfolio', 'CREATE_CLIENTIDENTIFIER', 'CLIENTIDENTIFIER', 'CREATE', '1'),
('portfolio', 'CREATE_CLIENTIDENTIFIER_CHECKER', 'CLIENTIDENTIFIER', 'CREATE', '0'),
('portfolio', 'UPDATE_CLIENTIDENTIFIER', 'CLIENTIDENTIFIER', 'UPDATE', '1'),
('portfolio', 'UPDATE_CLIENTIDENTIFIER_CHECKER', 'CLIENTIDENTIFIER', 'UPDATE', '0'),
('portfolio', 'DELETE_CLIENTIDENTIFIER', 'CLIENTIDENTIFIER', 'DELETE', '1'),
('portfolio', 'DELETE_CLIENTIDENTIFIER_CHECKER', 'CLIENTIDENTIFIER', 'DELETE', '0'),
('portfolio', 'READ_DOCUMENT', 'DOCUMENT', 'READ', '0'),
('portfolio', 'CREATE_DOCUMENT', 'DOCUMENT', 'CREATE', '1'),
('portfolio', 'CREATE_DOCUMENT_CHECKER', 'DOCUMENT', 'CREATE', '0'),
('portfolio', 'UPDATE_DOCUMENT', 'DOCUMENT', 'UPDATE', '1'),
('portfolio', 'UPDATE_DOCUMENT_CHECKER', 'DOCUMENT', 'UPDATE', '0'),
('portfolio', 'DELETE_DOCUMENT', 'DOCUMENT', 'DELETE', '1'),
('portfolio', 'DELETE_DOCUMENT_CHECKER', 'DOCUMENT', 'DELETE', '0'),
('portfolio', 'READ_GROUP', 'GROUP', 'READ', '0'),
('portfolio', 'CREATE_GROUP', 'GROUP', 'CREATE', '1'),
('portfolio', 'CREATE_GROUP_CHECKER', 'GROUP', 'CREATE', '0'),
('portfolio', 'UPDATE_GROUP', 'GROUP', 'UPDATE', '1'),
('portfolio', 'UPDATE_GROUP_CHECKER', 'GROUP', 'UPDATE', '0'),
('portfolio', 'DELETE_GROUP', 'GROUP', 'DELETE', '1'),
('portfolio', 'DELETE_GROUP_CHECKER', 'GROUP', 'DELETE', '0'),
('portfolio', 'UNASSIGNSTAFF_GROUP', 'GROUP', 'UNASSIGNSTAFF', 1),
('portfolio', 'UNASSIGNSTAFF_GROUP_CHECKER', 'GROUP', 'UNASSIGNSTAFF', 0),
('portfolio', 'CREATE_LOANCHARGE', 'LOANCHARGE', 'CREATE', '1'),
('portfolio', 'CREATE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'CREATE', '0'),
('portfolio', 'UPDATE_LOANCHARGE', 'LOANCHARGE', 'UPDATE', '1'),
('portfolio', 'UPDATE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'UPDATE', '0'),
('portfolio', 'DELETE_LOANCHARGE', 'LOANCHARGE', 'DELETE', '1'),
('portfolio', 'DELETE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'DELETE', '0'),
('portfolio', 'WAIVE_LOANCHARGE', 'LOANCHARGE', 'WAIVE', '1'),
('portfolio', 'WAIVE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'WAIVE', '0'),
('portfolio', 'READ_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'READ', '0'),
('portfolio', 'CREATE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'CREATE', '1'),
('portfolio', 'CREATE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'CREATE', '0'),
('portfolio', 'UPDATE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UPDATE', '1'),
('portfolio', 'UPDATE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UPDATE', '0'),
('portfolio', 'DELETE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'DELETE', '1'),
('portfolio', 'DELETE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'DELETE', '0'),
('portfolio', 'READ_GUARANTOR', 'GUARANTOR', 'READ', 0),
('portfolio', 'CREATE_GUARANTOR', 'GUARANTOR', 'CREATE', 1),
('portfolio', 'CREATE_GUARANTOR_CHECKER', 'GUARANTOR', 'CREATE', 0),
('portfolio', 'UPDATE_GUARANTOR', 'GUARANTOR', 'UPDATE', 1),
('portfolio', 'UPDATE_GUARANTOR_CHECKER', 'GUARANTOR', 'UPDATE', 0),
('portfolio', 'DELETE_GUARANTOR', 'GUARANTOR', 'DELETE', 1),
('portfolio', 'DELETE_GUARANTOR_CHECKER', 'GUARANTOR', 'DELETE', 0),
('portfolio', 'READ_COLLATERAL', 'COLLATERAL', 'READ', '0'),
('portfolio', 'CREATE_COLLATERAL', 'COLLATERAL', 'CREATE', '1'),
('portfolio', 'UPDATE_COLLATERAL', 'COLLATERAL', 'UPDATE', '1'),
('portfolio', 'DELETE_COLLATERAL', 'COLLATERAL', 'DELETE', '1'),
('portfolio', 'CREATE_COLLATERAL_CHECKER', 'COLLATERAL', 'CREATE', '0'),
('portfolio', 'UPDATE_COLLATERAL_CHECKER', 'COLLATERAL', 'UPDATE', '0'),
('portfolio', 'DELETE_COLLATERAL_CHECKER', 'COLLATERAL', 'DELETE', '0'),
('transaction_loan', 'APPROVE_LOAN', 'LOAN', 'APPROVE', '1'),
('transaction_loan', 'APPROVEINPAST_LOAN', 'LOAN', 'APPROVEINPAST', '1'),
('transaction_loan', 'REJECT_LOAN', 'LOAN', 'REJECT', '1'),
('transaction_loan', 'REJECTINPAST_LOAN', 'LOAN', 'REJECTINPAST', '1'),
('transaction_loan', 'WITHDRAW_LOAN', 'LOAN', 'WITHDRAW', '1'),
('transaction_loan', 'WITHDRAWINPAST_LOAN', 'LOAN', 'WITHDRAWINPAST', '1'),
('transaction_loan', 'APPROVALUNDO_LOAN', 'LOAN', 'APPROVALUNDO', '1'),
('transaction_loan', 'DISBURSE_LOAN', 'LOAN', 'DISBURSE', '1'),
('transaction_loan', 'DISBURSEINPAST_LOAN', 'LOAN', 'DISBURSEINPAST', '1'),
('transaction_loan', 'DISBURSALUNDO_LOAN', 'LOAN', 'DISBURSALUNDO', '1'),
('transaction_loan', 'REPAYMENT_LOAN', 'LOAN', 'REPAYMENT', '1'),
('transaction_loan', 'REPAYMENTINPAST_LOAN', 'LOAN', 'REPAYMENTINPAST', '1'),
('transaction_loan', 'ADJUST_LOAN', 'LOAN', 'ADJUST', '1'),
('transaction_loan', 'WAIVEINTERESTPORTION_LOAN', 'LOAN', 'WAIVEINTERESTPORTION', '1'),
('transaction_loan', 'WRITEOFF_LOAN', 'LOAN', 'WRITEOFF', '1'),
('transaction_loan', 'CLOSE_LOAN', 'LOAN', 'CLOSE', '1'),
('transaction_loan', 'CLOSEASRESCHEDULED_LOAN', 'LOAN', 'CLOSEASRESCHEDULED', '1'),
('transaction_loan', 'UPDATELOANOFFICER_LOAN', 'LOAN', 'UPDATELOANOFFICER', 1),
('transaction_loan', 'UPDATELOANOFFICER_LOAN_CHECKER', 'LOAN', 'UPDATELOANOFFICER', 0),
('transaction_loan', 'REMOVELOANOFFICER_LOAN', 'LOAN', 'REMOVELOANOFFICER', 1),
('transaction_loan', 'REMOVELOANOFFICER_LOAN_CHECKER', 'LOAN', 'REMOVELOANOFFICER', 0),
('transaction_loan', 'BULKREASSIGN_LOAN', 'LOAN', 'BULKREASSIGN', '1'),
('transaction_loan', 'BULKREASSIGN_LOAN_CHECKER', 'LOAN', 'BULKREASSIGN', '0'),
('transaction_loan', 'APPROVE_LOAN_CHECKER', 'LOAN', 'APPROVE', '0'),
('transaction_loan', 'APPROVEINPAST_LOAN_CHECKER', 'LOAN', 'APPROVEINPAST', '0'),
('transaction_loan', 'REJECT_LOAN_CHECKER', 'LOAN', 'REJECT', '0'),
('transaction_loan', 'REJECTINPAST_LOAN_CHECKER', 'LOAN', 'REJECTINPAST', '0'),
('transaction_loan', 'WITHDRAW_LOAN_CHECKER', 'LOAN', 'WITHDRAW', '0'),
('transaction_loan', 'WITHDRAWINPAST_LOAN_CHECKER', 'LOAN', 'WITHDRAWINPAST', '0'),
('transaction_loan', 'APPROVALUNDO_LOAN_CHECKER', 'LOAN', 'APPROVALUNDO', '0'),
('transaction_loan', 'DISBURSE_LOAN_CHECKER', 'LOAN', 'DISBURSE', '0'),
('transaction_loan', 'DISBURSEINPAST_LOAN_CHECKER', 'LOAN', 'DISBURSEINPAST', '0'),
('transaction_loan', 'DISBURSALUNDO_LOAN_CHECKER', 'LOAN', 'DISBURSALUNDO', '0'),
('transaction_loan', 'REPAYMENT_LOAN_CHECKER', 'LOAN', 'REPAYMENT', '0'),
('transaction_loan', 'REPAYMENTINPAST_LOAN_CHECKER', 'LOAN', 'REPAYMENTINPAST', '0'),
('transaction_loan', 'ADJUST_LOAN_CHECKER', 'LOAN', 'ADJUST', '0'),
('transaction_loan', 'WAIVEINTERESTPORTION_LOAN_CHECKER', 'LOAN', 'WAIVEINTERESTPORTION', '0'),
('transaction_loan', 'WRITEOFF_LOAN_CHECKER', 'LOAN', 'WRITEOFF', '0'),
('transaction_loan', 'CLOSE_LOAN_CHECKER', 'LOAN', 'CLOSE', '0'),
('transaction_loan', 'CLOSEASRESCHEDULED_LOAN_CHECKER', 'LOAN', 'CLOSEASRESCHEDULED', '0'),
('transaction_savings', 'DEPOSIT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'DEPOSIT', '1'),
('transaction_savings', 'DEPOSIT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'DEPOSIT', '0'),
('transaction_savings', 'WITHDRAWAL_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'WITHDRAWAL', '1'),
('transaction_savings', 'WITHDRAWAL_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'WITHDRAWAL', '0'),
('transaction_savings', 'ACTIVATE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'ACTIVATE', '1'),
('transaction_savings', 'ACTIVATE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'ACTIVATE', '0'),
('transaction_savings', 'CALCULATEINTEREST_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'CALCULATEINTEREST', '1'),
('transaction_savings', 'CALCULATEINTEREST_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'CALCULATEINTEREST', '0');

-- == accounting related permissions
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES 
('accounting', 'CREATE_GLACCOUNT', 'GLACCOUNT', 'CREATE', 1),
('accounting', 'UPDATE_GLACCOUNT', 'GLACCOUNT', 'UPDATE', 1),
('accounting', 'DELETE_GLACCOUNT', 'GLACCOUNT', 'DELETE', 1),
('accounting', 'CREATE_GLCLOSURE', 'GLCLOSURE', 'CREATE', 1),
('accounting', 'UPDATE_GLCLOSURE', 'GLCLOSURE', 'UPDATE', 1),
('accounting', 'DELETE_GLCLOSURE', 'GLCLOSURE', 'DELETE', 1), 
('accounting', 'CREATE_JOURNALENTRY', 'JOURNALENTRY', 'CREATE', 1),
('accounting', 'REVERSE_JOURNALENTRY', 'JOURNALENTRY', 'REVERSE', 1);


INSERT INTO `m_role` (`id`, `name`, `description`) 
VALUES 
(1,'Super user','This role provides all application permissions.');

/* role 1 is super user, give it ALL_FUNCTIONS */
INSERT INTO m_role_permission(role_id, permission_id)
select 1, id
from m_permission
where code = 'ALL_FUNCTIONS';

INSERT INTO `m_appuser` (`id`, `office_id`, `username`, `firstname`, `lastname`, `password`, `email`, 
`firsttime_login_remaining`, `nonexpired`, `nonlocked`, `nonexpired_credentials`, `enabled`) 
VALUES 
(1,1,'mifos','App','Administrator','5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a','demomfi@mifos.org','\0','','','','');


INSERT INTO `m_appuser_role` (`appuser_id`, `role_id`) VALUES (1,1);


-- Add in permissions for any special datatables added in base reference data
-- This needs to always happen at end of the script

/* add a create, read, update and delete permission for each registered datatable */
insert into m_permission(grouping, `code`, entity_name, action_name)
select 'datatable', concat('CREATE_', r.registered_table_name), r.registered_table_name, 'CREATE'
from x_registered_table r;

insert into m_permission(grouping, `code`, entity_name, action_name)
select 'datatable', concat('READ_', r.registered_table_name), r.registered_table_name, 'READ'
from x_registered_table r;

insert into m_permission(grouping, `code`, entity_name, action_name)
select 'datatable', concat('UPDATE_', r.registered_table_name), r.registered_table_name, 'UPDATE'
from x_registered_table r;

insert into m_permission(grouping, `code`, entity_name, action_name)
select 'datatable', concat('DELETE_', r.registered_table_name), r.registered_table_name, 'DELETE'
from x_registered_table r;


/* regardless of inserted permission settings above, no permissions (transactions) are preselected as being part of the maker-checker process
so, just set the flag to false... the end-user can decide which permissions should be maker-checkerable
*/
update m_permission set can_maker_checker = false;