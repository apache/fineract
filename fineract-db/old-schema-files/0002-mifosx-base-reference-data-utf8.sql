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

﻿-- currency symbols may not apply through command line on windows so use a different client like mysql workbench

INSERT INTO `ref_loan_transaction_processing_strategy`
(`id`,`code`,`name`)
VALUES
(1, 'mifos-standard-strategy', 'Mifos style'),
(2, 'heavensfamily-strategy', 'Heavensfamily'),
(3, 'creocore-strategy', 'Creocore'),
(4, 'rbi-india-strategy', 'RBI (India)');

INSERT INTO `c_configuration`
(`name`, `enabled`)
VALUES 
('maker-checker', 0);

INSERT INTO `r_enum_value` 
VALUES 
('amortization_method_enum',0,'Equal principle payments','Equal principle payments'),
('amortization_method_enum',1,'Equal installments','Equal installments'),
('interest_calculated_in_period_enum',0,'Daily','Daily'),
('interest_calculated_in_period_enum',1,'Same as repayment period','Same as repayment period'),
('interest_method_enum',0,'Declining Balance','Declining Balance'),
('interest_method_enum',1,'Flat','Flat'),
('interest_period_frequency_enum',2,'Per month','Per month'),
('interest_period_frequency_enum',3,'Per year','Per year'),
('loan_status_id',100,'Submitted and awaiting approval','Submitted and awaiting approval'),
('loan_status_id',200,'Approved','Approved'),
('loan_status_id',300,'Active','Active'),
('loan_status_id',400,'Withdrawn by client','Withdrawn by client'),
('loan_status_id',500,'Rejected','Rejected'),
('loan_status_id',600,'Closed','Closed'),
('loan_status_id',601,'Written-Off','Written-Off'),
('loan_status_id',602,'Rescheduled','Rescheduled'),
('loan_status_id',700,'Overpaid','Overpaid'),
('loan_transaction_strategy_id',1,'mifos-standard-strategy','Mifos style'),
('loan_transaction_strategy_id',2,'heavensfamily-strategy','Heavensfamily'),
('loan_transaction_strategy_id',3,'creocore-strategy','Creocore'),
('loan_transaction_strategy_id',4,'rbi-india-strategy','RBI (India)'),
('processing_result_enum',0,'invalid','Invalid'),
('processing_result_enum',1,'processed','Processed'),
('processing_result_enum',2,'awaiting.approval','Awaiting Approval'),
('processing_result_enum',3,'rejected','Rejected'),
('repayment_period_frequency_enum',0,'Days','Days'),
('repayment_period_frequency_enum',1,'Weeks','Weeks'),
('repayment_period_frequency_enum',2,'Months','Months'),
('term_period_frequency_enum',0,'Days','Days'),
('term_period_frequency_enum',1,'Weeks','Weeks'),
('term_period_frequency_enum',2,'Months','Months'),
('term_period_frequency_enum',3,'Years','Years');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '1', 'Disbursement', 'Disbursement');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '2', 'Repayment', 'Repayment');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '3', 'Contra', 'Contra');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '4', 'Waive Interest', 'Waive Interest');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '5', 'Repayment At Disbursement', 'Repayment At Disbursement');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '6', 'Write-Off', 'Write-Off');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '7', 'Marked for Rescheduling', 'Marked for Rescheduling');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '8', 'Recovery Repayment', 'Recovery Repayment');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '9', 'Waive Charges', 'Waive Charges');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '10', 'Apply Charges', 'Apply Charges');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '11', 'Apply Interest', 'Apply Interest');

INSERT INTO `m_currency`
(`id`,`code`,`decimal_places`,`display_symbol`,`name`, `internationalized_name_code`)
VALUES 
(1,'AED',2,NULL,'UAE Dirham','currency.AED'),
(2,'AFN',2,NULL,'Afghanistan Afghani','currency.AFN'),
(3,'ALL',2,NULL,'Albanian Lek','currency.ALL'),
(4,'AMD',2,NULL,'Armenian Dram','currency.AMD'),
(5,'ANG',2,NULL,'Netherlands Antillian Guilder','currency.ANG'),
(6,'AOA',2,NULL,'Angolan Kwanza','currency.AOA'),
(7,'ARS',2,'$','Argentine Peso','currency.ARS'),
(8,'AUD',2,'A$','Australian Dollar','currency.AUD'),
(9,'AWG',2,NULL,'Aruban Guilder','currency.AWG'),
(10,'AZM',2,NULL,'Azerbaijanian Manat','currency.AZM'),
(11,'BAM',2,NULL,'Bosnia and Herzegovina Convertible Marks','currency.BAM'),
(12,'BBD',2,NULL,'Barbados Dollar','currency.BBD'),
(13,'BDT',2,NULL,'Bangladesh Taka','currency.BDT'),
(14,'BGN',2,NULL,'Bulgarian Lev','currency.BGN'),
(15,'BHD',3,NULL,'Bahraini Dinar','currency.BHD'),
(16,'BIF',0,NULL,'Burundi Franc','currency.BIF'),
(17,'BMD',2,NULL,'Bermudian Dollar','currency.BMD'),
(18,'BND',2,'B$','Brunei Dollar','currency.BND'),
(19,'BOB',2,'Bs.','Bolivian Boliviano','currency.BOB'),
(20,'BRL',2,'R$','Brazilian Real','currency.BRL'),
(21,'BSD',2,NULL,'Bahamian Dollar','currency.BSD'),
(22,'BTN',2,NULL,'Bhutan Ngultrum','currency.BTN'),
(23,'BWP',2,NULL,'Botswana Pula','currency.BWP'),
(24,'BYR',0,NULL,'Belarussian Ruble','currency.BYR'),
(25,'BZD',2,'BZ$','Belize Dollar','currency.BZD'),
(26,'CAD',2,NULL,'Canadian Dollar','currency.CAD'),
(27,'CDF',2,NULL,'Franc Congolais','currency.CDF'),
(28,'CHF',2,NULL,'Swiss Franc','currency.CHF'),
(29,'CLP',0,'$','Chilean Peso','currency.CLP'),
(30,'CNY',2,NULL,'Chinese Yuan Renminbi','currency.CNY'),
(31,'COP',2,'$','Colombian Peso','currency.COP'),
(32,'CRC',2,'₡','Costa Rican Colon','currency.CRC'),
(33,'CSD',2,NULL,'Serbian Dinar','currency.CSD'),
(34,'CUP',2,'$MN','Cuban Peso','currency.CUP'),
(35,'CVE',2,NULL,'Cape Verde Escudo','currency.CVE'),
(36,'CYP',2,NULL,'Cyprus Pound','currency.CYP'),
(37,'CZK',2,NULL,'Czech Koruna','currency.CZK'),
(38,'DJF',0,NULL,'Djibouti Franc','currency.DJF'),
(39,'DKK',2,NULL,'Danish Krone','currency.DKK'),
(40,'DOP',2,'RD$','Dominican Peso','currency.DOP'),
(41,'DZD',2,NULL,'Algerian Dinar','currency.DZD'),
(42,'EEK',2,NULL,'Estonian Kroon','currency.EEK'),
(43,'EGP',2,NULL,'Egyptian Pound','currency.EGP'),
(44,'ERN',2,NULL,'Eritrea Nafka','currency.ERN'),
(45,'ETB',2,NULL,'Ethiopian Birr','currency.ETB'),
(46,'EUR',2,'€','Euro','currency.EUR'),
(47,'FJD',2,NULL,'Fiji Dollar','currency.FJD'),
(48,'FKP',2,NULL,'Falkland Islands Pound','currency.FKP'),
(49,'GBP',2,NULL,'Pound Sterling','currency.GBP'),
(50,'GEL',2,NULL,'Georgian Lari','currency.GEL'),
(51,'GHC',2,'GHc','Ghana Cedi','currency.GHC'),
(52,'GIP',2,NULL,'Gibraltar Pound','currency.GIP'),
(53,'GMD',2,NULL,'Gambian Dalasi','currency.GMD'),
(54,'GNF',0,NULL,'Guinea Franc','currency.GNF'),
(55,'GTQ',2,'Q','Guatemala Quetzal','currency.GTQ'),
(56,'GYD',2,NULL,'Guyana Dollar','currency.GYD'),
(57,'HKD',2,NULL,'Hong Kong Dollar','currency.HKD'),
(58,'HNL',2,'L','Honduras Lempira','currency.HNL'),
(59,'HRK',2,NULL,'Croatian Kuna','currency.HRK'),
(60,'HTG',2,'G','Haiti Gourde','currency.HTG'),
(61,'HUF',2,NULL,'Hungarian Forint','currency.HUF'),
(62,'IDR',2,NULL,'Indonesian Rupiah','currency.IDR'),
(63,'ILS',2,NULL,'New Israeli Shekel','currency.ILS'),
(64,'INR',2,'₹','Indian Rupee','currency.INR'),
(65,'IQD',3,NULL,'Iraqi Dinar','currency.IQD'),
(66,'IRR',2,NULL,'Iranian Rial','currency.IRR'),
(67,'ISK',0,NULL,'Iceland Krona','currency.ISK'),
(68,'JMD',2,NULL,'Jamaican Dollar','currency.JMD'),
(69,'JOD',3,NULL,'Jordanian Dinar','currency.JOD'),
(70,'JPY',0,NULL,'Japanese Yen','currency.JPY'),
(71,'KES',2,'KSh','Kenyan Shilling','currency.KES'),
(72,'KGS',2,NULL,'Kyrgyzstan Som','currency.KGS'),
(73,'KHR',2,NULL,'Cambodia Riel','currency.KHR'),
(74,'KMF',0,NULL,'Comoro Franc','currency.KMF'),
(75,'KPW',2,NULL,'North Korean Won','currency.KPW'),
(76,'KRW',0,NULL,'Korean Won','currency.KRW'),
(77,'KWD',3,NULL,'Kuwaiti Dinar','currency.KWD'),
(78,'KYD',2,NULL,'Cayman Islands Dollar','currency.KYD'),
(79,'KZT',2,NULL,'Kazakhstan Tenge','currency.KZT'),
(80,'LAK',2,NULL,'Lao Kip','currency.LAK'),
(81,'LBP',2,'L£','Lebanese Pound','currency.LBP'),
(82,'LKR',2,NULL,'Sri Lanka Rupee','currency.LKR'),
(83,'LRD',2,NULL,'Liberian Dollar','currency.LRD'),
(84,'LSL',2,NULL,'Lesotho Loti','currency.LSL'),
(85,'LTL',2,NULL,'Lithuanian Litas','currency.LTL'),
(86,'LVL',2,NULL,'Latvian Lats','currency.LVL'),
(87,'LYD',3,NULL,'Libyan Dinar','currency.LYD'),
(88,'MAD',2,NULL,'Moroccan Dirham','currency.MAD'),
(89,'MDL',2,NULL,'Moldovan Leu','currency.MDL'),
(90,'MGA',2,NULL,'Malagasy Ariary','currency.MGA'),
(91,'MKD',2,NULL,'Macedonian Denar','currency.MKD'),
(92,'MMK',2,'K','Myanmar Kyat','currency.MMK'),
(93,'MNT',2,NULL,'Mongolian Tugrik','currency.MNT'),
(94,'MOP',2,NULL,'Macau Pataca','currency.MOP'),
(95,'MRO',2,NULL,'Mauritania Ouguiya','currency.MRO'),
(96,'MTL',2,NULL,'Maltese Lira','currency.MTL'),
(97,'MUR',2,NULL,'Mauritius Rupee','currency.MUR'),
(98,'MVR',2,NULL,'Maldives Rufiyaa','currency.MVR'),
(99,'MWK',2,NULL,'Malawi Kwacha','currency.MWK'),
(100,'MXN',2,'$','Mexican Peso','currency.MXN'),
(101,'MYR',2,NULL,'Malaysian Ringgit','currency.MYR'),
(102,'MZM',2,NULL,'Mozambique Metical','currency.MZM'),
(103,'NAD',2,NULL,'Namibia Dollar','currency.NAD'),
(104,'NGN',2,NULL,'Nigerian Naira','currency.NGN'),
(105,'NIO',2,'C$','Nicaragua Cordoba Oro','currency.NIO'),
(106,'NOK',2,NULL,'Norwegian Krone','currency.NOK'),
(107,'NPR',2,NULL,'Nepalese Rupee','currency.NPR'),
(108,'NZD',2,NULL,'New Zealand Dollar','currency.NZD'),
(109,'OMR',3,NULL,'Rial Omani','currency.OMR'),
(110,'PAB',2,'B/.','Panama Balboa','currency.PAB'),
(111,'PEN',2,'S/.','Peruvian Nuevo Sol','currency.PEN'),
(112,'PGK',2,NULL,'Papua New Guinea Kina','currency.PGK'),
(113,'PHP',2,NULL,'Philippine Peso','currency.PHP'),
(114,'PKR',2,NULL,'Pakistan Rupee','currency.PKR'),
(115,'PLN',2,NULL,'Polish Zloty','currency.PLN'),
(116,'PYG',0,'₲','Paraguayan Guarani','currency.PYG'),
(117,'QAR',2,NULL,'Qatari Rial','currency.QAR'),
(118,'RON',2,NULL,'Romanian Leu','currency.RON'),
(119,'RUB',2,NULL,'Russian Ruble','currency.RUB'),
(120,'RWF',0,NULL,'Rwanda Franc','currency.RWF'),
(121,'SAR',2,NULL,'Saudi Riyal','currency.SAR'),
(122,'SBD',2,NULL,'Solomon Islands Dollar','currency.SBD'),
(123,'SCR',2,NULL,'Seychelles Rupee','currency.SCR'),
(124,'SDD',2,NULL,'Sudanese Dinar','currency.SDD'),
(125,'SEK',2,NULL,'Swedish Krona','currency.SEK'),
(126,'SGD',2,NULL,'Singapore Dollar','currency.SGD'),
(127,'SHP',2,NULL,'St Helena Pound','currency.SHP'),
(128,'SIT',2,NULL,'Slovenian Tolar','currency.SIT'),
(129,'SKK',2,NULL,'Slovak Koruna','currency.SKK'),
(130,'SLL',2,NULL,'Sierra Leone Leone','currency.SLL'),
(131,'SOS',2,NULL,'Somali Shilling','currency.SOS'),
(132,'SRD',2,NULL,'Surinam Dollar','currency.SRD'),
(133,'STD',2,NULL,'Sao Tome and Principe Dobra','currency.STD'),
(134,'SVC',2,NULL,'El Salvador Colon','currency.SVC'),
(135,'SYP',2,NULL,'Syrian Pound','currency.SYP'),
(136,'SZL',2,NULL,'Swaziland Lilangeni','currency.SZL'),
(137,'THB',2,NULL,'Thai Baht','currency.THB'),
(138,'TJS',2,NULL,'Tajik Somoni','currency.TJS'),
(139,'TMM',2,NULL,'Turkmenistan Manat','currency.TMM'),
(140,'TND',3,'DT','Tunisian Dinar','currency.TND'),
(141,'TOP',2,NULL,'Tonga Pa\'anga','currency.TOP'),
(142,'TRY',2,NULL,'Turkish Lira','currency.TRY'),
(143,'TTD',2,NULL,'Trinidad and Tobago Dollar','currency.TTD'),
(144,'TWD',2,NULL,'New Taiwan Dollar','currency.TWD'),
(145,'TZS',2,NULL,'Tanzanian Shilling','currency.TZS'),
(146,'UAH',2,NULL,'Ukraine Hryvnia','currency.UAH'),
(147,'UGX',2,'USh','Uganda Shilling','currency.UGX'),
(148,'USD',2,'$','US Dollar','currency.USD'),
(149,'UYU',2,'$U','Peso Uruguayo','currency.UYU'),
(150,'UZS',2,NULL,'Uzbekistan Sum','currency.UZS'),
(151,'VEB',2,'Bs.F.','Venezuelan Bolivar','currency.VEB'),
(152,'VND',2,NULL,'Vietnamese Dong','currency.VND'),
(153,'VUV',0,NULL,'Vanuatu Vatu','currency.VUV'),
(154,'WST',2,NULL,'Samoa Tala','currency.WST'),
(155,'XAF',0,NULL,'CFA Franc BEAC','currency.XAF'),
(156,'XCD',2,NULL,'East Caribbean Dollar','currency.XCD'),
(157,'XDR',5,NULL,'SDR (Special Drawing Rights)','currency.XDR'),
(158,'XOF',0, 'CFA','CFA Franc BCEAO','currency.XOF'),
(159,'XPF',0,NULL,'CFP Franc','currency.XPF'),
(160,'YER',2,NULL,'Yemeni Rial','currency.YER'),
(161,'ZAR',2, 'R','South African Rand','currency.ZAR'),
(162,'ZMK',2,NULL,'Zambian Kwacha','currency.ZMK'),
(163,'ZWD',2,NULL,'Zimbabwe Dollar','currency.ZWD');
-- ======== end of currencies ==

INSERT INTO `m_organisation_currency` (`id`, `code`, `decimal_places`, `name`, `display_symbol`, `internationalized_name_code`) 
VALUES (21,'USD',2,'US Dollar','$','currency.USD');

INSERT INTO `m_office` (`id`, `parent_id`, `hierarchy`, `external_id`, `name`, `opening_date`) 
VALUES 
(1,NULL,'.','1','Head Office','2009-01-01');

INSERT INTO `m_group_level` (`id`, `parent_id`, `super_parent`, `level_name`, `recursable`, `can_have_clients`) 
VALUES (1, NULL, 1, 'Center', 1, 0);
INSERT INTO `m_group_level` (`id`, `parent_id`, `super_parent`, `level_name`, `recursable`, `can_have_clients`) 
VALUES (2, 1, 0, 'Group', 0, 1);



-- create single code and code value for client identifiers
INSERT INTO `m_code`
(`code_name`, `is_system_defined`) 
VALUES 
('Customer Identifier',1),
('LoanCollateral',1),
('LoanPurpose',1),
('Gender',1),
('YesNo',1),
('GuarantorRelationship',1);

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Passport', 1
from m_code mc
where mc.`code_name` = "Customer Identifier";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Id', 2
from m_code mc
where mc.`code_name` = "Customer Identifier";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Drivers License', 3
from m_code mc
where mc.`code_name` = "Customer Identifier";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Any Other Id Type', 4
from m_code mc
where mc.`code_name` = "Customer Identifier";

-- Adding a few Default Guarantor Relationships
insert into m_code_value (code_id,code_value,order_position) 
	select id,"Spouse",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Parent",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Sibling",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Business Associate",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Other",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";