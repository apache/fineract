-- create roles and associate with permissions
INSERT INTO `admin_role` (`id`, `name`, `description`, `createdby_id`, `created_date`, `lastmodified_date`, `lastmodifiedby_id`) 
VALUES 
(1,'Super user','This role provides all application permissions.',NULL,NULL,NULL,NULL),
(2,'Field officer','A field officer role allows the user to add client and loans and view reports but nothing else.',1,'2012-04-12 15:59:48','2012-04-12 15:59:48',1),
(3,'Data Entry (Portfolio only)','This role allows a user full permissions around client and loan functionality but nothing else.',1,'2012-04-12 16:01:25','2012-04-12 16:01:25',1),
(4,'Credit Committe Member','This role allows a user to approve reject or withdraw loans (with reporting).',1,'2012-04-12 16:11:25','2012-04-12 16:11:25',1),
(5,'Manager','This role allows a manager to do anything related to portfolio management and also view all reports.',1,'2012-04-12 17:02:11','2012-04-12 17:02:11',1);

INSERT INTO `admin_role_permission` (`role_id`, `permission_id`) 
VALUES 
(1,1),(1,2),(1,3),(1,4),(2,4),(2,5),(2,6),(2,13),(2,20),(3,3),(4,4),(4,7),(4,8),(4,9),(4,10),(4,11),(4,12),(5,3),(5,4);


-- insert office data required by appuser table/data
INSERT INTO `org_office` (`id`, `parent_id`, `hierarchy`, `external_id`, `name`, `opening_date`, `createdby_id`, `created_date`, `lastmodified_date`, `lastmodifiedby_id`) 
VALUES 
(1,NULL,'.','1','Head Office','2009-01-01',NULL,NULL,'2012-07-13 17:04:20',1),
(2,1,'.2.','2','sub branch 1','2012-01-02',1,'2012-04-14 05:42:40','2012-04-16 11:47:05',1),
(3,1,'.3.','3','sub branch 2','2012-03-01',1,'2012-04-16 04:12:02','2012-04-16 11:47:14',1),
(4,1,'.4.',NULL,'sub branch 3','2012-04-17',1,'2012-04-17 06:01:10','2012-04-17 06:01:10',1);

-- insert user admin related data
INSERT INTO `admin_appuser` (`id`, `office_id`, `username`, `firstname`, `lastname`, `password`, `email`, 
`firsttime_login_remaining`, `nonexpired`, `nonlocked`, `nonexpired_credentials`, `enabled`, 
`createdby_id`, `created_date`, `lastmodified_date`, `lastmodifiedby_id`) 
VALUES 
(1,1,'defaultadmin','App','Administrator','5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a',
'demomfi@mifos.org','\0','','','','',NULL,NULL,NULL,NULL);


INSERT INTO `admin_appuser_role` (`appuser_id`, `role_id`) VALUES (1,1);


-- insert remaining fund/currencies/product data

INSERT INTO `org_organisation_currency` (`id`, `code`, `decimal_places`, `name`, `display_symbol`, `internationalized_name_code`, `createdby_id`, `created_date`, `lastmodified_date`, `lastmodifiedby_id`) 
VALUES 
(17,'KES',2,'Kenyan Shilling','KSh','currency.KES',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),
(18,'BND',2,'Brunei Dollar','BND','currency.BND',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),
(19,'LBP',2,'Lebanese Pound','L','currency.LBP',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),
(20,'GHC',2,'Ghana Cedi','GHC','currency.GHC',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),
(21,'USD',2,'US Dollar','$','currency.USD',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),
(22,'XOF',0,'CFA Franc BCEAO','CFA','currency.XOF',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1);


INSERT INTO `portfolio_product_loan` (`id`, `currency_code`, `currency_digits`, `principal_amount`, `arrearstolerance_amount`, `name`, `description`, `nominal_interest_rate_per_period`, `interest_period_frequency_enum`, `annual_nominal_interest_rate`, `interest_method_enum`, `repay_every`, `repayment_period_frequency_enum`, `number_of_repayments`, `amortization_method_enum`, `flexible_repayment_schedule`, `interest_rebate`, `createdby_id`, `created_date`, `lastmodified_date`, `lastmodifiedby_id`, `interest_calculated_in_period_enum`, `fund_id`) 
VALUES 
(1,'XOF',0,'100000.000000','1000.000000','Agricultural Loan','An agricultural loan given to farmers to help buy crop, stock and machinery. With an arrears tolerance setting of 1,000 CFA, loans are not marked as \'in arrears\' or \'in bad standing\' if the amount outstanding is less than this. Interest rate is described using monthly percentage rate (MPR) even though the loan typically lasts a year and requires one repayment (typically at time when farmer sells crop)','1.750000',2,'21.000000',0,12,2,1,1,'\0','\0',1,'2012-04-12 22:14:34','2012-04-12 22:14:34',1,1,NULL);



-- inserting client

INSERT INTO `portfolio_client` (`id`, `office_id`, `external_id`, `firstname`, `lastname`, `joining_date`, `createdby_id`, `created_date`, `lastmodified_date`, `lastmodifiedby_id`) 
VALUES 
(1,1,NULL,'Willie','O\'Meara','2009-01-04',1,'2012-04-12 22:07:44','2012-04-12 22:07:44',1),
(2,1,NULL,'Declan','Browne','2009-01-04',1,'2012-04-12 22:15:39','2012-04-12 22:15:39',1),
(3,1,NULL,'Ja','Fallon','2009-01-11',1,'2012-04-12 22:16:30','2012-04-12 22:16:30',1),
(4,1,NULL,'Peter','Lambert','2009-01-11',1,'2012-04-12 22:17:01','2012-04-12 22:17:01',1),
(5,1,NULL,NULL,'Sunnyville vegetable growers Ltd','2009-01-24',1,'2012-04-12 22:19:18','2012-04-12 22:19:18',1),
(6,2,NULL,'Jacques','Lee','2012-04-03',1,'2012-04-14 06:09:22','2012-04-14 06:09:22',1),
(7,1,NULL,'Kalilou','Traor','2009-02-04',1,'2012-04-14 09:38:11','2012-04-14 09:38:11',1),
(8,1,NULL,'Sidi','Kon','2009-02-11',1,'2012-04-14 09:45:43','2012-04-14 09:45:43',1),
(9,1,NULL,'Moustapha','Yattabar','2009-02-18',1,'2012-04-14 10:24:20','2012-04-14 10:24:20',1),
(10,1,NULL,NULL,'Mali fruit sales ltd.','2009-02-18',1,'2012-04-14 10:25:37','2012-04-14 10:25:37',1),
(11,1,NULL,NULL,'Djenne co-op group','2009-02-25',1,'2012-04-14 10:31:03','2012-04-14 10:31:03',1);