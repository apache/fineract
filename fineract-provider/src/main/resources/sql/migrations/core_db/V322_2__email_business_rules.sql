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

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES
('report', 'READ_Active Clients - Email', 'Active Clients - Email', 'READ', 0),
('report', 'READ_Prospective Clients - Email', 'Prospective Clients - Email', 'READ', 0),
('report', 'READ_Active Loan Clients - Email', 'Active Loan Clients - Email', 'READ', 0),
('report', 'READ_Loans in arrears - Email', 'Loans in arrears - Email', 'READ', 0),
('report', 'READ_Loans disbursed to clients - Email', 'Loans disbursed to clients - Email', 'READ', 0),
('report', 'READ_Loan payments due - Email', 'Loan payments due - Email', 'READ', 0),
('report', 'READ_Dormant Prospects - Email', 'Dormant Prospects - Email', 'READ', 0),
('report', 'READ_Active Group Leaders - Email', 'Active Group Leaders - Email', 'READ', 0),
('report', 'READ_Loan Payments Due (Overdue Loans) - Email', 'Loan Payments Due (Overdue Loans) - Email', 'READ', 0),
('report', 'READ_Loan Payments Received (Active Loans) - Email', 'Loan Payments Received (Active Loans) - Email', 'READ', 0),
('report', 'READ_Loan Payments Received (Overdue Loans) - Email', 'Loan Payments Received (Overdue Loans)  - Email', 'READ', 0),
('report', 'READ_Loan Fully Repaid - Email', 'Loan Fully Repaid - Email', 'READ', 0),
('report', 'READ_Loans Outstanding after final instalment date - Email', 'Loans Outstanding after final instalment date - Email', 'READ', 0),
('report', 'READ_Happy Birthday - Email', 'Happy Birthday - Email', 'READ', 0),
('report', 'READ_Loan Rejected - Email', 'Loan Rejected - Email', 'READ', 0),
('report', 'READ_Loan Approved - Email', 'Loan Approved - Email', 'READ', 0),
('report', 'READ_Loan Repayment - Email', 'Loan Repayment - Email', 'READ', 0);