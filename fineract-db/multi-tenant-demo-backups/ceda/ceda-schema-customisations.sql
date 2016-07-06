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

﻿DELETE FROM `ref_loan_transaction_processing_strategy` WHERE id in (1, 3, 4);

INSERT INTO `m_code` (`code_name`, `is_system_defined`)
VALUES
('FieldOfEmployment', '0'),
('EducationLevel', '0'),
('MaritalStatus', '0'),
('PovertyStatus', '0');

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Banker', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "FieldOfEmployment";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.SoftwareDeveloper', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "FieldOfEmployment";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.University', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "EducationLevel";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Secondary', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "EducationLevel";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Primary', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "EducationLevel";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Married', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "MaritalStatus";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Single', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "MaritalStatus";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Divorced', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "MaritalStatus";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Widow', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "MaritalStatus";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.PovertyStatus.Band1', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "PovertyStatus";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.PovertyStatus.Band2', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "PovertyStatus";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.PovertyStatus.Band3', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "PovertyStatus";

-- ======= ADD in loan purspose and collateral values to enable this functionality
INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.House', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "LoanCollateral";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Television', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "LoanCollateral";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Gold', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "LoanCollateral";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Agriculture', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "LoanPurpose";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Manufacturing', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "LoanPurpose";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.HousingImprovement', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "LoanPurpose";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Male', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Gender";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Female', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Gender";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Yes', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "YesNo";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.No', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "YesNo";



DROP TABLE IF EXISTS `client additional data`;
CREATE TABLE `client additional data` (
  `client_id` bigint(20) NOT NULL,
  `Gender_cd` int(11) NOT NULL,
  `Date of Birth` date NOT NULL,
  `Home address` text NOT NULL,
  `Telephone number` varchar(20) NOT NULL,
  `Telephone number (2nd)` varchar(20) NOT NULL,
  `Email address` varchar(50) NOT NULL,
  `EducationLevel_cd` int(11) NOT NULL,
  `MaritalStatus_cd` int(11) NOT NULL,
  `Number of children` int(11) NOT NULL,
  `Citizenship` varchar(50) NOT NULL,
  `PovertyStatus_cd` int(11) NOT NULL,
  `YesNo_cd_Employed` int(11) NOT NULL,
  `FieldOfEmployment_cd_Field of employment` int(11) DEFAULT NULL,
  `Employer name` varchar(50) DEFAULT NULL,
  `Number of years` int(11) DEFAULT NULL,
  `Monthly salary` decimal(19,6) DEFAULT NULL,
  `YesNo_cd_Self employed` int(11) NOT NULL,
  `FieldOfEmployment_cd_Field of self-employment` int(11) DEFAULT NULL,
  `Business address` text,
  `Number of employees` int(11) DEFAULT NULL,
  `Monthly salaries paid` decimal(19,6) DEFAULT NULL,
  `Monthly net income of business activity` decimal(19,6) DEFAULT NULL,
  `Monthly rent` decimal(19,6) DEFAULT NULL,
  `Other income generating activities` varchar(100) DEFAULT NULL,
  `YesNo_cd_Bookkeeping` int(11) DEFAULT NULL,
  `YesNo_cd_Loans with other institutions` int(11) NOT NULL,
  `From whom` varchar(100) DEFAULT NULL,
  `Amount` decimal(19,6) DEFAULT NULL,
  `Interest rate pa` decimal(19,6) DEFAULT NULL,
  `Number of people depending on overal income` int(11) NOT NULL,
  `YesNo_cd_Bank account` int(11) NOT NULL,
  `YesNo_cd_Business plan provided` int(11) NOT NULL,
  `YesNo_cd_Access to internet` int(11) DEFAULT NULL,
  `Introduced by` varchar(100) DEFAULT NULL,
  `Known to introducer since` varchar(100) NOT NULL,
  `Last visited by` varchar(100) DEFAULT NULL,
  `Last visited on` date NOT NULL,
  PRIMARY KEY (`client_id`),
  CONSTRAINT `FK_client_additional_data` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `impact measurement`;
CREATE TABLE `impact measurement` (
  `loan_id` bigint(20) NOT NULL,
  `YesNo_cd_RepaidOnSchedule` int(11) NOT NULL,
  `ReasonNotRepaidOnSchedule` text DEFAULT NULL,
  `How was Loan Amount Invested` text NOT NULL,
  `Additional Income Generated` decimal(19,6) NOT NULL,
  `Additional Income Used For` text NOT NULL,
  `YesNo_cd_NewJobsCreated` int(11) NOT NULL,
  `Number of Jobs Created` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `FK_impact measurement` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `loan additional data`;
CREATE TABLE `loan additional data` (
  `loan_id` bigint(20) NOT NULL,
  `PurposeOfLoan_cd` int(11) NOT NULL,
  `CollateralType_cd` int(11) NOT NULL,
  `Collateral notes` text NOT NULL,
  `YesNo_cd_Guarantor` int(11) NOT NULL,
  `Guarantor name` varchar(100) DEFAULT NULL,
  `Guarantor relation` varchar(100) DEFAULT NULL,
  `Guarantor address` varchar(100) DEFAULT NULL,
  `Guarantor telephone number` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `FK_loan_additional_data` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- datatables mapping
INSERT INTO `x_registered_table`
(`registered_table_name`,
`application_table_name`)
VALUES
('client additional data', 'm_client'),
('impact measurement', 'm_loan'),
('loan additional data', 'm_loan');

-- make sure permissions created for registered datatables
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


-- ==== Chart of Accounts =====
truncate `acc_gl_account`;
INSERT INTO `acc_gl_account` VALUES 
(1,'Petty Cash Balances',NULL,'11100',0,1,2,1,NULL),
(2,'Cash in Valut 1',NULL,'11101',0,1,1,1,NULL),
(3,'Bank Balances',NULL,'11200',0,1,2,1,NULL),
(4,'Centenary Opening Account',NULL,'11201',0,1,1,1,NULL),
(5,'Centenary Expense Account',NULL,'11202',0,1,1,1,NULL),
(6,'Centenary USD Account',NULL,'11203',0,1,1,1,NULL),
(7,'Loans and Advances',NULL,'13100',0,1,2,1,NULL),
(8,'Loans to Clients',NULL,'13101',0,1,1,1,NULL),
(9,'Outstanding Interest',NULL,'13102',0,1,1,1,NULL),
(10,'Outstanding Late Payment Interest',NULL,'13103',0,1,1,1,NULL),
(11,'Outstanding Bank Fees to be collected',NULL,'13104',0,1,1,1,NULL),
(12,'WriteOff Accounts',NULL,'13200',0,1,2,1,NULL),
(13,'Write-offs (use for funds coming in)',NULL,'13201',0,1,1,1,NULL),
(14,'Write-offs outstanding principal',NULL,'13202',0,1,1,1,NULL),
(15,'Write-offs outstanding interest',NULL,'13203',0,1,1,1,NULL),
(16,'Write-offs collected bank fees',NULL,'13204',0,1,1,1,NULL),
(17,'Write-offs hardware/furniture',NULL,'13205',0,1,1,1,NULL),
(18,'Fixed Assets',NULL,'14100',0,1,2,1,NULL),
(19,'Office Equipment',NULL,'14101',0,1,1,1,NULL),
(20,'Suspense Items (unidentified deposits)',NULL,'15000',0,1,2,1,NULL),
(21,'Assets',NULL,'10000',0,1,2,1,NULL),
(22,'Liabilities',NULL,'20000',0,1,2,2,NULL),
(23,'Shares Account',NULL,'26100',0,1,2,2,NULL),
(24,'Shares Captial',NULL,'26101',0,1,1,2,NULL),
(25,'Donated Equity',NULL,'26300',0,1,2,2,NULL),
(26,'Donated Equity Ameropa Foundation',NULL,'26301',0,1,1,2,NULL),
(27,'Donated Equity e.h',NULL,'26302',0,1,1,2,NULL),
(28,'Overpaid Amounts',NULL,'27000',0,1,2,2,NULL),
(29,'Loss Provision',NULL,'28000',0,1,2,2,NULL),
(30,'Provision Outstanding Principal',NULL,'28001',0,1,1,2,NULL),
(31,'Provision Oustanding Interest',NULL,'28002',0,1,1,2,NULL),(32,'Income',NULL,'30000',0,1,2,4,NULL),
(33,'Interest Income from Loans',NULL,'31100',0,1,2,4,NULL),
(34,'Interest on Loans',NULL,'31101',0,1,1,4,NULL),
(35,'Late Payment Interest',NULL,'31102',0,1,1,4,NULL),
(36,'Income from Micro credit & Lending Activities',NULL,'31300',0,1,2,4,NULL),
(37,'Collected Bank Fees Receivable',NULL,'6201',0,1,1,4,NULL),
(38,'Deposits from Loans Write Off',NULL,'31400',0,1,2,4,NULL),
(39,'Expenditure',NULL,'40000',0,1,2,5,NULL),
(40,'Office Expenditure Account',NULL,'42100',0,1,2,5,NULL),
(41,'Water Charges',NULL,'42102',0,1,1,5,NULL),
(42,'Electricity Charges',NULL,'42103',0,1,1,5,NULL),
(43,'Printing and Stationary',NULL,'42105',0,1,1,5,NULL),
(44,'Office Rent',NULL,'42107',0,1,1,5,NULL),
(45,'Marketing Expense',NULL,'42109',0,1,1,5,NULL),
(46,'Office utilities',NULL,'42112',0,1,1,5,'(supplies, toiletries, kitchen)'),
(47,'Furniture',NULL,'42113',0,1,1,5,NULL),
(48,'CEDA Meeting Expense',NULL,'42114',0,1,1,5,NULL),
(49,'Employee Personal Expsense Account',NULL,'42200',0,1,2,5,NULL),
(50,'Salary Alice',NULL,'42201',0,1,1,5,NULL),
(51,'Salary Irene',NULL,'42202',0,1,1,5,NULL),
(52,'Salary Richard',NULL,'42203',0,1,1,5,NULL),
(53,'Salary Loan Officer TBA',NULL,'42204',0,1,1,5,NULL),
(54,'Medical Insurance Alice & Family',NULL,'42205',0,1,1,5,NULL),
(55,'Medical Insurance Irene',NULL,'42206',0,1,1,5,NULL),
(56,'Medical Insurance Richard',NULL,'42207',0,1,1,5,NULL),
(57,'Medical Insurance Loan Officer TBA',NULL,'42208',0,1,1,5,NULL),
(58,'PAYE all employees',NULL,'42209',0,1,1,5,NULL),
(59,'NSSF all employees',NULL,'42210',0,1,1,5,NULL),
(60,'Lunch Allowances all employees',NULL,'42211',0,1,1,5,NULL),
(61,'IT software and maintenance',NULL,'42300',0,1,2,5,NULL),
(62,'Mifos maintenance contract 12 months',NULL,'42301',0,1,1,5,NULL),
(63,'VPS Contract 12 months',NULL,'42302',0,1,1,5,NULL),
(64,'Bulk SMS Service',NULL,'42303',0,1,1,5,NULL),
(65,'Support Accounting Software',NULL,'42304',0,1,1,5,NULL),
(66,'Mifos Instance Setup',NULL,'42305',0,1,1,5,NULL),
(67,'Misc support expense',NULL,'42306',0,1,1,5,NULL),
(68,'Warid Mobile Line',NULL,'42307',0,1,1,5,NULL),
(69,'Landline',NULL,'42308',0,1,1,5,NULL),
(70,'Modem Alice',NULL,'42309',0,1,1,5,NULL),
(71,'Modem Irene',NULL,'42310',0,1,1,5,NULL),
(72,'Modem Richard',NULL,'42311',0,1,1,5,NULL),
(73,'Repairs',NULL,'42312',0,1,1,5,NULL),
(74,'Airtime Expenses',NULL,'42400',0,1,2,5,NULL),
(75,'Airtime Alice',NULL,'42401',0,1,1,5,NULL),
(76,'Airtime Richard',NULL,'42402',0,1,1,5,NULL),
(77,'Airtime Loan Office TBA',NULL,'42403',0,1,1,5,NULL),
(78,'Special Airtime Alice',NULL,'42404',0,1,1,5,NULL),
(79,'Transportation',NULL,'42500',0,1,2,5,NULL),
(80,'Flat monthly transportation cost',NULL,'42501',0,1,1,5,NULL),
(81,'Faciliation cost for Richard',NULL,'42502',0,1,1,5,NULL),
(82,'Faciliation cost for Loan Officer TBA',NULL,'42503',0,1,1,5,NULL),
(83,'Consultancy Expenses',NULL,'42600',0,1,2,5,NULL),
(84,'Audit Fees',NULL,'42601',0,1,1,5,NULL),
(85,'Legal Fees',NULL,'42602',0,1,1,5,NULL),
(86,'Miscellaneous Expenses Account',NULL,'42700',0,1,2,5,NULL),
(87,'MFI License',NULL,'42703',0,1,1,5,NULL),
(88,'Sundy Expenses',NULL,'42704',0,1,1,5,NULL),
(89,'Bank Fees',NULL,'42800',0,1,2,5,NULL),
(90,'Bank Charges Operating Account',NULL,'42801',0,1,1,5,NULL),
(91,'Bank Charges Expense Account',NULL,'42802',0,1,1,5,NULL),
(92,'E.H Account',NULL,'42900',0,1,2,5,NULL),
(93,'Airtime',NULL,'42901',0,1,1,5,NULL),
(94,'Modem',NULL,'42902',0,1,1,5,NULL),
(95,'Meals',NULL,'42903',0,1,1,5,NULL),
(96,'Transportation',NULL,'42904',0,1,1,5,NULL),
(97,'Miscellaneous',NULL,'42905',0,1,1,5,NULL);