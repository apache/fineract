/* Mifos individual lending demo

This demo shows a small MFI moving from paper/excel based system onto MIS adding in their 'historic details'.

Highlights:
   - ability to launch instance through public AMI on amazon
   - ability to mirror organisation structure, through offices, roles and users accounts.
   - ability model loan products
   - ability to input historic client and loan data
   - flexibility around 'automatic loan schedule generation'

Organisation: Demo MFI
   - 1 head office: Demo MFI Head Office
   - Currency: CFA Franc BCEAO (0 digits after decimal currency)
   - The organisation (head office) was founded or commenced activity in 2009.
*/
INSERT INTO `mifosngprovider`.`org_organisation`
(`id`, `contact_email`, `contact_name`, `name`, `opening_date`)
VALUES
(1,'demomfi@mifos.org','App Administrator','Demo MFI','2009-01-01');

/*One currency must be set by default*/
INSERT INTO `mifosngprovider`.`org_organisation_currency`
(`id`, `org_id`, `code`, `decimal_places`,`name`, `display_symbol`, `internationalized_name_code`)
VALUES (1,1,'XOF',0,'CFA Franc BCEAO', 'CFA', 'currency.XOF');

INSERT INTO `mifosngprovider`.`org_office`
(`id`, `org_id`, `parent_id`, `hierarchy`, `external_id`, `name`, `opening_date`)
VALUES 
(1,1,NULL,'.','1','Demo MFI Head Office','2009-01-01');


/* Mifos individual lending demo

All known application permissions to date are assigned to organisation.

*/
INSERT INTO `mifosngprovider`.`admin_permission`
(`id`,
`org_id`,
`group_enum`,
`code`,
`default_name`,
`default_description`
)
VALUES
(1, 1, 1, 'USER_ADMINISTRATION_SUPER_USER_ROLE', 'User administration ALL', 'An application user will have permission to execute all tasks related to user administration.'),
(2, 1, 2, 'ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE', 'Organisation adminsitration ALL', 'An application user will have permission to execute all tasks related to organisation administration.'),
(3, 1, 3, 'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE', 'Portfolio management ALL','An application user will have permission to execute all tasks related to portfolio management.'),
(4, 1, 4, 'REPORTING_SUPER_USER_ROLE', 'Reporting ALL','An application user will have permission to execute and view all reports.'),
(5, 1, 3, 'CAN_SUBMIT_NEW_LOAN_APPLICATION_ROLE', 'Can submit new loan application','Allows an application user to sumit new loan application.'),
(6, 1, 3, 'CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE', 'Can submit historic loan application','Allows an application user to sumit new loan application where the submitted on date is in the past.'),
(7, 1, 3, 'CAN_APPROVE_LOAN_ROLE', 'Can approve loan application','Allows an application user to approve a loan application.'),
(8, 1, 3, 'CAN_APPROVE_LOAN_IN_THE_PAST_ROLE', 'Can approve loan application in the past','Allows an application user to approve a loan application where the approval date is in the past.'),
(9, 1, 3, 'CAN_REJECT_LOAN_ROLE', 'Can reject loan application','Allows an application user to reject a loan application.'),
(10, 1, 3, 'CAN_REJECT_LOAN_IN_THE_PAST_ROLE', 'Can reject loan application in the past','Allows an application user to reject a loan application where the rejected date is in the past.'),
(11, 1, 3, 'CAN_WITHDRAW_LOAN_ROLE', 'Can withdraw loan application','Allows an application user to mark loan application as withdrawn by client.'),
(12, 1, 3, 'CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE', 'Can withdraw loan application in the past','Allows an application user to mark loan application as withdrawn by client where the withdran on date is in the past.'),
(13, 1, 3, 'CAN_DELETE_LOAN_THAT_IS_SUBMITTED_AND_NOT_APPROVED', 'Can delete submitted loan application','Allows an application user to complete delete the loan application if it is submitted but not approved.'),
(14, 1, 3, 'CAN_UNDO_LOAN_APPROVAL_ROLE', 'Can undo loan approval','Allows an application user to undo a loan approval.'),
(15, 1, 3, 'CAN_DISBURSE_LOAN_ROLE', 'Can disburse loan','Allows an application user to disburse a loan application.'),
(16, 1, 3, 'CAN_DISBURSE_LOAN_IN_THE_PAST_ROLE', 'Can disburse loan in the past','Allows an application user to disburse a loan where the disbursement date is in the past.'),
(17, 1, 3, 'CAN_UNDO_LOAN_DISBURSAL_ROLE', 'Can undo loan disbursal','Allows an application user to undo a loan disbursal if not payments already made.'),
(18, 1, 3, 'CAN_MAKE_LOAN_REPAYMENT_LOAN_ROLE', 'Can enter a repayment against a loan','Allows an application user to enter a repayment on the loan.'),
(19, 1, 3, 'CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE', 'Can enter a repayment against a loan in the past','Allows an application user to enter a repayment on the loan where the repayment date is in the past.'),
(20, 1, 3, 'CAN_ENROLL_NEW_CLIENT_ROLE', 'Can add a new client.','Allows an application user to add a new client.');


/** Mifos individual lending demo

Insert in several users, password is demo for all users but mifos user.

*/
INSERT INTO `admin_appuser` (`id`,
`org_id`,
`office_id`,
`username`,
`firstname`,
`lastname`,
`password`,
`email`,
`firsttime_login_remaining`,
`nonexpired`,
`nonlocked`,
`nonexpired_credentials`,
`enabled`,
`createdby_id`,
`created_date`,
`lastmodified_date`,
`lastmodifiedby_id`)
VALUES (1,1,1,'mifos','App','Administrator','5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a','demomfi@mifos.org','\0','','','','',NULL,NULL,NULL,NULL),(2,1,1,'super1','super','user','13bac23bfc7fe5257536dea35b9ed361c15eb2b440a140983db2eb6b1539c9cf','fake@email.com','\0','','','','',1,'2012-04-12 17:49:44','2012-04-12 17:51:00',2),(3,1,1,'manager1','manager','user','e85323fd00ff4129313ee45562c6c21215c03abce1dc17a7e843df7c182e98f2','fake@email.com','\0','','','','',1,'2012-04-12 17:56:03','2012-04-12 17:57:35',3),(4,1,1,'fieldofficer1','Field','Officer','ac51896ceae9573ffe3b49d426dbf063ddfa3a6e95437b14af4ce2d18b266c6f','fake@email.com','\0','','','','',1,'2012-04-12 17:59:48','2012-04-12 18:03:09',4),
(5,1,1,'dataentry1','Data Entry','User','7e86696ecd68e3168401feecbca2d23913eaf313362840ec40268b826fc95781','fake@email.com','\0','','','','',1,'2012-04-12 18:04:13','2012-04-12 18:05:36',5),(6,1,1,'committee1','Committee','Member','8087b7099cb145e5907c49359270db915a5c896f514ab4a82d83d46aeacc0d30','fake@email.com','\0','','','','',1,'2012-04-12 18:14:29','2012-04-12 18:16:17',6);


/* Mifos individual lending demo
 
Following Roles exist relating to business roles within 'Demo MFI' organisation.
*/
INSERT INTO `mifosngprovider`.`admin_role` (`id`, `org_id`,`name`,`description`, `createdby_id`, `created_date`, `lastmodified_date`, `lastmodifiedby_id`) VALUES 
(1,1,'Super user','This role provides all application permissions.',NULL,NULL,NULL,NULL),
(2,1,'Field officer','A field officer role allows the user to add client and loans and view reports but nothing else.',1,'2012-04-12 15:59:48','2012-04-12 15:59:48',1),
(3,1,'Data Entry (Portfolio only)','This role allows a user full permissions around client and loan functionality but nothing else.',1,'2012-04-12 16:01:25','2012-04-12 16:01:25',1),
(4,1,'Credit Committe Member','This role allows a user to approve reject or withdraw loans (with reporting).',1,'2012-04-12 16:11:25','2012-04-12 16:11:25',1),(5,1,'Manager','This role allows a manager to do anything related to portfolio management and also view all reports.',1,'2012-04-12 17:02:11','2012-04-12 17:02:11',1);


INSERT INTO `admin_role_permission` VALUES (1,1),(1,2),(1,3),(1,4),(2,4),(2,5),(2,6),(2,13),(2,20),(3,3),(4,4),(4,7),(4,8),(4,9),(4,10),(4,11),(4,12),(5,3),(5,4);

INSERT INTO `admin_appuser_role` VALUES (1,1),(2,1),(3,5),(4,2),(5,3),(6,4);



/**
 Setup products delivered by MFI (Agricultural, etc)
*/
INSERT INTO `mifosngprovider`.`portfolio_product_loan`
(`id`,
`org_id`,
`currency_code`,
`currency_digits`,
`principal_amount`,
`arrearstolerance_amount`,
`name`,
`description`,
`nominal_interest_rate_per_period`,
`interest_period_frequency_enum`,
`annual_nominal_interest_rate`,
`interest_method_enum`,
`interest_calculated_in_period_enum`,
`repay_every`,
`repayment_period_frequency_enum`,
`number_of_repayments`,
`amortization_method_enum`,
`flexible_repayment_schedule`,
`interest_rebate`,
`createdby_id`,
`created_date`,
`lastmodified_date`,
`lastmodifiedby_id`)
VALUES 
(1,1,'XOF',0,'100000.000000','1000.000000','Agricultural Loan','An agricultural loan given to farmers to help buy crop, stock and machinery. With an arrears tolerance setting of 1,000 CFA, loans are not marked as \'in arrears\' or \'in bad standing\' if the amount outstanding is less than this. Interest rate is described using monthly percentage rate (MPR) even though the loan typically lasts a year and requires one repayment (typically at time when farmer sells crop)','1.750000',2,'21.000000',0,1,12,2,1,1,'\0','\0',1,'2012-04-12 22:14:34','2012-04-12 22:14:34',1);


INSERT INTO `mifosngprovider`.`portfolio_client`
(`id`,
`org_id`,
`office_id`,
`external_id`,
`firstname`,
`lastname`,
`joining_date`,
`createdby_id`,
`created_date`,
`lastmodified_date`,
`lastmodifiedby_id`)
VALUES 
(1,1,1,NULL,'Patrick','O\'Meara','2009-01-04',1,'2012-04-12 22:07:44','2012-04-12 22:07:44',1),
(2,1,1,NULL,'Dennis','O\'Meara','2009-01-04',1,'2012-04-12 22:15:39','2012-04-12 22:15:39',1),
(3,1,1,NULL,'John','Smith','2009-01-11',1,'2012-04-12 22:16:30','2012-04-12 22:16:30',1),
(4,1,1,NULL,'Jimmy','O\'Meara','2009-01-11',1,'2012-04-12 22:17:01','2012-04-12 22:17:01',1),
(5,1,1,NULL,NULL,'Sunnyville vegetable growers Ltd','2009-01-24',1,'2012-04-12 22:19:18','2012-04-12 22:19:18',1);

/**
Note relating to some of the clients
*/
INSERT INTO `mifosngprovider`.`portfolio_note`
(`id`,
`org_id`,
`client_id`,
`loan_id`,
`loan_transaction_id`,
`note_type_enum`,
`note`,
`created_date`,
`createdby_id`,
`lastmodified_date`,
`lastmodifiedby_id`) 
VALUES 
(1,1,5,NULL,NULL,100,'A business comprising of four entrepreneurs who grow vegetables in Sunnyville. They have been working together since mid 2008 and are looking to grow the business.','2012-04-12 22:21:00',1,'2012-04-12 22:21:00',1);


/**
first five loans first for five clients
*/
INSERT INTO `portfolio_loan` VALUES 
(1,1,NULL,1,1,600,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,10,2,1,1,'\0','\0','0.000000','2009-01-05 12:33:10','2009-01-08 12:33:16','2009-01-15',NULL,NULL,'2009-01-15','2009-11-15',NULL,'2009-11-13 00:00:00',NULL,NULL,NULL,NULL,1,'2012-04-13 12:33:10','2012-04-13 12:34:40',1),(2,1,NULL,2,1,600,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,11,2,1,1,'\0','\0','0.000000','2009-01-08 13:08:16','2009-01-12 13:08:25','2009-01-15',NULL,NULL,'2009-01-15','2009-12-15','2009-12-15','2009-12-15 00:00:00',NULL,NULL,NULL,NULL,1,'2012-04-13 13:08:16','2012-04-13 13:11:53',1),(4,1,NULL,3,1,600,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,12,2,1,1,'\0','\0','0.000000','2009-01-13 13:14:30','2009-01-15 13:14:54','2009-01-22',NULL,NULL,'2009-01-22','2010-01-22','2010-01-07','2010-01-07 00:00:00',NULL,NULL,NULL,NULL,1,'2012-04-13 13:14:30','2012-04-13 13:15:32',1),(5,1,NULL,4,1,600,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,8,2,1,1,'\0','\0','0.000000','2009-01-12 13:23:32','2009-01-15 13:23:44','2009-01-22',NULL,NULL,'2009-01-22','2009-09-22','2009-09-22','2009-09-22 00:00:00',NULL,NULL,NULL,NULL,1,'2012-04-13 13:23:32','2012-04-13 13:24:01',1),(6,1,NULL,5,1,600,'XOF',0,'300000.000000','1000.000000','1.750000',2,'21.000000',0,1,3,2,4,1,'\0','\0','0.000000','2009-01-26 13:30:54','2009-01-27 13:31:02','2009-01-29','2009-05-01','2009-02-01','2009-01-29','2010-02-01','2010-02-01','2010-02-01 00:00:00',NULL,NULL,NULL,NULL,1,'2012-04-13 13:30:54','2012-04-13 13:33:05',1);

INSERT INTO `portfolio_loan_repayment_schedule` VALUES 
(1,1,1,'2009-11-15',1,'100000.000000','100000.000000','17500.000000','17500.000000','',1,'2012-04-13 12:33:11','2012-04-13 12:34:40',1),
(2,1,2,'2009-12-15',1,'100000.000000','100000.000000','19250.000000','19250.000000','',1,'2012-04-13 13:08:17','2012-04-13 13:11:53',1),
(4,1,4,'2010-01-22',1,'100000.000000','100000.000000','21000.000000','21000.000000','',1,'2012-04-13 13:14:30','2012-04-13 13:15:32',1),
(5,1,5,'2009-09-22',1,'100000.000000','100000.000000','14000.000000','14000.000000','',1,'2012-04-13 13:23:32','2012-04-13 13:24:01',1),
(6,1,6,'2009-05-01',1,'69345.000000','69345.000000','15750.000000','15750.000000','',1,'2012-04-13 13:30:54','2012-04-13 13:31:55',1),
(7,1,6,'2009-08-01',2,'72986.000000','72986.000000','12109.000000','12109.000000','',1,'2012-04-13 13:30:54','2012-04-13 13:32:03',1),
(8,1,6,'2009-11-01',3,'76817.000000','76817.000000','8278.000000','8278.000000','',1,'2012-04-13 13:30:54','2012-04-13 13:32:10',1),
(9,1,6,'2010-02-01',4,'80852.000000','80755.000000','4245.000000','4245.000000','\0',1,'2012-04-13 13:30:54','2012-04-13 13:32:30',1);

INSERT INTO `portfolio_loan_transaction` VALUES 
(1,1,1,1,NULL,'2009-01-15','100000.000000',1,'2012-04-13 12:33:21','2012-04-13 12:33:21',1),
(2,1,1,2,NULL,'2009-11-13','117500.000000',1,'2012-04-13 12:34:40','2012-04-13 12:34:40',1),
(3,1,2,1,NULL,'2009-01-15','100000.000000',1,'2012-04-13 13:08:30','2012-04-13 13:08:30',1),
(4,1,2,2,NULL,'2009-11-03','99000.000000',1,'2012-04-13 13:08:47','2012-04-13 13:08:47',1),
(5,1,2,2,NULL,'2009-12-15','20250.000000',1,'2012-04-13 13:11:53','2012-04-13 13:11:53',1),
(6,1,4,1,NULL,'2009-01-22','100000.000000',1,'2012-04-13 13:15:10','2012-04-13 13:15:10',1),
(7,1,4,2,NULL,'2010-01-07','121000.000000',1,'2012-04-13 13:15:32','2012-04-13 13:15:32',1),
(8,1,5,1,NULL,'2009-01-22','100000.000000',1,'2012-04-13 13:23:52','2012-04-13 13:23:52',1),
(9,1,5,2,NULL,'2009-09-22','114000.000000',1,'2012-04-13 13:24:01','2012-04-13 13:24:01',1),
(10,1,6,1,NULL,'2009-01-29','300000.000000',1,'2012-04-13 13:31:27','2012-04-13 13:31:27',1),
(11,1,6,2,NULL,'2009-05-01','85095.000000',1,'2012-04-13 13:31:55','2012-04-13 13:31:55',1),
(12,1,6,2,NULL,'2009-08-01','85095.000000',1,'2012-04-13 13:32:03','2012-04-13 13:32:03',1),
(13,1,6,2,NULL,'2009-11-01','85095.000000',1,'2012-04-13 13:32:10','2012-04-13 13:32:10',1),
(14,1,6,2,NULL,'2010-02-01','85000.000000',1,'2012-04-13 13:32:29','2012-04-13 13:32:29',1),
(15,1,6,4,NULL,'2010-02-01','97.000000',1,'2012-04-13 13:33:05','2012-04-13 13:33:05',1);