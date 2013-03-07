-- currency symbols may not apply through command line on windows so use a different client like mysql workbench

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
('interest_calculated_in_period_enum',0,'Daily','Daily'),('interest_calculated_in_period_enum',1,'Same as repayment period','Same as repayment period'),
('interest_method_enum',0,'Declining Balance','Declining Balance'),('interest_method_enum',1,'Flat','Flat'),
('interest_period_frequency_enum',2,'Per month','Per month'),('interest_period_frequency_enum',3,'Per year','Per year'),
('loan_status_id',100,'Submitted and awaiting approval','Submitted and awaiting approval'),('loan_status_id',200,'Approved','Approved'),
('loan_status_id',300,'Active','Active'),('loan_status_id',400,'Withdrawn by client','Withdrawn by client'),('loan_status_id',500,'Rejected','Rejected'),
('loan_status_id',600,'Closed','Closed'),('loan_status_id',601,'Written-Off','Written-Off'),('loan_status_id',602,'Rescheduled','Rescheduled'),('loan_status_id',700,'Overpaid','Overpaid'),
('loan_transaction_strategy_id',1,'mifos-standard-strategy','Mifos style'),('loan_transaction_strategy_id',2,'heavensfamily-strategy','Heavensfamily'),
('loan_transaction_strategy_id',3,'creocore-strategy','Creocore'),('loan_transaction_strategy_id',4,'rbi-india-strategy','RBI (India)'),
('processing_result_enum',0,'invalid','Invalid'),('processing_result_enum',1,'processed','Processed'),
('processing_result_enum',2,'awaiting.approval','Awaiting Approval'),('processing_result_enum',3,'rejected','Rejected'),
('repayment_period_frequency_enum',0,'Days','Days'),('repayment_period_frequency_enum',1,'Weeks','Weeks'),
('repayment_period_frequency_enum',2,'Months','Months'),('term_period_frequency_enum',0,'Days','Days'),
('term_period_frequency_enum',1,'Weeks','Weeks'),('term_period_frequency_enum',2,'Months','Months'),('term_period_frequency_enum',3,'Years','Years');

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
VALUES ('transaction_type_enum', '8', 'Recovery Repayment', 'Recovery Repayment');

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('transaction_type_enum', '9', 'Waive Charges', 'Waive Charges');


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

INSERT INTO `m_group_level` (`id`, `parent_id`, `super_parent`, `level_name`, `recursable`, `can_have_clients`) VALUES (1, NULL, 1, 'Center', 1, 0);
INSERT INTO `m_group_level` (`id`, `parent_id`, `super_parent`, `level_name`, `recursable`, `can_have_clients`) VALUES (2, 1, 0, 'Group', 0, 1);

-- ========= roles and permissions =========
/*
this scripts removes all current m_role_permission and m_permission entries
and then inserts new m_permission entries and just one m_role_permission entry
which gives the role (id 1 - super user) an ALL_FUNCTIONS permission

If you had other roles set up with specific permissions you will have to set up their permissions again.
*/
truncate `m_role_permission`;
truncate `m_permission`;
truncate `x_registered_table`;

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
('organisation', 'READ_DEPOSITPRODUCT', 'DEPOSITPRODUCT', 'READ', '0'),
('organisation', 'CREATE_DEPOSITPRODUCT', 'DEPOSITPRODUCT', 'CREATE', '1'),
('organisation', 'CREATE_DEPOSITPRODUCT_CHECKER', 'DEPOSITPRODUCT', 'CREATE', '0'),
('organisation', 'UPDATE_DEPOSITPRODUCT', 'DEPOSITPRODUCT', 'UPDATE', '1'),
('organisation', 'UPDATE_DEPOSITPRODUCT_CHECKER', 'DEPOSITPRODUCT', 'UPDATE', '0'),
('organisation', 'DELETE_DEPOSITPRODUCT', 'DEPOSITPRODUCT', 'DELETE', '1'),
('organisation', 'DELETE_DEPOSITPRODUCT_CHECKER', 'DEPOSITPRODUCT', 'DELETE', '0'),
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
('portfolio', 'READ_DEPOSITNOTE', 'DEPOSITNOTE', 'READ', '0'),
('portfolio', 'CREATE_DEPOSITNOTE', 'DEPOSITNOTE', 'CREATE', '1'),
('portfolio', 'UPDATE_DEPOSITNOTE', 'DEPOSITNOTE', 'UPDATE', '1'),
('portfolio', 'DELETE_DEPOSITNOTE', 'DEPOSITNOTE', 'DELETE', '1'),
('portfolio', 'CREATE_DEPOSITNOTE_CHECKER', 'DEPOSITNOTE', 'CREATE', '0'),
('portfolio', 'UPDATE_DEPOSITNOTE_CHECKER', 'DEPOSITNOTE', 'UPDATE', '0'),
('portfolio', 'DELETE_DEPOSITNOTE_CHECKER', 'DEPOSITNOTE', 'DELETE', '0'),
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
('portfolio', 'CREATE_LOANCHARGE', 'LOANCHARGE', 'CREATE', '1'),
('portfolio', 'CREATE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'CREATE', '0'),
('portfolio', 'UPDATE_LOANCHARGE', 'LOANCHARGE', 'UPDATE', '1'),
('portfolio', 'UPDATE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'UPDATE', '0'),
('portfolio', 'DELETE_LOANCHARGE', 'LOANCHARGE', 'DELETE', '1'),
('portfolio', 'DELETE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'DELETE', '0'),
('portfolio', 'WAIVE_LOANCHARGE', 'LOANCHARGE', 'WAIVE', '1'),
('portfolio', 'WAIVE_LOANCHARGE_CHECKER', 'LOANCHARGE', 'WAIVE', '0'),
('portfolio', 'READ_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'READ', '0'),
('portfolio', 'CREATE_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'CREATE', '1'),
('portfolio', 'CREATE_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'CREATE', '0'),
('portfolio', 'UPDATE_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'UPDATE', '1'),
('portfolio', 'UPDATE_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'UPDATE', '0'),
('portfolio', 'DELETE_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'DELETE', '1'),
('portfolio', 'DELETE_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'DELETE', '0'),
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
('transaction_deposit', 'APPROVE_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'APPROVE', '1'),
('transaction_deposit', 'REJECT_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'REJECT', '1'),
('transaction_deposit', 'WITHDRAW_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'WITHDRAW', '1'),
('transaction_deposit', 'APPROVALUNDO_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'APPROVALUNDO', '1'),
('transaction_deposit', 'WITHDRAWAL_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'WITHDRAWAL', '1'),
('transaction_deposit', 'INTEREST_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'INTEREST', '1'),
('transaction_deposit', 'RENEW_DEPOSITACCOUNT', 'DEPOSITACCOUNT', 'RENEW', '1'),
('transaction_deposit', 'APPROVE_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'APPROVE', '0'),
('transaction_deposit', 'REJECT_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'REJECT', '0'),
('transaction_deposit', 'WITHDRAW_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'WITHDRAW', '0'),
('transaction_deposit', 'APPROVALUNDO_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'APPROVALUNDO', '0'),
('transaction_deposit', 'WITHDRAWAL_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'WITHDRAWAL', '0'),
('transaction_deposit', 'INTEREST_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'INTEREST', '0'),
('transaction_deposit', 'RENEW_DEPOSITACCOUNT_CHECKER', 'DEPOSITACCOUNT', 'RENEW', '0');

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

update m_permission
set can_maker_checker = true
where grouping not in ('special', 'report')
and action_name != 'READ';

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


-- create single code and code value for client identifiers
INSERT INTO `m_code`
(`code_name`, `is_system_defined`) 
VALUES 
('Customer Identifier',1),
('LoanCollateral',1),
('LoanPurpose',1),
('Gender',1),
('YesNo',1);

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Passport', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Customer Identifier";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Id', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Customer Identifier";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Drivers License', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Customer Identifier";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Any Other Id Type', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Customer Identifier";