-- MySQL dump 10.13  Distrib 5.1.62, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: mifostenant-default
-- ------------------------------------------------------
-- Server version	5.1.62-0ubuntu0.11.10.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_appuser`
--

DROP TABLE IF EXISTS `admin_appuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_appuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  `firstname` varchar(100) NOT NULL,
  `lastname` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `firsttime_login_remaining` bit(1) NOT NULL,
  `nonexpired` bit(1) NOT NULL,
  `nonlocked` bit(1) NOT NULL,
  `nonexpired_credentials` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_org` (`username`),
  KEY `FKB3D587CE0DD567A` (`office_id`),
  CONSTRAINT `FKB3D587CE0DD567A` FOREIGN KEY (`office_id`) REFERENCES `org_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_appuser`
--

LOCK TABLES `admin_appuser` WRITE;
/*!40000 ALTER TABLE `admin_appuser` DISABLE KEYS */;
INSERT INTO `admin_appuser` VALUES (1,1,'mifos','App','Administrator','5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a','demomfi@mifos.org','\0','','','','',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `admin_appuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_appuser_role`
--

DROP TABLE IF EXISTS `admin_appuser_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_appuser_role` (
  `appuser_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`appuser_id`,`role_id`),
  KEY `FK7662CE59B4100309` (`appuser_id`),
  KEY `FK7662CE5915CEC7AB` (`role_id`),
  CONSTRAINT `FK7662CE5915CEC7AB` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`),
  CONSTRAINT `FK7662CE59B4100309` FOREIGN KEY (`appuser_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_appuser_role`
--

LOCK TABLES `admin_appuser_role` WRITE;
/*!40000 ALTER TABLE `admin_appuser_role` DISABLE KEYS */;
INSERT INTO `admin_appuser_role` VALUES (1,1);
/*!40000 ALTER TABLE `admin_appuser_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_permission`
--

DROP TABLE IF EXISTS `admin_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_enum` smallint(5) NOT NULL,
  `code` varchar(100) NOT NULL,
  `default_description` varchar(500) NOT NULL,
  `default_name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_permission`
--

LOCK TABLES `admin_permission` WRITE;
/*!40000 ALTER TABLE `admin_permission` DISABLE KEYS */;
INSERT INTO `admin_permission` VALUES (1,1,'USER_ADMINISTRATION_SUPER_USER_ROLE','An application user will have permission to execute all tasks related to user administration.','User administration ALL'),(2,2,'ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE','An application user will have permission to execute all tasks related to organisation administration.','Organisation adminsitration ALL'),(3,3,'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE','An application user will have permission to execute all tasks related to portfolio management.','Portfolio management ALL'),(4,4,'REPORTING_SUPER_USER_ROLE','An application user will have permission to execute and view all reports.','Reporting ALL'),(5,3,'CAN_SUBMIT_NEW_LOAN_APPLICATION_ROLE','Allows an application user to sumit new loan application.','Can submit new loan application'),(6,3,'CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE','Allows an application user to sumit new loan application where the submitted on date is in the past.','Can submit historic loan application'),(7,3,'CAN_APPROVE_LOAN_ROLE','Allows an application user to approve a loan application.','Can approve loan application'),(8,3,'CAN_APPROVE_LOAN_IN_THE_PAST_ROLE','Allows an application user to approve a loan application where the approval date is in the past.','Can approve loan application in the past'),(9,3,'CAN_REJECT_LOAN_ROLE','Allows an application user to reject a loan application.','Can reject loan application'),(10,3,'CAN_REJECT_LOAN_IN_THE_PAST_ROLE','Allows an application user to reject a loan application where the rejected date is in the past.','Can reject loan application in the past'),(11,3,'CAN_WITHDRAW_LOAN_ROLE','Allows an application user to mark loan application as withdrawn by client.','Can withdraw loan application'),(12,3,'CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE','Allows an application user to mark loan application as withdrawn by client where the withdran on date is in the past.','Can withdraw loan application in the past'),(13,3,'CAN_DELETE_LOAN_THAT_IS_SUBMITTED_AND_NOT_APPROVED','Allows an application user to complete delete the loan application if it is submitted but not approved.','Can delete submitted loan application'),(14,3,'CAN_UNDO_LOAN_APPROVAL_ROLE','Allows an application user to undo a loan approval.','Can undo loan approval'),(15,3,'CAN_DISBURSE_LOAN_ROLE','Allows an application user to disburse a loan application.','Can disburse loan'),(16,3,'CAN_DISBURSE_LOAN_IN_THE_PAST_ROLE','Allows an application user to disburse a loan where the disbursement date is in the past.','Can disburse loan in the past'),(17,3,'CAN_UNDO_LOAN_DISBURSAL_ROLE','Allows an application user to undo a loan disbursal if not payments already made.','Can undo loan disbursal'),(18,3,'CAN_MAKE_LOAN_REPAYMENT_LOAN_ROLE','Allows an application user to enter a repayment on the loan.','Can enter a repayment against a loan'),(19,3,'CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE','Allows an application user to enter a repayment on the loan where the repayment date is in the past.','Can enter a repayment against a loan in the past'),(20,3,'CAN_ENROLL_NEW_CLIENT_ROLE','Allows an application user to add a new client.','Can add a new client.');
/*!40000 ALTER TABLE `admin_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_role`
--

DROP TABLE IF EXISTS `admin_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_role`
--

LOCK TABLES `admin_role` WRITE;
/*!40000 ALTER TABLE `admin_role` DISABLE KEYS */;
INSERT INTO `admin_role` VALUES (1,'Super user','This role provides all application permissions.',NULL,NULL,NULL,NULL),(2,'Field officer','A field officer role allows the user to add client and loans and view reports but nothing else.',1,'2012-04-12 15:59:48','2012-04-12 15:59:48',1),(3,'Data Entry (Portfolio only)','This role allows a user full permissions around client and loan functionality but nothing else.',1,'2012-04-12 16:01:25','2012-04-12 16:01:25',1),(4,'Credit Committe Member','This role allows a user to approve reject or withdraw loans (with reporting).',1,'2012-04-12 16:11:25','2012-04-12 16:11:25',1),(5,'Manager','This role allows a manager to do anything related to portfolio management and also view all reports.',1,'2012-04-12 17:02:11','2012-04-12 17:02:11',1);
/*!40000 ALTER TABLE `admin_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_role_permission`
--

DROP TABLE IF EXISTS `admin_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_role_permission` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FK8DEDB04815CEC7AB` (`role_id`),
  KEY `FK8DEDB048103B544B` (`permission_id`),
  CONSTRAINT `FK8DEDB048103B544B` FOREIGN KEY (`permission_id`) REFERENCES `admin_permission` (`id`),
  CONSTRAINT `FK8DEDB04815CEC7AB` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_role_permission`
--

LOCK TABLES `admin_role_permission` WRITE;
/*!40000 ALTER TABLE `admin_role_permission` DISABLE KEYS */;
INSERT INTO `admin_role_permission` VALUES (1,1),(1,2),(1,3),(1,4),(2,4),(2,5),(2,6),(2,13),(2,20),(3,3),(4,4),(4,7),(4,8),(4,9),(4,10),(4,11),(4,12),(5,3),(5,4);
/*!40000 ALTER TABLE `admin_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `org_fund`
--

DROP TABLE IF EXISTS `org_fund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `org_fund` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fund_name_org` (`name`),
  UNIQUE KEY `fund_externalid_org` (`external_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_fund`
--

LOCK TABLES `org_fund` WRITE;
/*!40000 ALTER TABLE `org_fund` DISABLE KEYS */;
INSERT INTO `org_fund` VALUES (1,'My First Fund',NULL,1,'2012-07-17 00:33:46',1,'2012-07-17 00:33:46'),(2,'high end fund',NULL,1,'2012-07-17 03:19:10',1,'2012-07-17 03:19:10');
/*!40000 ALTER TABLE `org_fund` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `org_office`
--

DROP TABLE IF EXISTS `org_office`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `org_office` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL,
  `hierarchy` varchar(100) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `opening_date` date NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_org` (`name`),
  UNIQUE KEY `externalid_org` (`external_id`),
  KEY `FK2291C477E2551DCC` (`parent_id`),
  CONSTRAINT `FK2291C477E2551DCC` FOREIGN KEY (`parent_id`) REFERENCES `org_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_office`
--

LOCK TABLES `org_office` WRITE;
/*!40000 ALTER TABLE `org_office` DISABLE KEYS */;
INSERT INTO `org_office` VALUES (1,NULL,'.','1','Head Office','2009-01-01',NULL,NULL,'2012-07-13 17:04:20',1),(2,1,'.2.','2','sub branch 1','2012-01-02',1,'2012-04-14 05:42:40','2012-04-16 11:47:05',1),(3,1,'.3.','3','sub branch 2','2012-03-01',1,'2012-04-16 04:12:02','2012-04-16 11:47:14',1),(4,1,'.4.',NULL,'sub branch 3','2012-04-17',1,'2012-04-17 06:01:10','2012-04-17 06:01:10',1),(5,3,'.3.5.','old 44334','Eastern Branch','2012-07-04',1,'2012-07-17 01:23:31','2012-07-17 01:23:31',1),(6,4,'.4.6.',NULL,'new branch','2012-07-01',1,'2012-07-17 03:19:27','2012-07-17 03:19:27',1);
/*!40000 ALTER TABLE `org_office` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `org_organisation_currency`
--

DROP TABLE IF EXISTS `org_organisation_currency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `org_organisation_currency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(3) NOT NULL,
  `decimal_places` smallint(5) NOT NULL,
  `name` varchar(50) NOT NULL,
  `display_symbol` varchar(10) DEFAULT NULL,
  `internationalized_name_code` varchar(50) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_organisation_currency`
--

LOCK TABLES `org_organisation_currency` WRITE;
/*!40000 ALTER TABLE `org_organisation_currency` DISABLE KEYS */;
INSERT INTO `org_organisation_currency` VALUES (17,'KES',2,'Kenyan Shilling','KSh','currency.KES',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),(18,'BND',2,'Brunei Dollar','BND','currency.BND',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),(19,'LBP',2,'Lebanese Pound','L','currency.LBP',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),(20,'GHC',2,'Ghana Cedi','GHC','currency.GHC',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),(21,'USD',2,'US Dollar','$','currency.USD',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1),(22,'XOF',0,'CFA Franc BCEAO','CFA','currency.XOF',1,'2012-05-01 22:43:02','2012-05-01 22:43:02',1);
/*!40000 ALTER TABLE `org_organisation_currency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_client`
--

DROP TABLE IF EXISTS `portfolio_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_client` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `firstname` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `joining_date` date DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FKCE00CAB3E0DD567A` (`office_id`),
  KEY `FKAUD0000000000001` (`createdby_id`),
  KEY `FKAUD0000000000002` (`lastmodifiedby_id`),
  CONSTRAINT `FKAUD0000000000001` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FKAUD0000000000002` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FKCE00CAB3E0DD567A` FOREIGN KEY (`office_id`) REFERENCES `org_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_client`
--

LOCK TABLES `portfolio_client` WRITE;
/*!40000 ALTER TABLE `portfolio_client` DISABLE KEYS */;
INSERT INTO `portfolio_client` VALUES (1,1,NULL,'Willie','O\'Meara','2009-01-04',1,'2012-04-12 22:07:44','2012-04-12 22:07:44',1),(2,1,NULL,'Declan','Browne','2009-01-04',1,'2012-04-12 22:15:39','2012-04-12 22:15:39',1),(3,1,NULL,'Ja','Fallon','2009-01-11',1,'2012-04-12 22:16:30','2012-04-12 22:16:30',1),(4,1,NULL,'Peter','Lambert','2009-01-11',1,'2012-04-12 22:17:01','2012-04-12 22:17:01',1),(5,1,NULL,NULL,'Sunnyville vegetable growers Ltd','2009-01-24',1,'2012-04-12 22:19:18','2012-04-12 22:19:18',1),(6,2,NULL,'Jacques','Lee','2012-04-03',1,'2012-04-14 06:09:22','2012-04-14 06:09:22',1),(7,1,NULL,'Kalilou','Traor','2009-02-04',1,'2012-04-14 09:38:11','2012-04-14 09:38:11',1),(8,1,NULL,'Sidi','Kon','2009-02-11',1,'2012-04-14 09:45:43','2012-04-14 09:45:43',1),(9,1,NULL,'Moustapha','Yattabar','2009-02-18',1,'2012-04-14 10:24:20','2012-04-14 10:24:20',1),(10,1,NULL,NULL,'Mali fruit sales ltd.','2009-02-18',1,'2012-04-14 10:25:37','2012-04-14 10:25:37',1),(11,1,NULL,NULL,'Djenne co-op group','2009-02-25',1,'2012-04-14 10:31:03','2012-04-14 10:31:03',1);
/*!40000 ALTER TABLE `portfolio_client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_client_extra_Additional Information`
--

DROP TABLE IF EXISTS `portfolio_client_extra_Additional Information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_client_extra_Additional Information` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Ethnic Group` varchar(50) DEFAULT NULL,
  `Ethnic Group Other` varchar(50) DEFAULT NULL,
  `Household Location` varchar(50) DEFAULT NULL,
  `Household Location Other` varchar(50) DEFAULT NULL,
  `Religion` varchar(50) DEFAULT NULL,
  `Religion Other` varchar(50) DEFAULT NULL,
  `Knowledge of Person` varchar(50) DEFAULT NULL,
  `Gender` varchar(10) DEFAULT NULL,
  `Whois` mediumtext,
  PRIMARY KEY (`id`),
  CONSTRAINT `portfolio_client_extra_Additional Information_fk1` FOREIGN KEY (`id`) REFERENCES `portfolio_client` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_client_extra_Additional Information`
--

LOCK TABLES `portfolio_client_extra_Additional Information` WRITE;
/*!40000 ALTER TABLE `portfolio_client_extra_Additional Information` DISABLE KEYS */;
INSERT INTO `portfolio_client_extra_Additional Information` VALUES (1,'Italian',NULL,'North Sikkim',NULL,NULL,NULL,'Friend of staff member','Male','More info about this and other things\r\n\r\nuntil the end');
/*!40000 ALTER TABLE `portfolio_client_extra_Additional Information` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_client_extra_Highly Improbable Info`
--

DROP TABLE IF EXISTS `portfolio_client_extra_Highly Improbable Info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_client_extra_Highly Improbable Info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Fathers Favourite Team` varchar(50) DEFAULT NULL,
  `Mothers Favourite Team` varchar(50) DEFAULT NULL,
  `Fathers DOB` date DEFAULT NULL,
  `Mothers DOB` date DEFAULT NULL,
  `Fathers Education` varchar(50) DEFAULT NULL,
  `Mothers Education` varchar(50) DEFAULT NULL,
  `Number of Children` int(11) DEFAULT NULL,
  `Favourite Town` varchar(30) DEFAULT NULL,
  `Closing Comments` mediumtext,
  `Annual Family Income` decimal(19,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `portfolio_client_extra_Highly Improbable Info_fk1` FOREIGN KEY (`id`) REFERENCES `portfolio_client` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_client_extra_Highly Improbable Info`
--

LOCK TABLES `portfolio_client_extra_Highly Improbable Info` WRITE;
/*!40000 ALTER TABLE `portfolio_client_extra_Highly Improbable Info` DISABLE KEYS */;
INSERT INTO `portfolio_client_extra_Highly Improbable Info` VALUES (1,'AC Milan','Manchester Utd',NULL,NULL,'Secondary','Secondary',NULL,NULL,NULL,NULL),(2,'Sao Paulo',NULL,'2012-07-12',NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `portfolio_client_extra_Highly Improbable Info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_loan`
--

DROP TABLE IF EXISTS `portfolio_loan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_loan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `external_id` varchar(100) DEFAULT NULL,
  `client_id` bigint(20) NOT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `fund_id` bigint(20) DEFAULT NULL,
  `loan_status_id` smallint(5) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `principal_amount` decimal(19,6) NOT NULL,
  `arrearstolerance_amount` decimal(19,6) NOT NULL,
  `nominal_interest_rate_per_period` decimal(19,6) NOT NULL,
  `interest_period_frequency_enum` smallint(5) NOT NULL,
  `annual_nominal_interest_rate` decimal(19,6) NOT NULL,
  `interest_method_enum` smallint(5) NOT NULL,
  `interest_calculated_in_period_enum` smallint(5) NOT NULL DEFAULT '1',
  `repay_every` smallint(5) NOT NULL,
  `repayment_period_frequency_enum` smallint(5) NOT NULL,
  `number_of_repayments` smallint(5) NOT NULL,
  `amortization_method_enum` smallint(5) NOT NULL,
  `flexible_repayment_schedule` bit(1) NOT NULL,
  `interest_rebate` bit(1) NOT NULL,
  `interest_rebate_amount` decimal(19,6) DEFAULT NULL,
  `submittedon_date` datetime DEFAULT NULL,
  `approvedon_date` datetime DEFAULT NULL,
  `expected_disbursedon_date` date DEFAULT NULL,
  `expected_firstrepaymenton_date` date DEFAULT NULL,
  `interest_calculated_from_date` date DEFAULT NULL,
  `disbursedon_date` date DEFAULT NULL,
  `expected_maturedon_date` date DEFAULT NULL,
  `maturedon_date` date DEFAULT NULL,
  `closedon_date` datetime DEFAULT NULL,
  `rejectedon_date` datetime DEFAULT NULL,
  `rescheduledon_date` datetime DEFAULT NULL,
  `withdrawnon_date` datetime DEFAULT NULL,
  `writtenoffon_date` datetime DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `org_id` (`external_id`),
  KEY `FKB6F935D87179A0CB` (`client_id`),
  KEY `FKB6F935D8C8D4B434` (`product_id`),
  KEY `FK7C885878B1147D1` (`loan_status_id`),
  KEY `FK7C885877240145` (`fund_id`),
  CONSTRAINT `FK7C885877240145` FOREIGN KEY (`fund_id`) REFERENCES `org_fund` (`id`),
  CONSTRAINT `FK7C885878B1147D1` FOREIGN KEY (`loan_status_id`) REFERENCES `ref_loan_status` (`id`),
  CONSTRAINT `FKB6F935D87179A0CB` FOREIGN KEY (`client_id`) REFERENCES `portfolio_client` (`id`),
  CONSTRAINT `FKB6F935D8C8D4B434` FOREIGN KEY (`product_id`) REFERENCES `portfolio_product_loan` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_loan`
--

LOCK TABLES `portfolio_loan` WRITE;
/*!40000 ALTER TABLE `portfolio_loan` DISABLE KEYS */;
INSERT INTO `portfolio_loan` VALUES (1,NULL,6,1,1,100,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,1,1,12,1,'\0','\0','0.000000','2012-07-04 01:09:52',NULL,'2012-07-17',NULL,NULL,NULL,'2012-10-09',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(2,NULL,2,2,2,100,'LBP',2,'35000.000000','10.000000','1.000000',2,'12.000000',0,1,2,1,10,1,'\0','\0','0.000000','2012-07-01 03:39:58',NULL,'2012-07-17',NULL,NULL,NULL,'2012-12-04',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1);
/*!40000 ALTER TABLE `portfolio_loan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_loan_extra_Additional Information`
--

DROP TABLE IF EXISTS `portfolio_loan_extra_Additional Information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_loan_extra_Additional Information` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Business Location` varchar(50) DEFAULT NULL,
  `Business Location Other` varchar(50) DEFAULT NULL,
  `Business` varchar(10) DEFAULT NULL,
  `Business Description` mediumtext,
  `Business Title` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `portfolio_loan_extra_Additional Information_fk1` FOREIGN KEY (`id`) REFERENCES `portfolio_loan` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_loan_extra_Additional Information`
--

LOCK TABLES `portfolio_loan_extra_Additional Information` WRITE;
/*!40000 ALTER TABLE `portfolio_loan_extra_Additional Information` DISABLE KEYS */;
INSERT INTO `portfolio_loan_extra_Additional Information` VALUES (1,'East Sikkim',NULL,'New',NULL,NULL);
/*!40000 ALTER TABLE `portfolio_loan_extra_Additional Information` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_loan_repayment_schedule`
--

DROP TABLE IF EXISTS `portfolio_loan_repayment_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_loan_repayment_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `duedate` date DEFAULT NULL,
  `installment` smallint(5) DEFAULT NULL,
  `principal_amount` decimal(19,6) DEFAULT NULL,
  `principal_completed_derived` decimal(19,6) DEFAULT NULL,
  `interest_amount` decimal(19,6) DEFAULT NULL,
  `interest_completed_derived` decimal(19,6) DEFAULT NULL,
  `completed_derived` bit(1) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK488B92AA40BE0710` (`loan_id`),
  CONSTRAINT `FK488B92AA40BE0710` FOREIGN KEY (`loan_id`) REFERENCES `portfolio_loan` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_loan_repayment_schedule`
--

LOCK TABLES `portfolio_loan_repayment_schedule` WRITE;
/*!40000 ALTER TABLE `portfolio_loan_repayment_schedule` DISABLE KEYS */;
INSERT INTO `portfolio_loan_repayment_schedule` VALUES (1,1,'2012-07-24',1,'8150.000000','0.000000','404.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(2,1,'2012-07-31',2,'8183.000000','0.000000','371.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(3,1,'2012-08-07',3,'8216.000000','0.000000','338.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(4,1,'2012-08-14',4,'8249.000000','0.000000','305.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(5,1,'2012-08-21',5,'8283.000000','0.000000','271.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(6,1,'2012-08-28',6,'8316.000000','0.000000','238.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(7,1,'2012-09-04',7,'8350.000000','0.000000','204.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(8,1,'2012-09-11',8,'8383.000000','0.000000','171.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(9,1,'2012-09-18',9,'8417.000000','0.000000','137.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(10,1,'2012-09-25',10,'8451.000000','0.000000','103.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(11,1,'2012-10-02',11,'8485.000000','0.000000','69.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(12,1,'2012-10-09',12,'8517.000000','0.000000','33.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1),(13,2,'2012-07-31',1,'3427.920000','0.000000','161.540000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(14,2,'2012-08-14',2,'3443.740000','0.000000','145.720000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(15,2,'2012-08-28',3,'3459.640000','0.000000','129.820000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(16,2,'2012-09-11',4,'3475.600000','0.000000','113.860000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(17,2,'2012-09-25',5,'3491.650000','0.000000','97.810000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(18,2,'2012-10-09',6,'3507.760000','0.000000','81.700000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(19,2,'2012-10-23',7,'3523.950000','0.000000','65.510000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(20,2,'2012-11-06',8,'3540.220000','0.000000','49.240000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(21,2,'2012-11-20',9,'3556.550000','0.000000','32.910000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1),(22,2,'2012-12-04',10,'3572.970000','0.000000','16.490000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1);
/*!40000 ALTER TABLE `portfolio_loan_repayment_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_loan_transaction`
--

DROP TABLE IF EXISTS `portfolio_loan_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_loan_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `transaction_type_enum` smallint(5) NOT NULL,
  `contra_id` bigint(20) DEFAULT NULL,
  `transaction_date` date NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKCFCEA42640BE0710` (`loan_id`),
  KEY `FKCFCEA426FC69F3F1` (`contra_id`),
  CONSTRAINT `FKCFCEA42640BE0710` FOREIGN KEY (`loan_id`) REFERENCES `portfolio_loan` (`id`),
  CONSTRAINT `FKCFCEA426FC69F3F1` FOREIGN KEY (`contra_id`) REFERENCES `portfolio_loan_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_loan_transaction`
--

LOCK TABLES `portfolio_loan_transaction` WRITE;
/*!40000 ALTER TABLE `portfolio_loan_transaction` DISABLE KEYS */;
/*!40000 ALTER TABLE `portfolio_loan_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_note`
--

DROP TABLE IF EXISTS `portfolio_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_note` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `loan_transaction_id` bigint(20) DEFAULT NULL,
  `note_type_enum` smallint(5) NOT NULL,
  `note` varchar(1000) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7C9708924D26803` (`loan_transaction_id`),
  KEY `FK7C97089541F0A56` (`createdby_id`),
  KEY `FK7C970897179A0CB` (`client_id`),
  KEY `FK7C970898F889C3F` (`lastmodifiedby_id`),
  KEY `FK7C9708940BE0710` (`loan_id`),
  CONSTRAINT `FK7C9708924D26803` FOREIGN KEY (`loan_transaction_id`) REFERENCES `portfolio_loan_transaction` (`id`),
  CONSTRAINT `FK7C9708940BE0710` FOREIGN KEY (`loan_id`) REFERENCES `portfolio_loan` (`id`),
  CONSTRAINT `FK7C97089541F0A56` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FK7C970897179A0CB` FOREIGN KEY (`client_id`) REFERENCES `portfolio_client` (`id`),
  CONSTRAINT `FK7C970898F889C3F` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_note`
--

LOCK TABLES `portfolio_note` WRITE;
/*!40000 ALTER TABLE `portfolio_note` DISABLE KEYS */;
INSERT INTO `portfolio_note` VALUES (1,8,NULL,NULL,100,'Just a quick\r\nnote','2012-07-17 01:22:13',1,'2012-07-17 01:22:13',1),(2,1,NULL,NULL,100,'First note\r\n\r\nedited','2012-07-17 05:15:05',1,'2012-07-17 05:15:17',1),(3,11,NULL,NULL,100,'one','2012-07-17 05:19:42',1,'2012-07-17 05:19:42',1),(4,2,NULL,NULL,100,'Declan has just started','2012-07-17 05:20:22',1,'2012-07-17 05:20:22',1);
/*!40000 ALTER TABLE `portfolio_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_office_transaction`
--

DROP TABLE IF EXISTS `portfolio_office_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_office_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_office_id` bigint(20) DEFAULT NULL,
  `to_office_id` bigint(20) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` int(11) NOT NULL,
  `transaction_amount` decimal(19,6) NOT NULL,
  `transaction_date` date NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1E37728B93C6C1B6` (`to_office_id`),
  KEY `FK1E37728B783C5C25` (`from_office_id`),
  CONSTRAINT `FK1E37728B783C5C25` FOREIGN KEY (`from_office_id`) REFERENCES `org_office` (`id`),
  CONSTRAINT `FK1E37728B93C6C1B6` FOREIGN KEY (`to_office_id`) REFERENCES `org_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_office_transaction`
--

LOCK TABLES `portfolio_office_transaction` WRITE;
/*!40000 ALTER TABLE `portfolio_office_transaction` DISABLE KEYS */;
INSERT INTO `portfolio_office_transaction` VALUES (1,NULL,1,'USD',2,'55555.000000','2012-07-17',NULL,'2012-07-17 03:07:09',1,'2012-07-17 03:07:09',1),(2,1,3,'LBP',2,'34000.000000','2012-06-06',NULL,'2012-07-17 03:38:09',1,'2012-07-17 03:38:09',1);
/*!40000 ALTER TABLE `portfolio_office_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_product_loan`
--

DROP TABLE IF EXISTS `portfolio_product_loan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_product_loan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `principal_amount` decimal(19,6) NOT NULL,
  `arrearstolerance_amount` decimal(19,6) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `fund_id` bigint(20) DEFAULT NULL,
  `nominal_interest_rate_per_period` decimal(19,6) NOT NULL,
  `interest_period_frequency_enum` smallint(5) NOT NULL,
  `annual_nominal_interest_rate` decimal(19,6) NOT NULL,
  `interest_method_enum` smallint(5) NOT NULL,
  `interest_calculated_in_period_enum` smallint(5) NOT NULL DEFAULT '1',
  `repay_every` smallint(5) NOT NULL,
  `repayment_period_frequency_enum` smallint(5) NOT NULL,
  `number_of_repayments` smallint(5) NOT NULL,
  `amortization_method_enum` smallint(5) NOT NULL,
  `flexible_repayment_schedule` bit(1) NOT NULL,
  `interest_rebate` bit(1) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKAUD0000000000003` (`createdby_id`),
  KEY `FKAUD0000000000004` (`lastmodifiedby_id`),
  KEY `FKA6A8A7D77240145` (`fund_id`),
  CONSTRAINT `FKA6A8A7D77240145` FOREIGN KEY (`fund_id`) REFERENCES `org_fund` (`id`),
  CONSTRAINT `FKAUD0000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  CONSTRAINT `FKAUD0000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_product_loan`
--

LOCK TABLES `portfolio_product_loan` WRITE;
/*!40000 ALTER TABLE `portfolio_product_loan` DISABLE KEYS */;
INSERT INTO `portfolio_product_loan` VALUES (1,'XOF',0,'100000.000000','1000.000000','Agricultural Loan','An agricultural loan given to farmers to help buy crop, stock and machinery. With an arrears tolerance setting of 1,000 CFA, loans are not marked as \'in arrears\' or \'in bad standing\' if the amount outstanding is less than this. Interest rate is described using monthly percentage rate (MPR) even though the loan typically lasts a year and requires one repayment (typically at time when farmer sells crop)',1,'1.750000',2,'21.000000',0,1,12,2,1,1,'\0','\0',1,'2012-04-12 22:14:34','2012-07-17 00:34:01',1),(2,'LBP',2,'35000.000000','10.000000','Different Loan Type',NULL,NULL,'1.000000',2,'12.000000',0,1,2,1,10,1,'\0','\0',1,'2012-07-17 03:20:34','2012-07-17 03:20:34',1),(3,'BND',2,'5000.000000','10.000000','Further Loan Product',NULL,2,'1.500000',2,'18.000000',0,1,1,2,6,0,'\0','\0',1,'2012-07-17 03:21:38','2012-07-17 03:21:38',1);
/*!40000 ALTER TABLE `portfolio_product_loan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ref_currency`
--

DROP TABLE IF EXISTS `ref_currency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ref_currency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(3) NOT NULL,
  `decimal_places` smallint(5) NOT NULL,
  `display_symbol` varchar(10) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `internationalized_name_code` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=164 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ref_currency`
--

LOCK TABLES `ref_currency` WRITE;
/*!40000 ALTER TABLE `ref_currency` DISABLE KEYS */;
INSERT INTO `ref_currency` VALUES (1,'AED',2,NULL,'UAE Dirham','currency.AED'),(2,'AFN',2,NULL,'Afghanistan Afghani','currency.AFN'),(3,'ALL',2,NULL,'Albanian Lek','currency.ALL'),(4,'AMD',2,NULL,'Armenian Dram','currency.AMD'),(5,'ANG',2,NULL,'Netherlands Antillian Guilder','currency.ANG'),(6,'AOA',2,NULL,'Angolan Kwanza','currency.AOA'),(7,'ARS',2,NULL,'Argentine Peso','currency.ARS'),(8,'AUD',2,NULL,'Australian Dollar','currency.AUD'),(9,'AWG',2,NULL,'Aruban Guilder','currency.AWG'),(10,'AZM',2,NULL,'Azerbaijanian Manat','currency.AZM'),(11,'BAM',2,NULL,'Bosnia and Herzegovina Convertible Marks','currency.BAM'),(12,'BBD',2,NULL,'Barbados Dollar','currency.BBD'),(13,'BDT',2,NULL,'Bangladesh Taka','currency.BDT'),(14,'BGN',2,NULL,'Bulgarian Lev','currency.BGN'),(15,'BHD',3,NULL,'Bahraini Dinar','currency.BHD'),(16,'BIF',0,NULL,'Burundi Franc','currency.BIF'),(17,'BMD',2,NULL,'Bermudian Dollar','currency.BMD'),(18,'BND',2,'B$','Brunei Dollar','currency.BND'),(19,'BOB',2,NULL,'Bolivian Boliviano','currency.BOB'),(20,'BRL',2,NULL,'Brazilian Real','currency.BRL'),(21,'BSD',2,NULL,'Bahamian Dollar','currency.BSD'),(22,'BTN',2,NULL,'Bhutan Ngultrum','currency.BTN'),(23,'BWP',2,NULL,'Botswana Pula','currency.BWP'),(24,'BYR',0,NULL,'Belarussian Ruble','currency.BYR'),(25,'BZD',2,NULL,'Belize Dollar','currency.BZD'),(26,'CAD',2,NULL,'Canadian Dollar','currency.CAD'),(27,'CDF',2,NULL,'Franc Congolais','currency.CDF'),(28,'CHF',2,NULL,'Swiss Franc','currency.CHF'),(29,'CLP',0,NULL,'Chilean Peso','currency.CLP'),(30,'CNY',2,NULL,'Chinese Yuan Renminbi','currency.CNY'),(31,'COP',2,NULL,'Colombian Peso','currency.COP'),(32,'CRC',2,NULL,'Costa Rican Colon','currency.CRC'),(33,'CSD',2,NULL,'Serbian Dinar','currency.CSD'),(34,'CUP',2,NULL,'Cuban Peso','currency.CUP'),(35,'CVE',2,NULL,'Cape Verde Escudo','currency.CVE'),(36,'CYP',2,NULL,'Cyprus Pound','currency.CYP'),(37,'CZK',2,NULL,'Czech Koruna','currency.CZK'),(38,'DJF',0,NULL,'Djibouti Franc','currency.DJF'),(39,'DKK',2,NULL,'Danish Krone','currency.DKK'),(40,'DOP',2,NULL,'Dominican Peso','currency.DOP'),(41,'DZD',2,NULL,'Algerian Dinar','currency.DZD'),(42,'EEK',2,NULL,'Estonian Kroon','currency.EEK'),(43,'EGP',2,NULL,'Egyptian Pound','currency.EGP'),(44,'ERN',2,NULL,'Eritrea Nafka','currency.ERN'),(45,'ETB',2,NULL,'Ethiopian Birr','currency.ETB'),(46,'EUR',2,NULL,'euro','currency.EUR'),(47,'FJD',2,NULL,'Fiji Dollar','currency.FJD'),(48,'FKP',2,NULL,'Falkland Islands Pound','currency.FKP'),(49,'GBP',2,NULL,'Pound Sterling','currency.GBP'),(50,'GEL',2,NULL,'Georgian Lari','currency.GEL'),(51,'GHC',2,'GHc','Ghana Cedi','currency.GHC'),(52,'GIP',2,NULL,'Gibraltar Pound','currency.GIP'),(53,'GMD',2,NULL,'Gambian Dalasi','currency.GMD'),(54,'GNF',0,NULL,'Guinea Franc','currency.GNF'),(55,'GTQ',2,NULL,'Guatemala Quetzal','currency.GTQ'),(56,'GYD',2,NULL,'Guyana Dollar','currency.GYD'),(57,'HKD',2,NULL,'Hong Kong Dollar','currency.HKD'),(58,'HNL',2,NULL,'Honduras Lempira','currency.HNL'),(59,'HRK',2,NULL,'Croatian Kuna','currency.HRK'),(60,'HTG',2,NULL,'Haiti Gourde','currency.HTG'),(61,'HUF',2,NULL,'Hungarian Forint','currency.HUF'),(62,'IDR',2,NULL,'Indonesian Rupiah','currency.IDR'),(63,'ILS',2,NULL,'New Israeli Shekel','currency.ILS'),(64,'INR',2,NULL,'Indian Rupee','currency.INR'),(65,'IQD',3,NULL,'Iraqi Dinar','currency.IQD'),(66,'IRR',2,NULL,'Iranian Rial','currency.IRR'),(67,'ISK',0,NULL,'Iceland Krona','currency.ISK'),(68,'JMD',2,NULL,'Jamaican Dollar','currency.JMD'),(69,'JOD',3,NULL,'Jordanian Dinar','currency.JOD'),(70,'JPY',0,NULL,'Japanese Yen','currency.JPY'),(71,'KES',2,'KSh','Kenyan Shilling','currency.KES'),(72,'KGS',2,NULL,'Kyrgyzstan Som','currency.KGS'),(73,'KHR',2,NULL,'Cambodia Riel','currency.KHR'),(74,'KMF',0,NULL,'Comoro Franc','currency.KMF'),(75,'KPW',2,NULL,'North Korean Won','currency.KPW'),(76,'KRW',0,NULL,'Korean Won','currency.KRW'),(77,'KWD',3,NULL,'Kuwaiti Dinar','currency.KWD'),(78,'KYD',2,NULL,'Cayman Islands Dollar','currency.KYD'),(79,'KZT',2,NULL,'Kazakhstan Tenge','currency.KZT'),(80,'LAK',2,NULL,'Lao Kip','currency.LAK'),(81,'LBP',2,'L£','Lebanese Pound','currency.LBP'),(82,'LKR',2,NULL,'Sri Lanka Rupee','currency.LKR'),(83,'LRD',2,NULL,'Liberian Dollar','currency.LRD'),(84,'LSL',2,NULL,'Lesotho Loti','currency.LSL'),(85,'LTL',2,NULL,'Lithuanian Litas','currency.LTL'),(86,'LVL',2,NULL,'Latvian Lats','currency.LVL'),(87,'LYD',3,NULL,'Libyan Dinar','currency.LYD'),(88,'MAD',2,NULL,'Moroccan Dirham','currency.MAD'),(89,'MDL',2,NULL,'Moldovan Leu','currency.MDL'),(90,'MGA',2,NULL,'Malagasy Ariary','currency.MGA'),(91,'MKD',2,NULL,'Macedonian Denar','currency.MKD'),(92,'MMK',2,NULL,'Myanmar Kyat','currency.MMK'),(93,'MNT',2,NULL,'Mongolian Tugrik','currency.MNT'),(94,'MOP',2,NULL,'Macau Pataca','currency.MOP'),(95,'MRO',2,NULL,'Mauritania Ouguiya','currency.MRO'),(96,'MTL',2,NULL,'Maltese Lira','currency.MTL'),(97,'MUR',2,NULL,'Mauritius Rupee','currency.MUR'),(98,'MVR',2,NULL,'Maldives Rufiyaa','currency.MVR'),(99,'MWK',2,NULL,'Malawi Kwacha','currency.MWK'),(100,'MXN',2,NULL,'Mexican Peso','currency.MXN'),(101,'MYR',2,NULL,'Malaysian Ringgit','currency.MYR'),(102,'MZM',2,NULL,'Mozambique Metical','currency.MZM'),(103,'NAD',2,NULL,'Namibia Dollar','currency.NAD'),(104,'NGN',2,NULL,'Nigerian Naira','currency.NGN'),(105,'NIO',2,NULL,'Nicaragua Cordoba Oro','currency.NIO'),(106,'NOK',2,NULL,'Norwegian Krone','currency.NOK'),(107,'NPR',2,NULL,'Nepalese Rupee','currency.NPR'),(108,'NZD',2,NULL,'New Zealand Dollar','currency.NZD'),(109,'OMR',3,NULL,'Rial Omani','currency.OMR'),(110,'PAB',2,NULL,'Panama Balboa','currency.PAB'),(111,'PEN',2,NULL,'Peruvian Nuevo Sol','currency.PEN'),(112,'PGK',2,NULL,'Papua New Guinea Kina','currency.PGK'),(113,'PHP',2,NULL,'Philippine Peso','currency.PHP'),(114,'PKR',2,NULL,'Pakistan Rupee','currency.PKR'),(115,'PLN',2,NULL,'Polish Zloty','currency.PLN'),(116,'PYG',0,NULL,'Paraguayan Guarani','currency.PYG'),(117,'QAR',2,NULL,'Qatari Rial','currency.QAR'),(118,'RON',2,NULL,'Romanian Leu','currency.RON'),(119,'RUB',2,NULL,'Russian Ruble','currency.RUB'),(120,'RWF',0,NULL,'Rwanda Franc','currency.RWF'),(121,'SAR',2,NULL,'Saudi Riyal','currency.SAR'),(122,'SBD',2,NULL,'Solomon Islands Dollar','currency.SBD'),(123,'SCR',2,NULL,'Seychelles Rupee','currency.SCR'),(124,'SDD',2,NULL,'Sudanese Dinar','currency.SDD'),(125,'SEK',2,NULL,'Swedish Krona','currency.SEK'),(126,'SGD',2,NULL,'Singapore Dollar','currency.SGD'),(127,'SHP',2,NULL,'St Helena Pound','currency.SHP'),(128,'SIT',2,NULL,'Slovenian Tolar','currency.SIT'),(129,'SKK',2,NULL,'Slovak Koruna','currency.SKK'),(130,'SLL',2,NULL,'Sierra Leone Leone','currency.SLL'),(131,'SOS',2,NULL,'Somali Shilling','currency.SOS'),(132,'SRD',2,NULL,'Surinam Dollar','currency.SRD'),(133,'STD',2,NULL,'Sao Tome and Principe Dobra','currency.STD'),(134,'SVC',2,NULL,'El Salvador Colon','currency.SVC'),(135,'SYP',2,NULL,'Syrian Pound','currency.SYP'),(136,'SZL',2,NULL,'Swaziland Lilangeni','currency.SZL'),(137,'THB',2,NULL,'Thai Baht','currency.THB'),(138,'TJS',2,NULL,'Tajik Somoni','currency.TJS'),(139,'TMM',2,NULL,'Turkmenistan Manat','currency.TMM'),(140,'TND',3,'DT','Tunisian Dinar','currency.TND'),(141,'TOP',2,NULL,'Tonga Pa\'anga','currency.TOP'),(142,'TRY',2,NULL,'Turkish Lira','currency.TRY'),(143,'TTD',2,NULL,'Trinidad and Tobago Dollar','currency.TTD'),(144,'TWD',2,NULL,'New Taiwan Dollar','currency.TWD'),(145,'TZS',2,NULL,'Tanzanian Shilling','currency.TZS'),(146,'UAH',2,NULL,'Ukraine Hryvnia','currency.UAH'),(147,'UGX',2,NULL,'Uganda Shilling','currency.UGX'),(148,'USD',2,'$','US Dollar','currency.USD'),(149,'UYU',2,NULL,'Peso Uruguayo','currency.UYU'),(150,'UZS',2,NULL,'Uzbekistan Sum','currency.UZS'),(151,'VEB',2,NULL,'Venezuelan Bolivar','currency.VEB'),(152,'VND',2,NULL,'Vietnamese Dong','currency.VND'),(153,'VUV',0,NULL,'Vanuatu Vatu','currency.VUV'),(154,'WST',2,NULL,'Samoa Tala','currency.WST'),(155,'XAF',0,NULL,'CFA Franc BEAC','currency.XAF'),(156,'XCD',2,NULL,'East Caribbean Dollar','currency.XCD'),(157,'XDR',5,NULL,'SDR (Special Drawing Rights)','currency.XDR'),(158,'XOF',0,'CFA','CFA Franc BCEAO','currency.XOF'),(159,'XPF',0,NULL,'CFP Franc','currency.XPF'),(160,'YER',2,NULL,'Yemeni Rial','currency.YER'),(161,'ZAR',2,NULL,'South African Rand','currency.ZAR'),(162,'ZMK',2,NULL,'Zambian Kwacha','currency.ZMK'),(163,'ZWD',2,NULL,'Zimbabwe Dollar','currency.ZWD');
/*!40000 ALTER TABLE `ref_currency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ref_loan_status`
--

DROP TABLE IF EXISTS `ref_loan_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ref_loan_status` (
  `id` smallint(5) NOT NULL,
  `display_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ref_loan_status`
--

LOCK TABLES `ref_loan_status` WRITE;
/*!40000 ALTER TABLE `ref_loan_status` DISABLE KEYS */;
INSERT INTO `ref_loan_status` VALUES (100,'Submitted and awaiting approval'),(200,'Approved'),(300,'Active'),(400,'Withdrawn by client'),(500,'Rejected'),(600,'Closed');
/*!40000 ALTER TABLE `ref_loan_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rpt_sequence`
--

DROP TABLE IF EXISTS `rpt_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rpt_sequence` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=483 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rpt_sequence`
--

LOCK TABLES `rpt_sequence` WRITE;
/*!40000 ALTER TABLE `rpt_sequence` DISABLE KEYS */;
INSERT INTO `rpt_sequence` VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),(51),(52),(53),(54),(55),(56),(57),(58),(59),(60),(61),(62),(63),(64),(65),(66),(67),(68),(69),(70),(71),(72),(73),(74),(75),(76),(77),(78),(79),(80),(81),(82),(83),(84),(85),(86),(87),(88),(89),(90),(91),(92),(93),(94),(95),(96),(97),(98),(99),(100),(101),(102),(103),(104),(105),(106),(107),(108),(109),(110),(111),(112),(113),(114),(115),(116),(117),(118),(119),(120),(121),(122),(123),(124),(125),(126),(127),(128),(129),(130),(131),(132),(133),(134),(135),(136),(137),(138),(139),(140),(141),(142),(143),(144),(145),(146),(147),(148),(149),(150),(151),(152),(153),(154),(155),(156),(157),(158),(159),(160),(161),(162),(163),(164),(165),(166),(167),(168),(169),(170),(171),(172),(173),(174),(175),(176),(177),(178),(179),(180),(181),(182),(183),(184),(185),(186),(187),(188),(189),(190),(191),(192),(193),(194),(195),(196),(197),(198),(199),(200),(201),(202),(203),(204),(205),(206),(207),(208),(209),(210),(211),(212),(213),(214),(215),(216),(217),(218),(219),(220),(221),(222),(223),(224),(225),(226),(227),(228),(229),(230),(231),(232),(233),(234),(235),(236),(237),(238),(239),(240),(241),(242),(243),(244),(245),(246),(247),(248),(249),(250),(251),(252),(253),(254),(255),(256),(257),(258),(259),(260),(261),(262),(263),(264),(265),(266),(267),(268),(269),(270),(271),(272),(273),(274),(275),(276),(277),(278),(279),(280),(281),(282),(283),(284),(285),(286),(287),(288),(289),(290),(291),(292),(293),(294),(295),(296),(297),(298),(299),(300),(301),(302),(303),(304),(305),(306),(307),(308),(309),(310),(311),(312),(313),(314),(315),(316),(317),(318),(319),(320),(321),(322),(323),(324),(325),(326),(327),(328),(329),(330),(331),(332),(333),(334),(335),(336),(337),(338),(339),(340),(341),(342),(343),(344),(345),(346),(347),(348),(349),(350),(351),(352),(353),(354),(355),(356),(357),(358),(359),(360),(361),(362),(363),(364),(365),(366),(367),(368),(369),(370),(371),(372),(373),(374),(375),(376),(377),(378),(379),(380),(381),(382),(383),(384),(385),(386),(387),(388),(389),(390),(391),(392),(393),(394),(395),(396),(397),(398),(399),(400),(401),(402),(403),(404),(405),(406),(407),(408),(409),(410),(411),(412),(413),(414),(415),(416),(417),(418),(419),(420),(421),(422),(423),(424),(425),(426),(427),(428),(429),(430),(431),(432),(433),(434),(435),(436),(437),(438),(439),(440),(441),(442),(443),(444),(445),(446),(447),(448),(449),(450),(451),(452),(453),(454),(455),(456),(457),(458),(459),(460),(461),(462),(463),(464),(465),(466),(467),(468),(469),(470),(471),(472),(473),(474),(475),(476),(477),(478),(479),(480),(481),(482);
/*!40000 ALTER TABLE `rpt_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stretchy_parameter`
--

DROP TABLE IF EXISTS `stretchy_parameter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stretchy_parameter` (
  `parameter_id` int(11) NOT NULL AUTO_INCREMENT,
  `parameter_name` varchar(45) NOT NULL,
  `parameter_variable` varchar(45) DEFAULT NULL,
  `parameter_label` varchar(45) NOT NULL,
  `parameter_displayType` varchar(45) NOT NULL,
  `parameter_FormatType` varchar(10) NOT NULL,
  `parameter_default` varchar(45) NOT NULL,
  `special` varchar(1) DEFAULT NULL,
  `selectOne` varchar(1) DEFAULT NULL,
  `selectAll` varchar(1) DEFAULT NULL,
  `parameter_sql` text,
  PRIMARY KEY (`parameter_id`),
  UNIQUE KEY `name_UNIQUE` (`parameter_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchy_parameter`
--

LOCK TABLES `stretchy_parameter` WRITE;
/*!40000 ALTER TABLE `stretchy_parameter` DISABLE KEYS */;
INSERT INTO `stretchy_parameter` VALUES (3,'FullReportList',NULL,'n/a','n/a','n/a','n/a','Y',NULL,NULL,'select r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\r\nrp.parameter_id, rp.report_parameter_name, p.parameter_name\r\nfrom stretchy_report r\r\nleft join stretchy_report_parameter rp on rp.report_id = r.report_id\r\nleft join stretchy_parameter p on p.parameter_id = rp.parameter_id\r\norder by r.report_name, rp.parameter_id'),(4,'FullParameterList',NULL,'n/a','n/a','n/a','n/a','Y',NULL,NULL,'select parameter_name, parameter_variable, parameter_label, parameter_displayType, parameter_FormatType, parameter_default, selectOne,  selectAll\r\nfrom stretchy_parameter p\r\nwhere special is null\r\norder by parameter_id'),(5,'selectOfficeId','officeId','Office','select','number','0',NULL,'Y',NULL,'select id, \r\nconcat(substring(\"........................................\", 1, \r\n   ((LENGTH(`hierarchy`) - LENGTH(REPLACE(`hierarchy`, \'.\', \'\')) - 1) * 4)), \r\n   `name`) as tc\r\nfrom org_office\r\norder by hierarchy'),(6,'currencyIdSelectAll','currencyId','Currency','select','number','0',NULL,'Y','Y','select `code`, `name`\r\nfrom org_organisation_currency\r\norder by `code`'),(7,'currencyIdSelectOne','currencyId','Currency','select','number','0',NULL,'Y',NULL,'select `code`, `name`\r\nfrom org_organisation_currency\r\norder by `code`');
/*!40000 ALTER TABLE `stretchy_parameter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stretchy_report`
--

DROP TABLE IF EXISTS `stretchy_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stretchy_report` (
  `report_id` int(11) NOT NULL AUTO_INCREMENT,
  `report_name` varchar(100) NOT NULL,
  `report_type` varchar(20) NOT NULL,
  `report_subtype` varchar(20) DEFAULT NULL,
  `report_category` varchar(45) DEFAULT NULL,
  `report_sql` text,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `report_name_UNIQUE` (`report_name`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchy_report`
--

LOCK TABLES `stretchy_report` WRITE;
/*!40000 ALTER TABLE `stretchy_report` DISABLE KEYS */;
INSERT INTO `stretchy_report` VALUES (29,'Client Listing','Table',NULL,NULL,'select ounder.`name` as \"Office/Branch\",  concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\", \r\nc.joining_date as \"Joining Date\"\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\nwhere o.id = ${officeId}\r\norder by ounder.hierarchy'),(30,'Client Loans Listing','Table',NULL,NULL,'select ounder.`name` as \"Office/Branch\", concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\",c.joining_date as \"Joining Date\",\r\np.name as Loan, st.display_name as \"Status\",  l.number_of_repayments as \"Expected No. Repayments\",\r\nl.annual_nominal_interest_rate as \" Annual Nominal Interest Rate\", \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  l.principal_amount,date( l.submittedon_date) as Submitted,\r\ndate(l.approvedon_date) Approved, l.expected_disbursedon_date As \"Expected Disbursal\",\r\ndate(l.expected_firstrepaymenton_date) as \"Expected First Repayment\",\r\ndate(l.expected_maturedon_date) \"Expected Maturity\",\r\ndate(l.disbursedon_date) as Disbursed, date(l.closedon_date) as Closed,\r\ndate(l.withdrawnon_date) as Withdrawn, date(l.rejectedon_date) as Rejected,\r\ndate(l.rescheduledon_date) Rescheduled, date(l.writtenoffon_date) \"Written Off\"\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\nleft join portfolio_loan l on l.client_id = c.id\r\nleft join portfolio_product_loan p on p.id = l.product_id\r\nleft join ref_loan_status st on st.id = l.loan_status_id\r\nleft join ref_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\norder by ounder.hierarchy, \"Name\" , l.id'),(31,'Loans Awaiting Disbursal','Table',NULL,NULL,'SELECT ounder.`name` as \"Office/Branch\", concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\", \r\nl.id as \"Loan ID\", pl.`name` as \"Product\", \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nl.principal_amount as Principal,  \r\ndate(l.approvedon_date) \"Approved\", l.expected_disbursedon_date \"Expected Disbursal\"\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\njoin portfolio_loan l on l.client_id = c.id\r\njoin portfolio_product_loan pl on pl.id = l.product_id\r\nleft join ref_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\nand l.loan_status_id = 200\r\norder by ounder.hierarchy, l.expected_disbursedon_date, \"Name\"'),(32,'Upcoming Repayments','Table',NULL,NULL,'SELECT r.duedate \"Due Date\", ounder.`name` as \"Office/Branch\",   ifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nc.id as \"Client ID\",\r\nconcat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\", \r\nl.id as \"Loan ID\", pl.`name` as \"Product\", \r\n\r\n(select\r\n    if(if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) > 90, \"High\", \r\n        if (if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) > 30, \"Medium\", \r\n            if (if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) = 0, \r\n                if (min(r.installment) = 1, \"Low\", \"Very Low\"),\r\n                if (min(r.installment), \"Medium\", \"Low\")))) \r\nfrom portfolio_loan lr\r\nleft join portfolio_loan_repayment_schedule r on r.loan_id = lr.id\r\n                                        and r.completed_derived is false\r\nwhere lr.id = l.id) as \"Loan Risk\",\r\n\r\n r.installment as Installment, \r\n(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Due\",\r\n(r.interest_amount - ifnull(r.interest_completed_derived, 0)) as \"Interest Due\" ,\r\n((r.principal_amount - ifnull(r.principal_completed_derived, 0)) +\r\n(r.interest_amount - ifnull(r.interest_completed_derived, 0))) as \"Total Due\"\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\njoin portfolio_loan l on l.client_id = c.id\r\njoin portfolio_product_loan pl on pl.id = l.product_id\r\njoin portfolio_loan_repayment_schedule r on r.loan_id = l.id\r\nleft join ref_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\" )\r\nand l.loan_status_id = 300\r\nand r.duedate >= curdate()\r\norder by r.duedate, \"Name\"'),(34,'Loans - Active Loans Portfolio Status','Table',NULL,NULL,'select  x.`Office/Branch`, x.Currency, x.`Name`, x.Loan, x.`Loan ID`, x.Disbursed, \r\nx.`Principal Outstanding`, x.`Interest Outstanding`, x.`Days Overdue`,\r\nx.`Principal Overdue`, x.`Interest Overdue`, \r\n\r\nif(x.`Days Overdue` > 90, \"High\", \r\n        if (x.`Days Overdue` > 30, \"Medium\", \r\n            if (`Days Overdue` = 0, \r\n                if (`First Overdue Installment` = 1, \"Low\", \"Very Low\"),\r\n                if (`First Overdue Installment` = 1, \"Medium\", \"Low\"))))\r\n\r\nas \"Loan Risk\", \r\nx.`First Overdue Installment`, x.`First Overdue Installment Date`\r\nfrom\r\n(select ounder.hierarchy, ounder.`name` as \"Office/Branch\", concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\", p.`name` as Loan,\r\nl.id as \"Loan ID\", l.disbursedon_date as Disbursed, ifnull(cur.display_symbol, l.currency_code) as Currency,\r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Outstanding\",\r\nsum(r.interest_amount - ifnull(r.interest_completed_derived, 0)) as \"Interest Outstanding\",\r\nif(datediff(curdate(), min(duedate)) < 0, 0, datediff(curdate(), min(duedate))) as \"Days Overdue\",   \r\nmin(installment) as \"First Overdue Installment\",\r\nmin(duedate) as \"First Overdue Installment Date\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) as \"Principal Overdue\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.interest_amount - ifnull(r.interest_completed_derived, 0))\r\n            , 0)) as \"Interest Overdue\"\r\n\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\njoin portfolio_loan l on l.client_id = c.id\r\nleft join ref_currency cur on cur.code = l.currency_code\r\nleft join portfolio_product_loan p on p.id = l.product_id\r\nleft join portfolio_loan_repayment_schedule r on r.loan_id = l.id\r\n                                        and r.completed_derived is false\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand l.loan_status_id = 300\r\ngroup by l.id) x\r\norder by x.hierarchy, x.Currency, x.`Name`, x.Loan, x.`Loan ID`'),(35,'Portfolio at Risk','Table',NULL,NULL,'select  \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Outstanding\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) as \"Principal Overdue\",\r\n            \r\n    cast(round(\r\n    (sum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) * 100) / \r\n            sum(r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 2) as char)\r\n            as \"Portfolio at Risk %\"\r\n            \r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\njoin  portfolio_loan l on l.client_id = c.id\r\nleft join portfolio_product_loan p on p.id = l.product_id\r\nleft join portfolio_loan_repayment_schedule r on r.loan_id = l.id\r\n                                        and r.completed_derived is false\r\nleft join ref_currency cur on cur.code = l.currency_code\r\n\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand l.loan_status_id = 300\r\ngroup by l.currency_code\r\norder by l.currency_code'),(38,'Upcoming Repayments Summary By Month, Currency and Loan Risk','Table',NULL,NULL,'SELECT DATE_FORMAT(r.duedate,\'%Y-%m\') \"Due Month\",  ifnull(cur.display_symbol, l.currency_code) as Currency,  \r\n\r\n(select\r\n    if(if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) > 90, \"4 High\", \r\n        if (if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) > 30, \"3 Medium\", \r\n            if (if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) = 0, \r\n                if (min(r.installment) = 1, \"Low\", \"1 Very Low\"),\r\n                if (min(r.installment), \"3 Medium\", \"2 Low\")))) \r\nfrom portfolio_loan lr\r\nleft join portfolio_loan_repayment_schedule r on r.loan_id = lr.id\r\n                                        and r.completed_derived is false\r\nwhere lr.id = l.id) as \"Loan Risk\",\r\n\r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Due\",\r\nsum(r.interest_amount - ifnull(r.interest_completed_derived, 0)) as \"Interest Due\",\r\n\r\n(sum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) +\r\nsum(r.interest_amount - ifnull(r.interest_completed_derived, 0))) as \"Total Due\"\r\n\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\njoin portfolio_loan l on l.client_id = c.id\r\njoin portfolio_loan_repayment_schedule r on r.loan_id = l.id\r\nleft join ref_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\" )\r\nand l.loan_status_id = 300\r\nand r.duedate >= curdate()\r\ngroup by `Due Month`, `Currency`, `Loan Risk`\r\norder by `Due Month`,  `Loan Risk`'),(39,'Loan Disbursals in Previous Year','Chart','Bar',NULL,'select prevYr.startMonth as \"Disbursed Month\", ifnull(monthlyDisb.disbursed,0) as Disbursed\r\nfrom\r\n(SELECT DATE_FORMAT(@startDate := date_add(@startDate, interval 1 Month),\'%Y-%m\') as startMonth\r\nFROM rpt_sequence s, (SELECT @startDate := date_add(curdate(), interval -13 Month)) d\r\nWHERE s.id <= 13) prevYr\r\nleft join \r\n(select DATE_FORMAT(lt.transaction_date,\'%Y-%m\') as txnMonth, sum(lt.amount) as disbursed\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\njoin portfolio_loan l on l.client_id = c.id\r\njoin portfolio_loan_transaction lt on lt.loan_id = l.id\r\nwhere o.id = ${officeId}\r\nand l.currency_code = \"${currencyId}\"\r\nand lt.transaction_type_enum = 1\r\ngroup by `txnMonth`) monthlyDisb on monthlyDisb.txnMonth = prevYr.startMonth'),(40,'Loan Repayments in Previous Year','Chart','Bar',NULL,'select prevYr.startMonth as \"Repayment Month\", ifnull(monthlyR.repaid,0) as Repaid\r\nfrom\r\n(SELECT DATE_FORMAT(@startDate := date_add(@startDate, interval 1 Month),\'%Y-%m\') as startMonth\r\nFROM rpt_sequence s, (SELECT @startDate := date_add(curdate(), interval -13 Month)) d\r\nWHERE s.id <= 13) prevYr\r\nleft join \r\n(select DATE_FORMAT(lt.transaction_date,\'%Y-%m\') as txnMonth, sum(lt.amount) as repaid\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\njoin portfolio_loan l on l.client_id = c.id\r\njoin portfolio_loan_transaction lt on lt.loan_id = l.id\r\nwhere o.id = ${officeId}\r\nand l.currency_code = \"${currencyId}\"\r\nand lt.transaction_type_enum = 2 and lt.contra_id is null\r\ngroup by `txnMonth`) monthlyR on monthlyR.txnMonth = prevYr.startMonth\r\norder by prevYr.startMonth'),(41,'Clients by Surname (a bit silly)','Chart','Pie',NULL,'select c.lastname, count(*)  as thecount\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\nwhere o.id = ${officeId}\r\ngroup by c.lastname'),(42,'Client Listing - Additional and Unlikely','Table',NULL,NULL,'select ounder.`name` as \"Office/Branch\",  concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\",  \r\na1.`Ethnic Group`, a1.`Ethnic Group Other`, a1.`Household Location`, a1. `Household Location Other`,\r\na1.Religion, a1.`Religion Other`, a1.`Knowledge of Person`, a1.Gender, a1.whois,\r\na2.`Fathers Favourite Team`, a2.`Mothers Favourite Team`, a2.`Fathers DOB`, a2.`Mothers DOB`,\r\na2.`Fathers Education`, a2.`Mothers Education`, a2.`Number of Children`,\r\na2.`Favourite Town`, a2.`Closing Comments`, a2.`Annual Family Income`\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\nleft join `portfolio_client_extra_Additional Information` a1 on a1.id = c.id\r\nleft join `portfolio_client_extra_Highly Improbable Info` a2 on a2.id = c.id\r\nwhere o.id = ${officeId}\r\norder by ounder.hierarchy'),(43,'Client Loans Listing - Additional','Table',NULL,NULL,'select ounder.`name` as \"Office/Branch\", concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\",\r\np.name as Loan, st.display_name as \"Status\", \r\nl.annual_nominal_interest_rate as \" Annual Nominal Interest Rate\", \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  l.principal_amount,\r\nl1.`Business Location`, l1.`Business Location Other`, l1.`Business` as \"Business Type\", \r\nl1.`Business Description`, l1.`Business Title`\r\nfrom org_office o \r\njoin org_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin portfolio_client c on c.office_id = ounder.id\r\nleft join portfolio_loan l on l.client_id = c.id\r\nleft join `portfolio_loan_extra_Additional Information` l1 on l1.id = l.id\r\nleft join portfolio_product_loan p on p.id = l.product_id\r\nleft join ref_loan_status st on st.id = l.loan_status_id\r\nleft join ref_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\norder by ounder.hierarchy, \"Name\" , l.id');
/*!40000 ALTER TABLE `stretchy_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stretchy_report_parameter`
--

DROP TABLE IF EXISTS `stretchy_report_parameter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stretchy_report_parameter` (
  `report_id` int(11) NOT NULL,
  `parameter_id` int(11) NOT NULL,
  `report_parameter_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`report_id`,`parameter_id`),
  UNIQUE KEY `report_id_name_UNIQUE` (`report_id`,`report_parameter_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchy_report_parameter`
--

LOCK TABLES `stretchy_report_parameter` WRITE;
/*!40000 ALTER TABLE `stretchy_report_parameter` DISABLE KEYS */;
INSERT INTO `stretchy_report_parameter` VALUES (29,5,NULL),(30,5,NULL),(31,5,NULL),(32,5,NULL),(32,6,NULL),(34,5,NULL),(34,6,NULL),(35,5,NULL),(35,6,NULL),(38,5,NULL),(38,6,NULL),(39,5,NULL),(39,7,NULL),(40,5,NULL),(40,7,NULL),(41,5,NULL),(42,5,NULL),(43,5,NULL);
/*!40000 ALTER TABLE `stretchy_report_parameter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stretchydata_allowed_list`
--

DROP TABLE IF EXISTS `stretchydata_allowed_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stretchydata_allowed_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchydata_allowed_list`
--

LOCK TABLES `stretchydata_allowed_list` WRITE;
/*!40000 ALTER TABLE `stretchydata_allowed_list` DISABLE KEYS */;
INSERT INTO `stretchydata_allowed_list` VALUES (2,'Business'),(8,'Education'),(3,'Ethnic Group'),(7,'Football Team'),(4,'Gender'),(5,'Knowledge of Person'),(1,'Location'),(6,'Religion');
/*!40000 ALTER TABLE `stretchydata_allowed_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stretchydata_allowed_value`
--

DROP TABLE IF EXISTS `stretchydata_allowed_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stretchydata_allowed_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `allowed_list_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `stretchydata_allowed_value_fk1` (`allowed_list_id`,`name`),
  CONSTRAINT `stretchydata_allowed_value_fk1` FOREIGN KEY (`allowed_list_id`) REFERENCES `stretchydata_allowed_list` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchydata_allowed_value`
--

LOCK TABLES `stretchydata_allowed_value` WRITE;
/*!40000 ALTER TABLE `stretchydata_allowed_value` DISABLE KEYS */;
INSERT INTO `stretchydata_allowed_value` VALUES (1,'East Sikkim',1),(2,'North Sikkim',1),(6,'Other',1),(3,'South Sikkim',1),(4,'West Sikkim',1),(8,'Existing',2),(7,'New',2),(15,'Bedouin',3),(16,'Berber',3),(17,'Italian',3),(18,'Miknasa',3),(19,'Mrazig',3),(20,'Other',3),(46,'Unknown',3),(21,'Female',4),(22,'Male',4),(26,'Friend of staff member',5),(27,'Not known by any staff member',5),(28,'Other',5),(25,'Son/daughter of staff member',5),(24,'Spouse of staff member',5),(23,'Staff member',5),(32,'Animist',6),(33,'Atheist',6),(30,'Catholic',6),(31,'Muslim',6),(34,'Other',6),(29,'Protestant',6),(35,'Unknown',6),(36,'AC Milan',7),(37,'Juventus',7),(39,'Manchester Utd',7),(45,'None, hates soccer',7),(38,'Sao Paulo',7),(44,'None',8),(40,'Primary',8),(41,'Secondary',8),(42,'Tertiary',8),(43,'Trade',8);
/*!40000 ALTER TABLE `stretchydata_allowed_value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stretchydata_dataset`
--

DROP TABLE IF EXISTS `stretchydata_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stretchydata_dataset` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `datasettype_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `stretchydata_dataset_fk1` (`datasettype_id`,`name`),
  CONSTRAINT `stretchydata_dataset` FOREIGN KEY (`datasettype_id`) REFERENCES `stretchydata_datasettype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchydata_dataset`
--

LOCK TABLES `stretchydata_dataset` WRITE;
/*!40000 ALTER TABLE `stretchydata_dataset` DISABLE KEYS */;
INSERT INTO `stretchydata_dataset` VALUES (1,'Additional Information',1),(9,'Highly Improbable Info',1),(8,'Additional Information',2);
/*!40000 ALTER TABLE `stretchydata_dataset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stretchydata_dataset_fields`
--

DROP TABLE IF EXISTS `stretchydata_dataset_fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stretchydata_dataset_fields` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `data_type` varchar(15) NOT NULL,
  `data_length` int(11) DEFAULT NULL,
  `display_type` varchar(45) DEFAULT NULL,
  `dataset_id` int(11) NOT NULL,
  `allowed_list_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `stretchydata_dataset_fields_fk1` (`dataset_id`,`name`),
  KEY `stretchydata_dataset_fields_fk2` (`allowed_list_id`),
  CONSTRAINT `stretchydata_dataset_fields_fk1` FOREIGN KEY (`dataset_id`) REFERENCES `stretchydata_dataset` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `stretchydata_dataset_fields_fk2` FOREIGN KEY (`allowed_list_id`) REFERENCES `stretchydata_allowed_list` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchydata_dataset_fields`
--

LOCK TABLES `stretchydata_dataset_fields` WRITE;
/*!40000 ALTER TABLE `stretchydata_dataset_fields` DISABLE KEYS */;
INSERT INTO `stretchydata_dataset_fields` VALUES (2,'Business Location','String',50,'List',8,1),(3,'Business Location Other','String',50,NULL,8,NULL),(4,'Business','String',10,'List',8,2),(5,'Business Description','Text',NULL,NULL,8,NULL),(6,'Ethnic Group','String',50,'List',1,3),(7,'Ethnic Group Other','String',50,NULL,1,NULL),(8,'Household Location','String',50,'List',1,1),(9,'Household Location Other','String',50,NULL,1,NULL),(10,'Religion','String',50,'List',1,6),(11,'Religion Other','String',50,NULL,1,NULL),(12,'Knowledge of Person','String',50,'List',1,5),(13,'Gender','String',10,'List',1,4),(14,'Business Title','String',100,NULL,8,NULL),(15,'Whois','Text',NULL,NULL,1,NULL),(60,'Fathers Favourite Team','String',50,'List',9,7),(61,'Mothers Favourite Team','String',50,'List',9,7),(62,'Fathers DOB','Date',NULL,NULL,9,NULL),(63,'Mothers DOB','Date',NULL,NULL,9,NULL),(64,'Fathers Education','String',50,'List',9,8),(65,'Mothers Education','String',50,'List',9,8),(67,'Number of Children','Integer',NULL,NULL,9,NULL),(68,'Favourite Town','String',30,NULL,9,NULL),(69,'Closing Comments','Text',NULL,NULL,9,NULL),(70,'Annual Family Income','Decimal',NULL,NULL,9,NULL);
/*!40000 ALTER TABLE `stretchydata_dataset_fields` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stretchydata_datasettype`
--

DROP TABLE IF EXISTS `stretchydata_datasettype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stretchydata_datasettype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchydata_datasettype`
--

LOCK TABLES `stretchydata_datasettype` WRITE;
/*!40000 ALTER TABLE `stretchydata_datasettype` DISABLE KEYS */;
INSERT INTO `stretchydata_datasettype` VALUES (1,'portfolio_client'),(2,'portfolio_loan');
/*!40000 ALTER TABLE `stretchydata_datasettype` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-07-17  5:32:33
