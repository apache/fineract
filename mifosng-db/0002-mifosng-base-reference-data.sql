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
('loan_status_id',600,'Closed','Closed'),('loan_status_id',700,'Overpaid','Overpaid'),
('loan_transaction_strategy_id',1,'mifos-standard-strategy','Mifos style'),('loan_transaction_strategy_id',2,'heavensfamily-strategy','Heavensfamily'),
('loan_transaction_strategy_id',3,'creocore-strategy','Creocore'),('loan_transaction_strategy_id',4,'rbi-india-strategy','RBI (India)'),
('processing_result_enum',0,'invalid','Invalid'),('processing_result_enum',1,'processed','Processed'),
('processing_result_enum',2,'awaiting.approval','Awaiting Approval'),('processing_result_enum',3,'rejected','Rejected'),
('repayment_period_frequency_enum',0,'Days','Days'),('repayment_period_frequency_enum',1,'Weeks','Weeks'),
('repayment_period_frequency_enum',2,'Months','Months'),('term_period_frequency_enum',0,'Days','Days'),
('term_period_frequency_enum',1,'Weeks','Weeks'),('term_period_frequency_enum',2,'Months','Months'),('term_period_frequency_enum',3,'Years','Years');

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

-- create single code and code value for client identifiers
INSERT INTO `m_code`
(`id`,`code_name`, `is_system_defined`) VALUES (1,'Customer Identifier',1);

INSERT INTO `m_code_value`
(`id`,`code_id`,`code_value`,`order_position`)
VALUES (1,1,'Passport number',0);

INSERT INTO `m_office` (`id`, `parent_id`, `hierarchy`, `external_id`, `name`, `opening_date`) 
VALUES 
(1,NULL,'.','1','Head Office','2009-01-01');

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

-- ========= reports =========

INSERT INTO `stretchy_parameter` VALUES (1,'FullReportList',NULL,'n/a','n/a','n/a','n/a','Y',NULL,NULL,'select  r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\r\n  \n\nrp.parameter_id, rp.report_parameter_name, p.parameter_name\r\n  from stretchy_report r\r\n  left join stretchy_report_parameter rp on rp.report_id = r.report_id\r\n  \n\nleft join stretchy_parameter p on p.parameter_id = rp.parameter_id\r\n  where r.use_report is true\r\n  and exists\r\n  (\r\n select \'f\'\r\n  from m_appuser_role ur \n\n\r\n  join m_role r on r.id = ur.role_id\r\n  join m_role_permission rp on rp.role_id = r.id\r\n  join m_permission p on p.id = rp.permission_id\r\n  where \n\nur.appuser_id = ${currentUserId}\r\n  and (p.code in (\'ALL_FUNCTIONS_READ\', \'ALL_FUNCTIONS\') or p.code = concat(\"READ_\", r.report_name))\r\n )\r\n  order by \n\nr.report_category, r.report_name, rp.parameter_id'),(2,'FullParameterList',NULL,'n/a','n/a','n/a','n/a','Y',NULL,NULL,'select parameter_name, parameter_variable, \n\nparameter_label, parameter_displayType, \r\nparameter_FormatType, parameter_default, selectOne,  selectAll\r\nfrom stretchy_parameter p\r\nwhere special is null\r\n\n\norder by parameter_id'),(3,'reportCategoryList',NULL,'n/a','n/a','n/a','n/a','Y',NULL,NULL,'select  r.report_id, r.report_name, r.report_type, r.report_subtype, \n\nr.report_category,\r\n  rp.parameter_id, rp.report_parameter_name, p.parameter_name\r\n  from stretchy_report r\r\n  left join stretchy_report_parameter rp on \n\nrp.report_id = r.report_id\r\n  left join stretchy_parameter p on p.parameter_id = rp.parameter_id\r\n  where r.report_category = \'${reportCategory}\'\r\n  and \n\nr.use_report is true\r\n  and exists\r\n  (\r\n select \'f\'\r\n  from m_appuser_role ur \r\n  join m_role r on r.id = ur.role_id\r\n  join m_role_permission rp on \n\nrp.role_id = r.id\r\n  join m_permission p on p.id = rp.permission_id\r\n  where ur.appuser_id = ${currentUserId}\r\n  and (p.code in (\'ALL_FUNCTIONS_READ\', \n\n\'ALL_FUNCTIONS\') or p.code = concat(\"READ_\", r.report_name))\r\n )\r\n  order by r.report_category, r.report_name, rp.parameter_id'),(5,'OfficeIdSelectOne','officeId','Office','select','number','0',NULL,'Y',NULL,'select id, \r\nconcat(substring(\"........................................\", 1, \r\n   \n\n((LENGTH(`hierarchy`) - LENGTH(REPLACE(`hierarchy`, \'.\', \'\')) - 1) * 4)), \r\n   `name`) as tc\r\nfrom m_office\r\nwhere hierarchy like concat\n\n(\'${currentUserHierarchy}\', \'%\')\r\norder by hierarchy'),(6,'loanOfficerIdSelectAll','loanOfficerId','Loan Officer','select','number','0',NULL,'Y','Y','(select id, \n\ndisplay_name as `Name` from m_staff\nwhere is_loan_officer = true)\r\nunion all\r\n(select -10, \'-\')\r\norder by 2'),(10,'currencyIdSelectAll','currencyId','Currency','select','number','0',NULL,'Y','Y','select `code`, `name`\r\nfrom m_organisation_currency\r\norder by `code`'),(11,'currencyIdSelectOne','currencyId','Currency','select','number','0',NULL,'Y',NULL,'select `code`, `name`\r\nfrom m_organisation_currency\r\norder by `code`'),(20,'fundIdSelectAll','fundId','Fund','select','number','0',NULL,'Y','Y','(select id, `name`\r\nfrom m_fund)\r\nunion all\r\n(select -10, \'-\')\r\norder by 2'),(25,'loanProductIdSelectAll','loanProductId','Product','select','number','0',NULL,'Y','Y','select id, `name`\r\nfrom m_product_loan\r\norder by 2'),(40,'startDateSelect','startDate','startDate','date','date','today',NULL,NULL,NULL,NULL),(41,'endDateSelect','endDate','endDate','date','date','today',NULL,NULL,NULL,NULL);
INSERT INTO `stretchy_report` VALUES (1,'Client Listing','Table',NULL,'Client','select ounder.`name` as \"Office/Branch\", c.account_no as \"Client Account No.\",  \r\nc.display_name as \"Name\",  \n\nc.joined_date as \"Joined\", c.external_id as \"External Id\"\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand \n\nounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\nwhere o.id = ${officeId}\r\nand c.is_deleted=0\r\n\n\norder by ounder.hierarchy, c.account_no','Individual Client Report\r\n\r\nLists the small number of defined fields on the client table.  Would expect to copy this \n\nreport and add any \'one to one\' additional data for specific tenant needs.\r\n\r\nCan be run for any size MFI but you\'d expect it only to be run within a branch for \n\nlarger ones.  Depending on how many columns are displayed, there is probably is a limit of about 20/50k clients returned for html display (export to excel doesn\'t \n\nhave that client browser/memory impact).',1,1),(2,'Client Loans Listing','Table',NULL,'Client','select ounder.`name` as \"Office/Branch\", c.account_no as \"Client \n\nAccount No.\", \r\nc.display_name as \"Name\", \r\nlo.display_name as \"Loan Officer\", l.account_no as \"Loan Account No.\", l.external_id as \"External Id\", \r\n\n\np.name as Loan, st.enum_message_property as \"Status\",  \r\nf.`name` as Fund,\r\nifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nl.principal_amount,\n\n\r\nl.arrearstolerance_amount as \"Arrears Tolerance Amount\",\r\nl.number_of_repayments as \"Expected No. Repayments\",\r\nl.annual_nominal_interest_rate as \" Annual \n\nNominal Interest Rate\", \r\nl.nominal_interest_rate_per_period as \"Nominal Interest Rate Per Period\",\r\n\r\nipf.enum_message_property as \"Interest Rate Frequency\n\n\",\r\nim.enum_message_property as \"Interest Method\",\r\nicp.enum_message_property as \"Interest Calculated in Period\",\r\nl.term_frequency as \"Term Frequency\",\n\n\r\ntf.enum_message_property as \"Term Frequency Period\",\r\nl.repay_every as \"Repayment Frequency\",\r\nrf.enum_message_property as \"Repayment Frequency Period\",\n\n\r\nam.enum_message_property as \"Amortization\",\r\n\r\nl.total_charges_due_at_disbursement_derived as \"Total Charges Due At Disbursement\",\r\n\r\ndate( \n\nl.submittedon_date) as Submitted, date(l.approvedon_date) Approved, l.expected_disbursedon_date As \"Expected Disbursal\",\r\ndate(l.expected_firstrepaymenton_date) as \n\n\"Expected First Repayment\", date(l.interest_calculated_from_date) as \"Interest Calculated From\" ,\r\ndate(l.disbursedon_date) as Disbursed, date\n\n(l.expected_maturedon_date) \"Expected Maturity\",\r\ndate(l.maturedon_date) as \"Matured On\", date(l.closedon_date) as Closed,\r\ndate(l.rejectedon_date) as \n\nRejected, date(l.rescheduledon_date) as Rescheduled, \r\ndate(l.withdrawnon_date) as Withdrawn, date(l.writtenoffon_date) \"Written Off\"\r\nfrom m_office o \r\njoin \n\nm_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand ounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on \n\nc.office_id = ounder.id\r\nleft join m_loan l on l.client_id = c.id\r\nleft join m_staff lo on lo.id = l.loan_officer_id\r\nleft join m_product_loan p on p.id = \n\nl.product_id\r\nleft join m_fund f on f.id = l.fund_id\r\nleft join r_enum_value st on st.enum_name = \"loan_status_id\" and st.enum_id = l.loan_status_id\r\nleft join \n\nr_enum_value ipf on ipf.enum_name = \"interest_period_frequency_enum\" and ipf.enum_id = l.interest_period_frequency_enum\r\nleft join r_enum_value im on im.enum_name \n\n= \"interest_method_enum\" and im.enum_id = l.interest_method_enum\r\nleft join r_enum_value tf on tf.enum_name = \"term_period_frequency_enum\" and tf.enum_id = \n\nl.term_period_frequency_enum\r\nleft join r_enum_value icp on icp.enum_name = \"interest_calculated_in_period_enum\" and icp.enum_id = \n\nl.interest_calculated_in_period_enum\r\nleft join r_enum_value rf on rf.enum_name = \"repayment_period_frequency_enum\" and rf.enum_id = \n\nl.repayment_period_frequency_enum\r\nleft join r_enum_value am on am.enum_name = \"amortization_method_enum\" and am.enum_id = l.amortization_method_enum\r\n\r\nleft \n\njoin m_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand \n\n(l.product_id = \"${loanProductId}\" or \"-1\" = \"${loanProductId}\")\r\nand (ifnull(l.loan_officer_id, -10) = \"${loanOfficerId}\" or \"-1\" = \n\n\"${loanOfficerId}\")\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\norder by ounder.hierarchy, 2 , l.id','Individual Client Report\r\n\r\nPretty \n\nwide report that lists the basic details of client loans.  \r\n\r\nCan be run for any size MFI but you\'d expect it only to be run within a branch for larger ones.  \n\nThere is probably is a limit of about 20/50k clients returned for html display (export to excel doesn\'t have that client browser/memory impact).',1,1),(5,'Loans Awaiting Disbursal','Table',NULL,'Loan Portfolio','SELECT ounder.`name` as \"Office/Branch\", lo.display_name as \"Loan Officer\", \r\nc.display_name as \"Name\", l.account_no as \"Loan Account No.\", pl.`name` as \"Product\", \r\nf.`name` as Fund, ifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nl.principal_amount as Principal,  \r\ndate(l.approvedon_date) \"Approved\", l.expected_disbursedon_date \"Expected Disbursal\"\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand ounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_product_loan pl on pl.id = l.product_id\r\nleft join m_staff lo on lo.id = l.loan_officer_id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand (l.product_id = \"${loanProductId}\" or \"-1\" = \"${loanProductId}\")\r\nand (ifnull(l.loan_officer_id, -10) = \"${loanOfficerId}\" or \"-1\" = \"${loanOfficerId}\")\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\nand l.loan_status_id = 200\r\norder by ounder.hierarchy, l.expected_disbursedon_date,  c.display_name','Individual Client Report',1,1),(6,'Loans Awaiting Disbursal Summary','Table',NULL,'Loan Portfolio','SELECT ounder.`name` \n\nas \"Office/Branch\",  pl.`name` as \"Product\", \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  f.`name` as Fund,\r\nsum(l.principal_amount) as \n\nPrincipal\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand ounder.hierarchy like concat\n\n(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_product_loan pl on pl.id = \n\nl.product_id\r\nleft join m_staff lo on lo.id = l.loan_officer_id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\n\n\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand (l.product_id = \"${loanProductId}\" or \"-1\" = \n\n\"${loanProductId}\")\r\nand (ifnull(l.loan_officer_id, -10) = \"${loanOfficerId}\" or \"-1\" = \"${loanOfficerId}\")\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 \n\n= ${fundId})\r\nand l.loan_status_id = 200\r\ngroup by ounder.hierarchy, pl.`name`, l.currency_code,  f.`name`\r\norder by ounder.hierarchy, pl.`name`, \n\nl.currency_code,  f.`name`','Individual Client Report',1,1),(7,'Loans Awaiting Disbursal Summary by Month','Table',NULL,'Loan Portfolio','SELECT ounder.`name` as \n\n\"Office/Branch\",  pl.`name` as \"Product\", \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nyear(l.expected_disbursedon_date) as \"Year\", \n\nmonthname(l.expected_disbursedon_date) as \"Month\",\r\nsum(l.principal_amount) as Principal\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like \n\nconcat(o.hierarchy, \'%\')\r\nand ounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on \n\nl.client_id = c.id\r\njoin m_product_loan pl on pl.id = l.product_id\r\nleft join m_staff lo on lo.id = l.loan_officer_id\r\nleft join m_currency cur on cur.code = \n\nl.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand \n\n(l.product_id = \"${loanProductId}\" or \"-1\" = \"${loanProductId}\")\r\nand (ifnull(l.loan_officer_id, -10) = \"${loanOfficerId}\" or \"-1\" = \n\n\"${loanOfficerId}\")\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\nand l.loan_status_id = 200\r\ngroup by ounder.hierarchy, pl.`name`, \n\nl.currency_code, year(l.expected_disbursedon_date), month(l.expected_disbursedon_date)\r\norder by ounder.hierarchy, pl.`name`, l.currency_code, year\n\n(l.expected_disbursedon_date), month(l.expected_disbursedon_date)','Individual Client Report',1,1),(10,'Active Loans Portfolio Status','Table',NULL,'Loan','select \n\nounder.`name` as \"Office/Branch\", lo.display_name as \"Loan Officer\", c.display_name as \"Name\", \r\np.`name` as Loan, f.`name` as Fund, l.account_no as \"Loan \n\nAccount No\",\r\nl.disbursedon_date as Disbursed, ifnull(cur.display_symbol, l.currency_code) as Currency,\r\nsum(r.principal_amount - ifnull\n\n(r.principal_completed_derived, 0)) as \"Principal Outstanding\",\r\nsum(r.interest_amount - ifnull(r.interest_completed_derived, 0)) as \"Interest Outstanding\",\r\n\n\n\r\nif(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) as \"Days Overdue\",   \r\nmin(r.installment) as \"First Overdue Installment\n\n\",\r\nmin(r.duedate) as \"First Overdue Installment Date\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, \n\n0))\r\n            , 0)) as \"Principal Overdue\",\r\nsum(if(r.duedate <= curdate(), \r\n        (ifnull(r.interest_amount, 0) - ifnull(r.interest_completed_derived, \n\n0))\r\n            , 0)) as \"Interest Overdue\"\r\n\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand \n\nounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\nleft join \n\nm_staff lo on lo.id = l.loan_officer_id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\nleft join m_product_loan \n\np on p.id = l.product_id\r\nleft join m_loan_repayment_schedule r on r.loan_id = l.id\r\n                                        and r.completed_derived is false\r\n\n\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand (l.product_id = \"${loanProductId}\" or \"-1\" = \n\n\"${loanProductId}\")\r\nand (ifnull(l.loan_officer_id, -10) = \"${loanOfficerId}\" or \"-1\" = \"${loanOfficerId}\")\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 \n\n= ${fundId})\r\nand l.loan_status_id = 300\r\ngroup by l.id\r\norder by ounder.hierarchy, p.`name`, l.currency_code, c.display_name,  l.account_no','Individual Client \n\nReport',1,1),(11,'Active Loans Summary per Branch','Table',NULL,'Loan Portfolio','select ounder.`name` as \"Office/Branch\",\r\nifnull(cur.display_symbol, l.currency_code) as Currency,\r\ncount(distinct(c.id)) as \"No. of Clients\", count(distinct(l.id)) as \"No. of Active Loans\",\r\ncount(distinct(if(r.duedate <= curdate(), \r\n		if(r.principal_amount - ifnull(r.principal_completed_derived, 0) > 0, l.id, null), null)\r\n  )) as \"No. of Loans in Arrears\",\r\n\r\nsum(l.principal_amount) as \"Total Loans Disbursed\",\r\nsum(ifnull(r.principal_completed_derived, 0)) as \"Total Principal Repaid\",\r\nsum(ifnull(r.interest_completed_derived, 0)) as \"Total Interest Repaid\",\r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Total Principal Outstanding\",\r\nsum(ifnull(r.interest_amount, 0) - ifnull(r.interest_completed_derived, 0)) as \"Total Interest utstanding\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) as \"Total Principal in Arrears\",\r\ncast(round((sum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) * 100) / \r\n            sum(r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 2) as char)\r\n            as \"Portfolio at Risk %\"\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand ounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nleft join m_loan_repayment_schedule r on r.loan_id = l.id\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand l.loan_status_id = 300\r\ngroup by ounder.hierarchy, l.currency_code\r\norder by ounder.hierarchy, l.currency_code',NULL,1,1),(15,'Portfolio at Risk','Table',NULL,'Loan Portfolio','select  ifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Outstanding\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) as \"Principal Overdue\",\r\n            cast(round(\r\n    (sum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) * 100) / sum(r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 2) as char)\r\n            as \"Portfolio at Risk %\"\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand ounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin  m_loan l on l.client_id = c.id\r\nleft join m_staff lo on lo.id = l.loan_officer_id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\nleft join m_product_loan p on p.id = l.product_id\r\nleft join m_loan_repayment_schedule r on r.loan_id = l.id\r\n                                        and r.completed_derived is false\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand (l.product_id = \"${loanProductId}\" or \"-1\" = \"${loanProductId}\")\r\nand (ifnull(l.loan_officer_id, -10) = \"${loanOfficerId}\" or \"-1\" = \"${loanOfficerId}\")\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\nand l.loan_status_id = 300\r\ngroup by l.currency_code\r\norder by l.currency_code',NULL,1,1),(16,'Portfolio at Risk by Branch','Table',NULL,'Loan Portfolio','select  concat(substring(\"........................................\", 1, \r\n   ((LENGTH(ounder.`hierarchy`) - LENGTH(REPLACE(ounder.`hierarchy`, \'.\', \'\')) - 1) * 4)), \r\nounder.`name`) as \"Office/Branch\",\r\nifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Outstanding\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) as \"Principal Overdue\",\r\n    cast(round(\r\n    (sum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0)) , 0)) * 100) / \r\n            sum(r.principal_amount - ifnull(r.principal_completed_derived, 0)), 2) as char) \r\n	as \"Portfolio at Risk %\"\r\n            \r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand ounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin  m_loan l on l.client_id = c.id\r\nleft join m_staff lo on lo.id = l.loan_officer_id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\nleft join m_product_loan p on p.id = l.product_id\r\nleft join m_loan_repayment_schedule r on r.loan_id = l.id\r\n                                        and r.completed_derived is false\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand (l.product_id = \"${loanProductId}\" or \"-1\" = \"${loanProductId}\")\r\nand (ifnull(l.loan_officer_id, -10) = \"${loanOfficerId}\" or \"-1\" = \"${loanOfficerId}\")\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\nand l.loan_status_id = 300\r\ngroup by ounder.hierarchy, l.currency_code\r\norder by ounder.hierarchy, l.currency_code',NULL,1,1),(20,'Funds Disbursed Between Dates Summary','Table',NULL,'Fund','select ifnull(f.`name`, \'-\') as Fund,  ifnull(cur.display_symbol, l.currency_code) as Currency, \r\nround(sum(l.principal_amount), 4) as disbursed_amount\r\nfrom m_office ounder \r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_currency cur on cur.`code` = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\nwhere disbursedon_date between \'${startDate}\' and \'${endDate}\'\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\nand (l.currency_code = \'${currencyId}\' or \'-1\' = \'${currencyId}\')\r\nand ounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\ngroup by ifnull(f.`name`, \'-\') , ifnull(cur.display_symbol, l.currency_code)\r\norder by ifnull(f.`name`, \'-\') , ifnull(cur.display_symbol, l.currency_code)',NULL,1,1),(21,'Funds Disbursed Between Dates Summary by Office','Table',NULL,'Fund','select ounder.`name` as \"Office/Branch\", \n\nifnull(f.`name`, \'-\') as Fund,  ifnull(cur.display_symbol, l.currency_code) as Currency, round(sum(l.principal_amount), 4) as disbursed_amount\r\nfrom m_office o\r\n\n\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\nand ounder.hierarchy like concat(\'${currentUserHierarchy}\', \'%\')\r\njoin m_client c \n\non c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_currency cur on cur.`code` = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\n\n\nwhere disbursedon_date between \'${startDate}\' and \'${endDate}\'\r\nand o.id = ${officeId}\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\nand \n\n(l.currency_code = \'${currencyId}\' or \'-1\' = \'${currencyId}\')\r\ngroup by ounder.`name`,  ifnull(f.`name`, \'-\') , ifnull(cur.display_symbol, \n\nl.currency_code)\r\norder by ounder.`name`,  ifnull(f.`name`, \'-\') , ifnull(cur.display_symbol, l.currency_code)',NULL,1,1),(48,'Balance Sheet','Pentaho',NULL,'Accounting',NULL,'Balance Sheet',1,0),(49,'Income Statement','Pentaho',NULL,'Accounting',NULL,'Profit and Loss Statement',1,0),(50,'Trial Balance','Pentaho',NULL,'Accounting',NULL,'Trial Balance Report',1,0);
INSERT INTO `stretchy_report_parameter` VALUES (1,5,NULL),(2,5,NULL),(2,6,NULL),(2,10,NULL),(2,20,NULL),(2,25,NULL),(5,5,NULL),(5,6,NULL),(5,10,NULL),(5,20,NULL),(5,25,NULL),(6,5,NULL),(6,6,NULL),(6,10,NULL),(6,20,NULL),(6,25,NULL),(7,5,NULL),(7,6,NULL),(7,10,NULL),(7,20,NULL),(7,25,NULL),(10,5,NULL),(10,6,NULL),(10,10,NULL),(10,20,NULL),(10,25,NULL),(11,5,NULL),(11,10,NULL),(15,5,NULL),(15,6,NULL),(15,10,NULL),(15,20,NULL),(15,25,NULL),(16,5,NULL),(16,6,NULL),(16,10,NULL),(16,20,NULL),(16,25,NULL),(20,10,NULL),(20,20,NULL),(20,40,NULL),(20,41,NULL),(21,5,NULL),(21,10,NULL),(21,20,NULL),(21,40,NULL),(21,41,NULL),(48,5,'branch'),(48,41,'date'),(49,5,'branch'),(49,40,'fromDate'),(49,41,'toDate'),(50,5,'branch'),(50,40,'fromDate'),(50,41,'toDate');




-- Add in permissions for any special datatables or reports added in base reference data
-- This needs to always happen at end of the script
/* add a read permission for each defined report */
insert into m_permission(grouping, `code`, entity_name, action_name)
select 'report', concat('READ_', sr.report_name), 
sr.report_name, 'READ'
from stretchy_report sr;

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