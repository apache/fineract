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
-- Table structure for table `m_appuser`
--

DROP TABLE IF EXISTS `m_appuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_appuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
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
  CONSTRAINT `FKB3D587CE0DD567A` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_appuser`
--

LOCK TABLES `m_appuser` WRITE;
/*!40000 ALTER TABLE `m_appuser` DISABLE KEYS */;
INSERT INTO `m_appuser` VALUES (1,0,1,'mifos','App(test)','Administrator','5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a','demomfi@mifos.org','\0','','','','',NULL,NULL,'2012-07-24 17:29:18',1),(3,0,1,'Rich','ard','Rich','7f4f0cfd724ea68218c53474baa7dc248e3d495152b41ea4714b56250ab47a00','rich@mifos.org','','','','','',1,'2012-07-19 15:48:49','2012-07-19 15:49:18',1),(4,0,7,'Mawi a','Mawi a','Lal','0faa62bef4ab6769e87d96bddd326330ffa35c91a0c9a8078dab6fba92f2819e','mawia@mifos.org','','','','','',1,'2012-07-19 15:52:00','2012-07-19 15:52:01',1),(5,0,12,'hugo','hugo','technologies','bcddd6fdfde05842bfeff1513ae6b3d4deaa716e0d349b939539e834aa04de7f','info@hugotechnologies.com','','','','','',1,'2012-07-23 04:28:51','2012-07-23 04:28:53',1),(6,0,1,'readonly1','readonly','example','e2f88de29fec7a561257b49bb14f837a72c50d288ff0ed4bbd5197b35a756062','fake@gmail.com','\0','','','','',1,'2012-08-21 21:56:30','2012-08-21 21:57:19',6);
/*!40000 ALTER TABLE `m_appuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_appuser_role`
--

DROP TABLE IF EXISTS `m_appuser_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_appuser_role` (
  `appuser_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`appuser_id`,`role_id`),
  KEY `FK7662CE59B4100309` (`appuser_id`),
  KEY `FK7662CE5915CEC7AB` (`role_id`),
  CONSTRAINT `FK7662CE5915CEC7AB` FOREIGN KEY (`role_id`) REFERENCES `m_role` (`id`),
  CONSTRAINT `FK7662CE59B4100309` FOREIGN KEY (`appuser_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_appuser_role`
--

LOCK TABLES `m_appuser_role` WRITE;
/*!40000 ALTER TABLE `m_appuser_role` DISABLE KEYS */;
INSERT INTO `m_appuser_role` VALUES (1,1),(3,1),(4,2),(5,1),(6,7);
/*!40000 ALTER TABLE `m_appuser_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_charge`
--

DROP TABLE IF EXISTS `m_charge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_charge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `charge_applies_to_enum` smallint(5) NOT NULL,
  `charge_time_enum` smallint(5) NOT NULL,
  `charge_calculation_enum` smallint(5) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_charge`
--

LOCK TABLES `m_charge` WRITE;
/*!40000 ALTER TABLE `m_charge` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_charge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_client`
--

DROP TABLE IF EXISTS `m_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_client` (
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
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FKCE00CAB3E0DD567A` (`office_id`),
  KEY `FKAUD0000000000001` (`createdby_id`),
  KEY `FKAUD0000000000002` (`lastmodifiedby_id`),
  CONSTRAINT `FKAUD0000000000001` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FKAUD0000000000002` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FKCE00CAB3E0DD567A` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_client`
--

LOCK TABLES `m_client` WRITE;
/*!40000 ALTER TABLE `m_client` DISABLE KEYS */;
INSERT INTO `m_client` VALUES (1,1,NULL,'Willie','O\'Meara','2009-01-04',1,'2012-04-12 22:07:44','2012-04-12 22:07:44',1,0),(2,1,NULL,'Declan','Browne','2009-01-04',1,'2012-04-12 22:15:39','2012-04-12 22:15:39',1,0),(3,1,NULL,'Ja','Fallon','2009-01-11',1,'2012-04-12 22:16:30','2012-04-12 22:16:30',1,0),(4,1,NULL,'Peter','Lambert','2009-01-11',1,'2012-04-12 22:17:01','2012-04-12 22:17:01',1,0),(5,1,NULL,NULL,'Sunnyville vegetable growers Ltd','2009-01-24',1,'2012-04-12 22:19:18','2012-04-12 22:19:18',1,0),(6,2,NULL,'Jacques','Lee','2012-04-03',1,'2012-04-14 06:09:22','2012-08-01 13:50:13',1,0),(7,1,NULL,'Kalilou','Traor','2009-02-04',1,'2012-04-14 09:38:11','2012-04-14 09:38:11',1,0),(8,1,NULL,'Sidi','Kon','2009-02-11',1,'2012-04-14 09:45:43','2012-04-14 09:45:43',1,0),(9,1,NULL,'Moustapha','Yattabar','2009-02-18',1,'2012-04-14 10:24:20','2012-04-14 10:24:20',1,0),(10,1,NULL,NULL,'Mali fruit sales ltd.','2009-02-18',1,'2012-04-14 10:25:37','2012-04-14 10:25:37',1,0),(11,1,NULL,NULL,'Djenne co-op group','2009-02-25',1,'2012-04-14 10:31:03','2012-04-14 10:31:03',1,0),(12,1,NULL,NULL,'Heavens Family','2012-07-19',1,'2012-07-19 15:03:47','2012-07-19 15:03:47',1,0),(13,7,NULL,NULL,'U Thanung','2012-07-19',1,'2012-07-19 16:01:49','2012-07-19 16:01:49',1,0),(14,16,NULL,'Customer','One','2012-07-23',1,'2012-07-23 04:27:18','2012-07-23 04:27:18',1,0),(15,12,NULL,NULL,'HGT 28','2012-07-28',1,'2012-07-28 11:28:16','2012-07-28 11:28:16',1,0),(16,10,NULL,'Wale','Afolabi','2012-08-19',1,'2012-08-19 08:58:44','2012-08-20 04:17:26',1,0),(17,6,NULL,NULL,'Agri','2012-08-27',1,'2012-08-27 17:02:28','2012-08-27 17:02:28',1,0);
/*!40000 ALTER TABLE `m_client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_client_xAdditional Information`
--

DROP TABLE IF EXISTS `m_client_xAdditional Information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_client_xAdditional Information` (
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
  CONSTRAINT `portfolio_client_extra_Additional Information_fk1` FOREIGN KEY (`id`) REFERENCES `m_client` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_client_xAdditional Information`
--

LOCK TABLES `m_client_xAdditional Information` WRITE;
/*!40000 ALTER TABLE `m_client_xAdditional Information` DISABLE KEYS */;
INSERT INTO `m_client_xAdditional Information` VALUES (1,'Italian',NULL,'North Sikkim',NULL,NULL,NULL,'Friend of staff member','Male','More info about this and other things\r\n\r\nuntil the end'),(4,'Bedouin',NULL,'North Sikkim',NULL,'Atheist',NULL,'Staff member',NULL,NULL),(13,'Other','Chin','Other','Zatual','Protestant',NULL,'Other','Male',NULL),(15,'Berber',NULL,'South Sikkim',NULL,'Animist',NULL,NULL,'Male',NULL),(16,NULL,NULL,NULL,NULL,'Unknown',NULL,NULL,NULL,NULL),(17,'Berber',NULL,'North Sikkim',NULL,'Muslim',NULL,'Spouse of staff member','Male',NULL);
/*!40000 ALTER TABLE `m_client_xAdditional Information` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_client_xHighly Improbable Info`
--

DROP TABLE IF EXISTS `m_client_xHighly Improbable Info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_client_xHighly Improbable Info` (
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
  CONSTRAINT `portfolio_client_extra_Highly Improbable Info_fk1` FOREIGN KEY (`id`) REFERENCES `m_client` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_client_xHighly Improbable Info`
--

LOCK TABLES `m_client_xHighly Improbable Info` WRITE;
/*!40000 ALTER TABLE `m_client_xHighly Improbable Info` DISABLE KEYS */;
INSERT INTO `m_client_xHighly Improbable Info` VALUES (1,'AC Milan','Manchester Utd',NULL,NULL,'Secondary','Secondary',NULL,NULL,NULL,NULL),(2,'Sao Paulo',NULL,'2012-07-12',NULL,NULL,NULL,NULL,NULL,'bally\r\n\r\ncl',NULL),(3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(16,'Sao Paulo',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(17,'Juventus',NULL,'2012-08-15',NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `m_client_xHighly Improbable Info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_code`
--

DROP TABLE IF EXISTS `m_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_code` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_name` (`code_name`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_code`
--

LOCK TABLES `m_code` WRITE;
/*!40000 ALTER TABLE `m_code` DISABLE KEYS */;
INSERT INTO `m_code` VALUES (1,'Business'),(2,'Education'),(3,'Ethnic Group'),(4,'Football Team'),(5,'Gender'),(6,'Knowledge of Person'),(7,'Location'),(8,'Religion');
/*!40000 ALTER TABLE `m_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_code_value`
--

DROP TABLE IF EXISTS `m_code_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_code_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code_id` int(11) NOT NULL,
  `code_value` varchar(100) DEFAULT NULL,
  `order_position` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_value` (`code_id`,`code_value`),
  KEY `FKCFCEA42640BE071Z` (`code_id`),
  CONSTRAINT `FKCFCEA42640BE071Z` FOREIGN KEY (`code_id`) REFERENCES `m_code` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_code_value`
--

LOCK TABLES `m_code_value` WRITE;
/*!40000 ALTER TABLE `m_code_value` DISABLE KEYS */;
INSERT INTO `m_code_value` VALUES (1,1,'Existing',8),(2,1,'New',7),(3,2,'None',44),(4,2,'Primary',40),(5,2,'Secondary',41),(6,2,'Tertiary',42),(7,2,'Trade',43),(8,3,'Bedouin',15),(9,3,'Berber',16),(10,3,'Italian',17),(11,3,'Miknasa',18),(12,3,'Mrazig',19),(13,3,'Other',20),(14,3,'Unknown',46),(15,4,'AC Milan',36),(16,4,'Juventus',37),(17,4,'Manchester Utd',39),(18,4,'None, hates soccer',45),(19,4,'Sao Paulo',38),(20,5,'Female',21),(21,5,'Male',22),(22,6,'Friend of staff member',26),(23,6,'Not known by any staff member',27),(24,6,'Other',28),(25,6,'Son/daughter of staff member',25),(26,6,'Spouse of staff member',24),(27,6,'Staff member',23),(28,7,'East Sikkim',1),(29,7,'North Sikkim',2),(30,7,'Other',6),(31,7,'South Sikkim',3),(32,7,'West Sikkim',4),(33,8,'Animist',32),(34,8,'Atheist',33),(35,8,'Catholic',30),(36,8,'Muslim',31),(37,8,'Other',34),(38,8,'Protestant',29),(39,8,'Unknown',35);
/*!40000 ALTER TABLE `m_code_value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_currency`
--

DROP TABLE IF EXISTS `m_currency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_currency` (
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
-- Dumping data for table `m_currency`
--

LOCK TABLES `m_currency` WRITE;
/*!40000 ALTER TABLE `m_currency` DISABLE KEYS */;
INSERT INTO `m_currency` VALUES (1,'AED',2,NULL,'UAE Dirham','currency.AED'),(2,'AFN',2,NULL,'Afghanistan Afghani','currency.AFN'),(3,'ALL',2,NULL,'Albanian Lek','currency.ALL'),(4,'AMD',2,NULL,'Armenian Dram','currency.AMD'),(5,'ANG',2,NULL,'Netherlands Antillian Guilder','currency.ANG'),(6,'AOA',2,NULL,'Angolan Kwanza','currency.AOA'),(7,'ARS',2,NULL,'Argentine Peso','currency.ARS'),(8,'AUD',2,NULL,'Australian Dollar','currency.AUD'),(9,'AWG',2,NULL,'Aruban Guilder','currency.AWG'),(10,'AZM',2,NULL,'Azerbaijanian Manat','currency.AZM'),(11,'BAM',2,NULL,'Bosnia and Herzegovina Convertible Marks','currency.BAM'),(12,'BBD',2,NULL,'Barbados Dollar','currency.BBD'),(13,'BDT',2,NULL,'Bangladesh Taka','currency.BDT'),(14,'BGN',2,NULL,'Bulgarian Lev','currency.BGN'),(15,'BHD',3,NULL,'Bahraini Dinar','currency.BHD'),(16,'BIF',0,NULL,'Burundi Franc','currency.BIF'),(17,'BMD',2,NULL,'Bermudian Dollar','currency.BMD'),(18,'BND',2,'B$','Brunei Dollar','currency.BND'),(19,'BOB',2,NULL,'Bolivian Boliviano','currency.BOB'),(20,'BRL',2,NULL,'Brazilian Real','currency.BRL'),(21,'BSD',2,NULL,'Bahamian Dollar','currency.BSD'),(22,'BTN',2,NULL,'Bhutan Ngultrum','currency.BTN'),(23,'BWP',2,NULL,'Botswana Pula','currency.BWP'),(24,'BYR',0,NULL,'Belarussian Ruble','currency.BYR'),(25,'BZD',2,NULL,'Belize Dollar','currency.BZD'),(26,'CAD',2,NULL,'Canadian Dollar','currency.CAD'),(27,'CDF',2,NULL,'Franc Congolais','currency.CDF'),(28,'CHF',2,NULL,'Swiss Franc','currency.CHF'),(29,'CLP',0,NULL,'Chilean Peso','currency.CLP'),(30,'CNY',2,NULL,'Chinese Yuan Renminbi','currency.CNY'),(31,'COP',2,NULL,'Colombian Peso','currency.COP'),(32,'CRC',2,NULL,'Costa Rican Colon','currency.CRC'),(33,'CSD',2,NULL,'Serbian Dinar','currency.CSD'),(34,'CUP',2,NULL,'Cuban Peso','currency.CUP'),(35,'CVE',2,NULL,'Cape Verde Escudo','currency.CVE'),(36,'CYP',2,NULL,'Cyprus Pound','currency.CYP'),(37,'CZK',2,NULL,'Czech Koruna','currency.CZK'),(38,'DJF',0,NULL,'Djibouti Franc','currency.DJF'),(39,'DKK',2,NULL,'Danish Krone','currency.DKK'),(40,'DOP',2,NULL,'Dominican Peso','currency.DOP'),(41,'DZD',2,NULL,'Algerian Dinar','currency.DZD'),(42,'EEK',2,NULL,'Estonian Kroon','currency.EEK'),(43,'EGP',2,NULL,'Egyptian Pound','currency.EGP'),(44,'ERN',2,NULL,'Eritrea Nafka','currency.ERN'),(45,'ETB',2,NULL,'Ethiopian Birr','currency.ETB'),(46,'EUR',2,NULL,'euro','currency.EUR'),(47,'FJD',2,NULL,'Fiji Dollar','currency.FJD'),(48,'FKP',2,NULL,'Falkland Islands Pound','currency.FKP'),(49,'GBP',2,NULL,'Pound Sterling','currency.GBP'),(50,'GEL',2,NULL,'Georgian Lari','currency.GEL'),(51,'GHC',2,'GHc','Ghana Cedi','currency.GHC'),(52,'GIP',2,NULL,'Gibraltar Pound','currency.GIP'),(53,'GMD',2,NULL,'Gambian Dalasi','currency.GMD'),(54,'GNF',0,NULL,'Guinea Franc','currency.GNF'),(55,'GTQ',2,NULL,'Guatemala Quetzal','currency.GTQ'),(56,'GYD',2,NULL,'Guyana Dollar','currency.GYD'),(57,'HKD',2,NULL,'Hong Kong Dollar','currency.HKD'),(58,'HNL',2,NULL,'Honduras Lempira','currency.HNL'),(59,'HRK',2,NULL,'Croatian Kuna','currency.HRK'),(60,'HTG',2,NULL,'Haiti Gourde','currency.HTG'),(61,'HUF',2,NULL,'Hungarian Forint','currency.HUF'),(62,'IDR',2,NULL,'Indonesian Rupiah','currency.IDR'),(63,'ILS',2,NULL,'New Israeli Shekel','currency.ILS'),(64,'INR',2,NULL,'Indian Rupee','currency.INR'),(65,'IQD',3,NULL,'Iraqi Dinar','currency.IQD'),(66,'IRR',2,NULL,'Iranian Rial','currency.IRR'),(67,'ISK',0,NULL,'Iceland Krona','currency.ISK'),(68,'JMD',2,NULL,'Jamaican Dollar','currency.JMD'),(69,'JOD',3,NULL,'Jordanian Dinar','currency.JOD'),(70,'JPY',0,NULL,'Japanese Yen','currency.JPY'),(71,'KES',2,'KSh','Kenyan Shilling','currency.KES'),(72,'KGS',2,NULL,'Kyrgyzstan Som','currency.KGS'),(73,'KHR',2,NULL,'Cambodia Riel','currency.KHR'),(74,'KMF',0,NULL,'Comoro Franc','currency.KMF'),(75,'KPW',2,NULL,'North Korean Won','currency.KPW'),(76,'KRW',0,NULL,'Korean Won','currency.KRW'),(77,'KWD',3,NULL,'Kuwaiti Dinar','currency.KWD'),(78,'KYD',2,NULL,'Cayman Islands Dollar','currency.KYD'),(79,'KZT',2,NULL,'Kazakhstan Tenge','currency.KZT'),(80,'LAK',2,NULL,'Lao Kip','currency.LAK'),(81,'LBP',2,'L£','Lebanese Pound','currency.LBP'),(82,'LKR',2,NULL,'Sri Lanka Rupee','currency.LKR'),(83,'LRD',2,NULL,'Liberian Dollar','currency.LRD'),(84,'LSL',2,NULL,'Lesotho Loti','currency.LSL'),(85,'LTL',2,NULL,'Lithuanian Litas','currency.LTL'),(86,'LVL',2,NULL,'Latvian Lats','currency.LVL'),(87,'LYD',3,NULL,'Libyan Dinar','currency.LYD'),(88,'MAD',2,NULL,'Moroccan Dirham','currency.MAD'),(89,'MDL',2,NULL,'Moldovan Leu','currency.MDL'),(90,'MGA',2,NULL,'Malagasy Ariary','currency.MGA'),(91,'MKD',2,NULL,'Macedonian Denar','currency.MKD'),(92,'MMK',2,'K','Myanmar Kyat','currency.MMK'),(93,'MNT',2,NULL,'Mongolian Tugrik','currency.MNT'),(94,'MOP',2,NULL,'Macau Pataca','currency.MOP'),(95,'MRO',2,NULL,'Mauritania Ouguiya','currency.MRO'),(96,'MTL',2,NULL,'Maltese Lira','currency.MTL'),(97,'MUR',2,NULL,'Mauritius Rupee','currency.MUR'),(98,'MVR',2,NULL,'Maldives Rufiyaa','currency.MVR'),(99,'MWK',2,NULL,'Malawi Kwacha','currency.MWK'),(100,'MXN',2,NULL,'Mexican Peso','currency.MXN'),(101,'MYR',2,NULL,'Malaysian Ringgit','currency.MYR'),(102,'MZM',2,NULL,'Mozambique Metical','currency.MZM'),(103,'NAD',2,NULL,'Namibia Dollar','currency.NAD'),(104,'NGN',2,NULL,'Nigerian Naira','currency.NGN'),(105,'NIO',2,NULL,'Nicaragua Cordoba Oro','currency.NIO'),(106,'NOK',2,NULL,'Norwegian Krone','currency.NOK'),(107,'NPR',2,NULL,'Nepalese Rupee','currency.NPR'),(108,'NZD',2,NULL,'New Zealand Dollar','currency.NZD'),(109,'OMR',3,NULL,'Rial Omani','currency.OMR'),(110,'PAB',2,NULL,'Panama Balboa','currency.PAB'),(111,'PEN',2,NULL,'Peruvian Nuevo Sol','currency.PEN'),(112,'PGK',2,NULL,'Papua New Guinea Kina','currency.PGK'),(113,'PHP',2,NULL,'Philippine Peso','currency.PHP'),(114,'PKR',2,NULL,'Pakistan Rupee','currency.PKR'),(115,'PLN',2,NULL,'Polish Zloty','currency.PLN'),(116,'PYG',0,NULL,'Paraguayan Guarani','currency.PYG'),(117,'QAR',2,NULL,'Qatari Rial','currency.QAR'),(118,'RON',2,NULL,'Romanian Leu','currency.RON'),(119,'RUB',2,NULL,'Russian Ruble','currency.RUB'),(120,'RWF',0,NULL,'Rwanda Franc','currency.RWF'),(121,'SAR',2,NULL,'Saudi Riyal','currency.SAR'),(122,'SBD',2,NULL,'Solomon Islands Dollar','currency.SBD'),(123,'SCR',2,NULL,'Seychelles Rupee','currency.SCR'),(124,'SDD',2,NULL,'Sudanese Dinar','currency.SDD'),(125,'SEK',2,NULL,'Swedish Krona','currency.SEK'),(126,'SGD',2,NULL,'Singapore Dollar','currency.SGD'),(127,'SHP',2,NULL,'St Helena Pound','currency.SHP'),(128,'SIT',2,NULL,'Slovenian Tolar','currency.SIT'),(129,'SKK',2,NULL,'Slovak Koruna','currency.SKK'),(130,'SLL',2,NULL,'Sierra Leone Leone','currency.SLL'),(131,'SOS',2,NULL,'Somali Shilling','currency.SOS'),(132,'SRD',2,NULL,'Surinam Dollar','currency.SRD'),(133,'STD',2,NULL,'Sao Tome and Principe Dobra','currency.STD'),(134,'SVC',2,NULL,'El Salvador Colon','currency.SVC'),(135,'SYP',2,NULL,'Syrian Pound','currency.SYP'),(136,'SZL',2,NULL,'Swaziland Lilangeni','currency.SZL'),(137,'THB',2,NULL,'Thai Baht','currency.THB'),(138,'TJS',2,NULL,'Tajik Somoni','currency.TJS'),(139,'TMM',2,NULL,'Turkmenistan Manat','currency.TMM'),(140,'TND',3,'DT','Tunisian Dinar','currency.TND'),(141,'TOP',2,NULL,'Tonga Pa\'anga','currency.TOP'),(142,'TRY',2,NULL,'Turkish Lira','currency.TRY'),(143,'TTD',2,NULL,'Trinidad and Tobago Dollar','currency.TTD'),(144,'TWD',2,NULL,'New Taiwan Dollar','currency.TWD'),(145,'TZS',2,NULL,'Tanzanian Shilling','currency.TZS'),(146,'UAH',2,NULL,'Ukraine Hryvnia','currency.UAH'),(147,'UGX',2,'USh','Uganda Shilling','currency.UGX'),(148,'USD',2,'$','US Dollar','currency.USD'),(149,'UYU',2,NULL,'Peso Uruguayo','currency.UYU'),(150,'UZS',2,NULL,'Uzbekistan Sum','currency.UZS'),(151,'VEB',2,NULL,'Venezuelan Bolivar','currency.VEB'),(152,'VND',2,NULL,'Vietnamese Dong','currency.VND'),(153,'VUV',0,NULL,'Vanuatu Vatu','currency.VUV'),(154,'WST',2,NULL,'Samoa Tala','currency.WST'),(155,'XAF',0,NULL,'CFA Franc BEAC','currency.XAF'),(156,'XCD',2,NULL,'East Caribbean Dollar','currency.XCD'),(157,'XDR',5,NULL,'SDR (Special Drawing Rights)','currency.XDR'),(158,'XOF',0,'CFA','CFA Franc BCEAO','currency.XOF'),(159,'XPF',0,NULL,'CFP Franc','currency.XPF'),(160,'YER',2,NULL,'Yemeni Rial','currency.YER'),(161,'ZAR',2,'R','South African Rand','currency.ZAR'),(162,'ZMK',2,NULL,'Zambian Kwacha','currency.ZMK'),(163,'ZWD',2,NULL,'Zimbabwe Dollar','currency.ZWD');
/*!40000 ALTER TABLE `m_currency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_deposit_account`
--

DROP TABLE IF EXISTS `m_deposit_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_deposit_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `status_enum` smallint(5) NOT NULL DEFAULT '0',
  `external_id` varchar(100) DEFAULT NULL,
  `client_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `deposit_amount` decimal(19,6) DEFAULT NULL,
  `maturity_nominal_interest_rate` decimal(19,6) NOT NULL,
  `tenure_months` int(11) NOT NULL,
  `interest_compounded_every` smallint(5) NOT NULL DEFAULT '1',
  `interest_compounded_every_period_enum` smallint(5) NOT NULL DEFAULT '2',
  `projected_commencement_date` date NOT NULL,
  `actual_commencement_date` date DEFAULT NULL,
  `projected_maturity_date` date NOT NULL,
  `actual_maturity_date` date DEFAULT NULL,
  `projected_interest_accrued_on_maturity` decimal(19,6) NOT NULL,
  `actual_interest_accrued` decimal(19,6) DEFAULT NULL,
  `projected_total_maturity_amount` decimal(19,6) NOT NULL,
  `actual_total_amount` decimal(19,6) DEFAULT NULL,
  `is_renewal_allowed` tinyint(1) NOT NULL DEFAULT '0',
  `renewed_account_id` bigint(20) DEFAULT NULL,
  `is_preclosure_allowed` tinyint(1) NOT NULL DEFAULT '0',
  `pre_closure_interest_rate` decimal(19,6) NOT NULL,
  `withdrawnon_date` datetime DEFAULT NULL,
  `rejectedon_date` datetime DEFAULT NULL,
  `closedon_date` datetime DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `deposit_acc_external_id` (`external_id`),
  KEY `FKKW0000000000001` (`client_id`),
  KEY `FKKW0000000000002` (`product_id`),
  KEY `FKKW0000000000003` (`renewed_account_id`),
  CONSTRAINT `FKKW0000000000001` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKKW0000000000002` FOREIGN KEY (`product_id`) REFERENCES `m_product_deposit` (`id`),
  CONSTRAINT `FKKW0000000000003` FOREIGN KEY (`renewed_account_id`) REFERENCES `m_deposit_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_deposit_account`
--

LOCK TABLES `m_deposit_account` WRITE;
/*!40000 ALTER TABLE `m_deposit_account` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_deposit_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_deposit_account_transaction`
--

DROP TABLE IF EXISTS `m_deposit_account_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_deposit_account_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deposit_account_id` bigint(20) NOT NULL,
  `transaction_type_enum` smallint(5) NOT NULL,
  `contra_id` bigint(20) DEFAULT NULL,
  `transaction_date` date NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKKW00000000000005` (`deposit_account_id`),
  KEY `FKKW00000000000006` (`contra_id`),
  CONSTRAINT `FKKW00000000000005` FOREIGN KEY (`deposit_account_id`) REFERENCES `m_deposit_account` (`id`),
  CONSTRAINT `FKKW00000000000006` FOREIGN KEY (`contra_id`) REFERENCES `m_deposit_account_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_deposit_account_transaction`
--

LOCK TABLES `m_deposit_account_transaction` WRITE;
/*!40000 ALTER TABLE `m_deposit_account_transaction` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_deposit_account_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_fund`
--

DROP TABLE IF EXISTS `m_fund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_fund` (
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_fund`
--

LOCK TABLES `m_fund` WRITE;
/*!40000 ALTER TABLE `m_fund` DISABLE KEYS */;
INSERT INTO `m_fund` VALUES (1,'My First Fund',NULL,1,'2012-07-17 00:33:46',1,'2012-07-17 00:33:46'),(2,'high end fund',NULL,1,'2012-07-17 03:19:10',1,'2012-07-17 03:19:10'),(3,'General Fund','General',1,'2012-07-19 15:30:54',1,'2012-07-19 15:30:54'),(4,'FGW Fund','FGW',1,'2012-07-19 15:31:21',1,'2012-07-19 15:31:21');
/*!40000 ALTER TABLE `m_fund` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_group`
--

DROP TABLE IF EXISTS `m_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `external_id` (`external_id`),
  KEY `FKJPWG000000000003` (`createdby_id`),
  KEY `FKJPWG000000000004` (`lastmodifiedby_id`),
  CONSTRAINT `FKJPWG000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FKJPWG000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_group`
--

LOCK TABLES `m_group` WRITE;
/*!40000 ALTER TABLE `m_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_group_client`
--

DROP TABLE IF EXISTS `m_group_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_group_client` (
  `group_id` bigint(20) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  PRIMARY KEY (`group_id`,`client_id`),
  KEY `client_id` (`client_id`),
  CONSTRAINT `m_group_client_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `m_group_client_ibfk_2` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_group_client`
--

LOCK TABLES `m_group_client` WRITE;
/*!40000 ALTER TABLE `m_group_client` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_group_client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_loan`
--

DROP TABLE IF EXISTS `m_loan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_loan` (
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
  `term_frequency` smallint(5) NOT NULL DEFAULT '0',
  `term_period_frequency_enum` smallint(5) NOT NULL DEFAULT '2',
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
  `loan_transaction_strategy_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `org_id` (`external_id`),
  KEY `FKB6F935D87179A0CB` (`client_id`),
  KEY `FKB6F935D8C8D4B434` (`product_id`),
  KEY `FK7C885877240145` (`fund_id`),
  KEY `FK_loan_ltp_strategy` (`loan_transaction_strategy_id`),
  CONSTRAINT `FK7C885877240145` FOREIGN KEY (`fund_id`) REFERENCES `m_fund` (`id`),
  CONSTRAINT `FKB6F935D87179A0CB` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FKB6F935D8C8D4B434` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`),
  CONSTRAINT `FK_loan_ltp_strategy` FOREIGN KEY (`loan_transaction_strategy_id`) REFERENCES `ref_loan_transaction_processing_strategy` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_loan`
--

LOCK TABLES `m_loan` WRITE;
/*!40000 ALTER TABLE `m_loan` DISABLE KEYS */;
INSERT INTO `m_loan` VALUES (1,NULL,6,1,1,100,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,12,1,1,1,12,1,'\0','\0','0.000000','2012-07-04 01:09:52',NULL,'2012-07-17',NULL,NULL,NULL,'2012-10-09',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,1),(2,NULL,2,2,2,300,'LBP',2,'35000.000000','10.000000','1.000000',2,'12.000000',0,1,20,1,2,1,10,1,'\0','\0','0.000000','2012-07-01 03:39:58','2012-07-05 08:56:24','2012-07-17',NULL,NULL,'2012-07-17','2012-12-04',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-07-17 03:39:58','2012-07-20 08:56:30',1,1),(3,NULL,13,1,3,400,'XOF',0,'700000.000000','1000.000000','2.000000',2,'24.000000',1,1,12,2,1,2,12,1,'\0','\0','0.000000','2012-04-17 16:14:31',NULL,'2012-04-17','2012-05-18','2012-04-17',NULL,'2013-04-18',NULL,'2012-04-17 17:04:16',NULL,NULL,'2012-04-17 17:04:16',NULL,1,'2012-07-19 16:14:31','2012-07-19 17:04:16',1,1),(4,NULL,13,1,3,400,'XOF',0,'700000.000000','1000.000000','1.250000',2,'15.000000',1,1,12,2,1,2,12,1,'\0','\0','0.000000','2012-04-17 17:07:53',NULL,'2012-04-17','2012-05-18','2012-04-17',NULL,'2013-04-18',NULL,'2012-04-17 17:50:15',NULL,NULL,'2012-04-17 17:50:15',NULL,1,'2012-07-19 17:07:53','2012-07-19 17:50:15',1,1),(5,NULL,13,5,3,300,'MMK',0,'700000.000000','1000.000000','15.000000',3,'15.000000',1,1,12,2,1,2,12,1,'\0','\0','0.000000','2012-04-17 17:54:17','2012-04-17 17:55:40','2012-04-17','2012-05-18','2012-04-17','2012-04-17','2013-04-18',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-07-19 17:54:17','2012-07-19 17:55:54',1,1),(6,NULL,2,1,1,200,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',1,1,24,2,24,2,1,0,'\0','\0','0.000000','2012-07-20 04:54:55','2012-07-21 15:38:54','2012-07-20',NULL,NULL,NULL,'2014-07-20',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-07-20 04:54:55','2012-08-12 15:38:54',1,1),(9,NULL,5,5,4,100,'MMK',0,'50000.000000','0.000000','1.250000',2,'15.000000',0,1,24,2,12,2,2,1,'\0','\0','0.000000','2012-07-22 09:09:24',NULL,'2012-07-22',NULL,NULL,NULL,'2014-07-22',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-07-22 09:09:24','2012-07-22 09:09:24',1,1),(10,NULL,14,1,1,300,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,10,2,1,2,10,1,'\0','\0','0.000000','2012-07-23 04:51:37','2012-07-23 04:52:10','2012-07-23',NULL,NULL,'2012-07-23','2013-05-23',NULL,NULL,NULL,NULL,NULL,NULL,5,'2012-07-23 04:51:37','2012-07-23 04:52:24',5,1),(11,NULL,4,1,3,600,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,12,2,12,2,1,1,'\0','\0','0.000000','2012-07-31 12:53:53','2012-07-31 12:54:11','2012-07-31','2012-08-04','2012-08-19','2012-07-31','2012-08-04','2012-07-31','2012-07-31 00:00:00',NULL,NULL,NULL,NULL,1,'2012-07-31 12:53:53','2012-07-31 12:54:51',1,1),(12,NULL,1,1,1,300,'XOF',0,'100000.000000','1000.000000','1.750000',2,'21.000000',0,1,10,1,1,1,10,1,'\0','\0','0.000000','2012-08-01 04:15:22','2012-08-02 04:15:36','2012-08-02',NULL,NULL,'2012-08-02','2012-10-11',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-02 04:15:22','2012-08-02 04:15:43',1,1),(13,NULL,2,5,4,300,'MMK',0,'50000.000000','0.000000','1.250000',2,'15.000000',1,1,24,2,12,2,2,1,'\0','\0','0.000000','2012-08-02 21:38:52','2012-08-02 21:39:56','2012-08-03',NULL,NULL,'2012-08-02','2014-08-02',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-03 21:38:52','2012-08-07 07:43:47',1,4),(14,NULL,2,4,3,300,'MMK',0,'50000.000000','0.000000','1.250000',2,'15.000000',1,1,52,1,1,1,52,1,'\0','\0','0.000000','2012-08-02 21:45:42','2012-08-02 21:46:03','2012-08-03',NULL,NULL,'2012-08-02','2013-08-01',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-03 21:45:42','2012-08-03 21:46:11',1,1),(15,NULL,2,4,3,300,'MMK',0,'50000.000000','0.000000','1.250000',2,'15.000000',1,1,104,1,1,1,104,1,'\0','\0','0.000000','2011-08-03 21:48:47','2011-08-03 21:49:06','2011-08-03',NULL,NULL,'2011-08-03','2013-07-31',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-03 21:48:47','2012-08-03 21:49:12',1,1),(16,NULL,2,4,3,300,'MMK',0,'50000.000000','0.000000','2.000000',2,'24.000000',0,1,16,1,1,1,16,1,'\0','\0','0.000000','2011-08-03 22:14:10','2011-08-03 22:14:21','2011-08-03',NULL,NULL,'2011-08-03','2011-11-23',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-03 22:14:10','2012-08-03 22:14:30',1,4),(17,NULL,2,4,3,300,'MMK',0,'50000.000000','0.000000','1.250000',2,'15.000000',0,1,104,1,1,1,104,1,'\0','\0','0.000000','2011-08-03 22:16:56','2011-08-03 22:17:04','2011-08-03',NULL,NULL,'2011-08-03','2013-07-31',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-03 22:16:56','2012-08-03 22:17:08',1,4),(18,NULL,2,4,3,300,'MMK',0,'50000.000000','0.000000','1.250000',2,'15.000000',1,1,104,1,1,1,104,1,'\0','\0','0.000000','2011-08-03 22:20:59','2011-08-03 22:21:06','2011-08-03',NULL,NULL,'2011-08-03','2013-07-31',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-03 22:20:59','2012-08-03 22:21:11',1,4),(19,NULL,2,4,3,300,'MMK',0,'50000.000000','0.000000','1.250000',2,'15.000000',1,1,104,1,1,1,104,1,'\0','\0','0.000000','2011-08-03 22:30:37','2011-08-03 22:31:22','2011-08-03',NULL,NULL,'2011-08-03','2013-07-31',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-03 22:30:37','2012-08-03 22:31:29',1,4),(20,NULL,4,1,1,100,'XOF',0,'100000.000000','1000.000000','22.370000',3,'22.370000',0,1,36,2,1,2,36,1,'\0','\0','0.000000','2012-08-02 12:59:57',NULL,'2012-08-17',NULL,NULL,NULL,'2015-08-17',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,1),(22,NULL,16,1,1,100,'XOF',0,'100000.000000','1000.000000','1.750000',3,'1.750000',0,1,12,2,1,2,12,1,'\0','\0','0.000000','2012-08-01 09:09:47',NULL,'2012-08-25',NULL,NULL,NULL,'2013-08-25',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,2),(23,NULL,2,2,NULL,100,'LBP',2,'35000.000000','10.000000','1.000000',2,'12.000000',1,1,20,2,1,2,10,1,'\0','\0','0.000000','2012-08-01 16:04:35',NULL,'2012-08-31','2012-10-30','2012-08-31',NULL,'2013-07-28',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,1),(24,NULL,2,3,2,100,'BND',2,'5000.000000','10.000000','1.500000',2,'18.000000',1,1,6,2,1,2,6,1,'\0','\0','0.000000','2012-08-31 16:06:04',NULL,'2012-08-31',NULL,NULL,NULL,'2013-02-28',NULL,NULL,NULL,NULL,NULL,NULL,1,'2012-08-31 16:06:04','2012-08-31 16:06:04',1,1);
/*!40000 ALTER TABLE `m_loan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_loan_charge`
--

DROP TABLE IF EXISTS `m_loan_charge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_loan_charge` (
  `loan_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  `charge_time_enum` smallint(5) NOT NULL,
  `charge_calculation_enum` smallint(5) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  PRIMARY KEY (`loan_id`,`charge_id`),
  KEY `charge_id` (`charge_id`),
  CONSTRAINT `m_loan_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_loan_charge_ibfk_2` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_loan_charge`
--

LOCK TABLES `m_loan_charge` WRITE;
/*!40000 ALTER TABLE `m_loan_charge` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_loan_charge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_loan_repayment_schedule`
--

DROP TABLE IF EXISTS `m_loan_repayment_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_loan_repayment_schedule` (
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
  `interest_waived_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  PRIMARY KEY (`id`),
  KEY `FK488B92AA40BE0710` (`loan_id`),
  CONSTRAINT `FK488B92AA40BE0710` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=690 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_loan_repayment_schedule`
--

LOCK TABLES `m_loan_repayment_schedule` WRITE;
/*!40000 ALTER TABLE `m_loan_repayment_schedule` DISABLE KEYS */;
INSERT INTO `m_loan_repayment_schedule` VALUES (1,1,'2012-07-24',1,'8150.000000','0.000000','404.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(2,1,'2012-07-31',2,'8183.000000','0.000000','371.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(3,1,'2012-08-07',3,'8216.000000','0.000000','338.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(4,1,'2012-08-14',4,'8249.000000','0.000000','305.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(5,1,'2012-08-21',5,'8283.000000','0.000000','271.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(6,1,'2012-08-28',6,'8316.000000','0.000000','238.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(7,1,'2012-09-04',7,'8350.000000','0.000000','204.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(8,1,'2012-09-11',8,'8383.000000','0.000000','171.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(9,1,'2012-09-18',9,'8417.000000','0.000000','137.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(10,1,'2012-09-25',10,'8451.000000','0.000000','103.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(11,1,'2012-10-02',11,'8485.000000','0.000000','69.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(12,1,'2012-10-09',12,'8517.000000','0.000000','33.000000','0.000000','\0',1,'2012-07-17 01:09:52','2012-07-17 01:09:52',1,'0.000000'),(13,2,'2012-07-31',1,'3427.920000','3427.920000','161.540000','161.540000','',1,'2012-07-17 03:39:58','2012-07-20 10:37:14',1,'0.000000'),(14,2,'2012-08-14',2,'3443.740000','0.000000','145.720000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(15,2,'2012-08-28',3,'3459.640000','0.000000','129.820000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(16,2,'2012-09-11',4,'3475.600000','0.000000','113.860000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(17,2,'2012-09-25',5,'3491.650000','0.000000','97.810000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(18,2,'2012-10-09',6,'3507.760000','0.000000','81.700000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(19,2,'2012-10-23',7,'3523.950000','0.000000','65.510000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(20,2,'2012-11-06',8,'3540.220000','0.000000','49.240000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(21,2,'2012-11-20',9,'3556.550000','0.000000','32.910000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(22,2,'2012-12-04',10,'3572.970000','0.000000','16.490000','0.000000','\0',1,'2012-07-17 03:39:58','2012-07-17 03:39:58',1,'0.000000'),(23,3,'2012-05-18',1,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(24,3,'2012-06-18',2,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(25,3,'2012-07-18',3,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(26,3,'2012-08-18',4,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(27,3,'2012-09-18',5,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(28,3,'2012-10-18',6,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(29,3,'2012-11-18',7,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(30,3,'2012-12-18',8,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(31,3,'2013-01-18',9,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(32,3,'2013-02-18',10,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(33,3,'2013-03-18',11,'58333.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(34,3,'2013-04-18',12,'58337.000000','0.000000','14000.000000','0.000000','\0',1,'2012-07-19 16:14:31','2012-07-19 16:14:31',1,'0.000000'),(35,4,'2012-05-18',1,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(36,4,'2012-06-18',2,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(37,4,'2012-07-18',3,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(38,4,'2012-08-18',4,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(39,4,'2012-09-18',5,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(40,4,'2012-10-18',6,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(41,4,'2012-11-18',7,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(42,4,'2012-12-18',8,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(43,4,'2013-01-18',9,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(44,4,'2013-02-18',10,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(45,4,'2013-03-18',11,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(46,4,'2013-04-18',12,'58337.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:07:53','2012-07-19 17:07:53',1,'0.000000'),(47,5,'2012-05-18',1,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(48,5,'2012-06-18',2,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(49,5,'2012-07-18',3,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(50,5,'2012-08-18',4,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(51,5,'2012-09-18',5,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(52,5,'2012-10-18',6,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(53,5,'2012-11-18',7,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(54,5,'2012-12-18',8,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(55,5,'2013-01-18',9,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(56,5,'2013-02-18',10,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(57,5,'2013-03-18',11,'58333.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(58,5,'2013-04-18',12,'58337.000000','0.000000','8750.000000','0.000000','\0',1,'2012-07-19 17:54:17','2012-07-19 17:54:17',1,'0.000000'),(59,6,'2014-07-20',1,'100000.000000','0.000000','42000.000000','0.000000','\0',1,'2012-07-20 04:54:55','2012-07-20 04:54:55',1,'0.000000'),(62,9,'2013-07-22',1,'23256.000000','0.000000','7500.000000','0.000000','\0',1,'2012-07-22 09:09:24','2012-07-22 09:09:24',1,'0.000000'),(63,9,'2014-07-22',2,'26744.000000','0.000000','4012.000000','0.000000','\0',1,'2012-07-22 09:09:24','2012-07-22 09:09:24',1,'0.000000'),(64,10,'2012-08-23',1,'9238.000000','0.000000','1750.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(65,10,'2012-09-23',2,'9400.000000','0.000000','1588.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(66,10,'2012-10-23',3,'9564.000000','0.000000','1424.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(67,10,'2012-11-23',4,'9732.000000','0.000000','1256.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(68,10,'2012-12-23',5,'9902.000000','0.000000','1086.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(69,10,'2013-01-23',6,'10075.000000','0.000000','913.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(70,10,'2013-02-23',7,'10251.000000','0.000000','737.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(71,10,'2013-03-23',8,'10431.000000','0.000000','557.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(72,10,'2013-04-23',9,'10613.000000','0.000000','375.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(73,10,'2013-05-23',10,'10794.000000','0.000000','189.000000','0.000000','\0',5,'2012-07-23 04:51:37','2012-07-23 04:51:37',5,'0.000000'),(74,11,'2012-08-04',1,'100000.000000','100000.000000','21000.000000','21000.000000','',1,'2012-07-31 12:53:53','2012-07-31 12:54:51',1,'0.000000'),(75,12,'2012-08-09',1,'9819.000000','9819.000000','404.000000','404.000000','',1,'2012-08-02 04:15:22','2012-08-02 04:15:56',1,'0.000000'),(76,12,'2012-08-16',2,'9859.000000','0.000000','364.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(77,12,'2012-08-23',3,'9899.000000','0.000000','324.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(78,12,'2012-08-30',4,'9939.000000','0.000000','284.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(79,12,'2012-09-06',5,'9979.000000','0.000000','244.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(80,12,'2012-09-13',6,'10019.000000','0.000000','204.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(81,12,'2012-09-20',7,'10059.000000','0.000000','164.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(82,12,'2012-09-27',8,'10100.000000','0.000000','123.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(83,12,'2012-10-04',9,'10141.000000','0.000000','82.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(84,12,'2012-10-11',10,'10186.000000','0.000000','42.000000','0.000000','\0',1,'2012-08-02 04:15:22','2012-08-02 04:15:22',1,'0.000000'),(87,13,'2013-08-02',1,'25000.000000','2500.000000','7500.000000','7500.000000','\0',1,'2012-08-03 21:40:08','2012-08-07 07:43:47',1,'0.000000'),(88,13,'2014-08-02',2,'25000.000000','0.000000','7500.000000','0.000000','\0',1,'2012-08-03 21:40:08','2012-08-07 07:43:47',1,'0.000000'),(141,14,'2012-08-09',1,'962.000000','156.000000','144.000000','144.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:47:27',1,'0.000000'),(142,14,'2012-08-16',2,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(143,14,'2012-08-23',3,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(144,14,'2012-08-30',4,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(145,14,'2012-09-06',5,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(146,14,'2012-09-13',6,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(147,14,'2012-09-20',7,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(148,14,'2012-09-27',8,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(149,14,'2012-10-04',9,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(150,14,'2012-10-11',10,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(151,14,'2012-10-18',11,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(152,14,'2012-10-25',12,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(153,14,'2012-11-01',13,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(154,14,'2012-11-08',14,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(155,14,'2012-11-15',15,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(156,14,'2012-11-22',16,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(157,14,'2012-11-29',17,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(158,14,'2012-12-06',18,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(159,14,'2012-12-13',19,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(160,14,'2012-12-20',20,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(161,14,'2012-12-27',21,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(162,14,'2013-01-03',22,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(163,14,'2013-01-10',23,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(164,14,'2013-01-17',24,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(165,14,'2013-01-24',25,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(166,14,'2013-01-31',26,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(167,14,'2013-02-07',27,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(168,14,'2013-02-14',28,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(169,14,'2013-02-21',29,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(170,14,'2013-02-28',30,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(171,14,'2013-03-07',31,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(172,14,'2013-03-14',32,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(173,14,'2013-03-21',33,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(174,14,'2013-03-28',34,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(175,14,'2013-04-04',35,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(176,14,'2013-04-11',36,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(177,14,'2013-04-18',37,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(178,14,'2013-04-25',38,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(179,14,'2013-05-02',39,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(180,14,'2013-05-09',40,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(181,14,'2013-05-16',41,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(182,14,'2013-05-23',42,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(183,14,'2013-05-30',43,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(184,14,'2013-06-06',44,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(185,14,'2013-06-13',45,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(186,14,'2013-06-20',46,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(187,14,'2013-06-27',47,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(188,14,'2013-07-04',48,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(189,14,'2013-07-11',49,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(190,14,'2013-07-18',50,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(191,14,'2013-07-25',51,'962.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(192,14,'2013-08-01',52,'938.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000'),(193,15,'2011-08-10',1,'481.000000','481.000000','144.000000','144.000000','',1,'2012-08-03 21:48:47','2012-08-03 21:49:35',1,'0.000000'),(194,15,'2011-08-17',2,'481.000000','481.000000','144.000000','144.000000','',1,'2012-08-03 21:48:47','2012-08-03 21:49:35',1,'0.000000'),(195,15,'2011-08-24',3,'481.000000','106.000000','144.000000','144.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:49:35',1,'0.000000'),(196,15,'2011-08-31',4,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(197,15,'2011-09-07',5,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(198,15,'2011-09-14',6,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(199,15,'2011-09-21',7,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(200,15,'2011-09-28',8,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(201,15,'2011-10-05',9,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(202,15,'2011-10-12',10,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(203,15,'2011-10-19',11,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(204,15,'2011-10-26',12,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(205,15,'2011-11-02',13,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(206,15,'2011-11-09',14,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(207,15,'2011-11-16',15,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(208,15,'2011-11-23',16,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(209,15,'2011-11-30',17,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(210,15,'2011-12-07',18,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(211,15,'2011-12-14',19,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(212,15,'2011-12-21',20,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(213,15,'2011-12-28',21,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(214,15,'2012-01-04',22,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(215,15,'2012-01-11',23,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(216,15,'2012-01-18',24,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(217,15,'2012-01-25',25,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(218,15,'2012-02-01',26,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(219,15,'2012-02-08',27,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(220,15,'2012-02-15',28,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(221,15,'2012-02-22',29,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(222,15,'2012-02-29',30,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(223,15,'2012-03-07',31,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(224,15,'2012-03-14',32,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(225,15,'2012-03-21',33,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(226,15,'2012-03-28',34,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(227,15,'2012-04-04',35,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(228,15,'2012-04-11',36,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(229,15,'2012-04-18',37,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(230,15,'2012-04-25',38,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(231,15,'2012-05-02',39,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(232,15,'2012-05-09',40,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(233,15,'2012-05-16',41,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(234,15,'2012-05-23',42,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(235,15,'2012-05-30',43,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(236,15,'2012-06-06',44,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(237,15,'2012-06-13',45,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(238,15,'2012-06-20',46,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(239,15,'2012-06-27',47,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(240,15,'2012-07-04',48,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(241,15,'2012-07-11',49,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(242,15,'2012-07-18',50,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(243,15,'2012-07-25',51,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(244,15,'2012-08-01',52,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(245,15,'2012-08-08',53,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(246,15,'2012-08-15',54,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(247,15,'2012-08-22',55,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(248,15,'2012-08-29',56,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(249,15,'2012-09-05',57,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(250,15,'2012-09-12',58,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(251,15,'2012-09-19',59,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(252,15,'2012-09-26',60,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(253,15,'2012-10-03',61,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(254,15,'2012-10-10',62,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(255,15,'2012-10-17',63,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(256,15,'2012-10-24',64,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(257,15,'2012-10-31',65,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(258,15,'2012-11-07',66,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(259,15,'2012-11-14',67,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(260,15,'2012-11-21',68,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(261,15,'2012-11-28',69,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(262,15,'2012-12-05',70,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(263,15,'2012-12-12',71,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(264,15,'2012-12-19',72,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(265,15,'2012-12-26',73,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(266,15,'2013-01-02',74,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(267,15,'2013-01-09',75,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(268,15,'2013-01-16',76,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(269,15,'2013-01-23',77,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(270,15,'2013-01-30',78,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(271,15,'2013-02-06',79,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(272,15,'2013-02-13',80,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(273,15,'2013-02-20',81,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(274,15,'2013-02-27',82,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(275,15,'2013-03-06',83,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(276,15,'2013-03-13',84,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(277,15,'2013-03-20',85,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(278,15,'2013-03-27',86,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(279,15,'2013-04-03',87,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(280,15,'2013-04-10',88,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(281,15,'2013-04-17',89,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(282,15,'2013-04-24',90,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(283,15,'2013-05-01',91,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(284,15,'2013-05-08',92,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(285,15,'2013-05-15',93,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(286,15,'2013-05-22',94,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(287,15,'2013-05-29',95,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(288,15,'2013-06-05',96,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(289,15,'2013-06-12',97,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(290,15,'2013-06-19',98,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(291,15,'2013-06-26',99,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(292,15,'2013-07-03',100,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(293,15,'2013-07-10',101,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(294,15,'2013-07-17',102,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(295,15,'2013-07-24',103,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(296,15,'2013-07-31',104,'457.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 21:48:47','2012-08-03 21:48:47',1,'0.000000'),(297,16,'2011-08-10',1,'3018.000000','449.000000','231.000000','231.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:15:14',1,'0.000000'),(298,16,'2011-08-17',2,'3032.000000','0.000000','217.000000','217.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:15:14',1,'0.000000'),(299,16,'2011-08-24',3,'3046.000000','0.000000','203.000000','203.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:15:14',1,'0.000000'),(300,16,'2011-08-31',4,'3060.000000','0.000000','189.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(301,16,'2011-09-07',5,'3074.000000','0.000000','175.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(302,16,'2011-09-14',6,'3089.000000','0.000000','160.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(303,16,'2011-09-21',7,'3103.000000','0.000000','146.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(304,16,'2011-09-28',8,'3117.000000','0.000000','132.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(305,16,'2011-10-05',9,'3131.000000','0.000000','118.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(306,16,'2011-10-12',10,'3146.000000','0.000000','103.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(307,16,'2011-10-19',11,'3160.000000','0.000000','89.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(308,16,'2011-10-26',12,'3175.000000','0.000000','74.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(309,16,'2011-11-02',13,'3190.000000','0.000000','59.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(310,16,'2011-11-09',14,'3204.000000','0.000000','45.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(311,16,'2011-11-16',15,'3219.000000','0.000000','30.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(312,16,'2011-11-23',16,'3236.000000','0.000000','13.000000','0.000000','\0',1,'2012-08-03 22:14:10','2012-08-03 22:14:10',1,'0.000000'),(313,17,'2011-08-10',1,'413.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:17:42',1,'0.000000'),(314,17,'2011-08-17',2,'414.000000','0.000000','143.000000','143.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:17:42',1,'0.000000'),(315,17,'2011-08-24',3,'415.000000','0.000000','142.000000','142.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:18:29',1,'0.000000'),(316,17,'2011-08-31',4,'416.000000','0.000000','141.000000','141.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:18:29',1,'0.000000'),(317,17,'2011-09-07',5,'418.000000','0.000000','139.000000','139.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:18:29',1,'0.000000'),(318,17,'2011-09-14',6,'419.000000','0.000000','138.000000','138.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:18:29',1,'0.000000'),(319,17,'2011-09-21',7,'420.000000','0.000000','137.000000','137.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:18:29',1,'0.000000'),(320,17,'2011-09-28',8,'421.000000','0.000000','136.000000','136.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:18:29',1,'0.000000'),(321,17,'2011-10-05',9,'422.000000','0.000000','135.000000','135.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:18:29',1,'0.000000'),(322,17,'2011-10-12',10,'424.000000','0.000000','133.000000','45.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:18:29',1,'0.000000'),(323,17,'2011-10-19',11,'425.000000','0.000000','132.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(324,17,'2011-10-26',12,'426.000000','0.000000','131.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(325,17,'2011-11-02',13,'427.000000','0.000000','130.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(326,17,'2011-11-09',14,'429.000000','0.000000','128.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(327,17,'2011-11-16',15,'430.000000','0.000000','127.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(328,17,'2011-11-23',16,'431.000000','0.000000','126.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(329,17,'2011-11-30',17,'432.000000','0.000000','125.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(330,17,'2011-12-07',18,'433.000000','0.000000','124.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(331,17,'2011-12-14',19,'435.000000','0.000000','122.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(332,17,'2011-12-21',20,'436.000000','0.000000','121.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(333,17,'2011-12-28',21,'437.000000','0.000000','120.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(334,17,'2012-01-04',22,'439.000000','0.000000','118.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(335,17,'2012-01-11',23,'440.000000','0.000000','117.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(336,17,'2012-01-18',24,'441.000000','0.000000','116.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(337,17,'2012-01-25',25,'442.000000','0.000000','115.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(338,17,'2012-02-01',26,'444.000000','0.000000','113.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(339,17,'2012-02-08',27,'445.000000','0.000000','112.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(340,17,'2012-02-15',28,'446.000000','0.000000','111.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(341,17,'2012-02-22',29,'447.000000','0.000000','110.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(342,17,'2012-02-29',30,'449.000000','0.000000','108.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(343,17,'2012-03-07',31,'450.000000','0.000000','107.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(344,17,'2012-03-14',32,'451.000000','0.000000','106.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(345,17,'2012-03-21',33,'453.000000','0.000000','104.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(346,17,'2012-03-28',34,'454.000000','0.000000','103.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(347,17,'2012-04-04',35,'455.000000','0.000000','102.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(348,17,'2012-04-11',36,'457.000000','0.000000','100.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(349,17,'2012-04-18',37,'458.000000','0.000000','99.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(350,17,'2012-04-25',38,'459.000000','0.000000','98.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(351,17,'2012-05-02',39,'461.000000','0.000000','96.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(352,17,'2012-05-09',40,'462.000000','0.000000','95.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(353,17,'2012-05-16',41,'463.000000','0.000000','94.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(354,17,'2012-05-23',42,'465.000000','0.000000','92.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(355,17,'2012-05-30',43,'466.000000','0.000000','91.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(356,17,'2012-06-06',44,'467.000000','0.000000','90.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(357,17,'2012-06-13',45,'469.000000','0.000000','88.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(358,17,'2012-06-20',46,'470.000000','0.000000','87.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(359,17,'2012-06-27',47,'471.000000','0.000000','86.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(360,17,'2012-07-04',48,'473.000000','0.000000','84.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(361,17,'2012-07-11',49,'474.000000','0.000000','83.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(362,17,'2012-07-18',50,'475.000000','0.000000','82.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(363,17,'2012-07-25',51,'477.000000','0.000000','80.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(364,17,'2012-08-01',52,'478.000000','0.000000','79.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(365,17,'2012-08-08',53,'479.000000','0.000000','78.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(366,17,'2012-08-15',54,'481.000000','0.000000','76.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(367,17,'2012-08-22',55,'482.000000','0.000000','75.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(368,17,'2012-08-29',56,'484.000000','0.000000','73.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(369,17,'2012-09-05',57,'485.000000','0.000000','72.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(370,17,'2012-09-12',58,'486.000000','0.000000','71.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(371,17,'2012-09-19',59,'488.000000','0.000000','69.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(372,17,'2012-09-26',60,'489.000000','0.000000','68.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(373,17,'2012-10-03',61,'491.000000','0.000000','66.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(374,17,'2012-10-10',62,'492.000000','0.000000','65.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(375,17,'2012-10-17',63,'493.000000','0.000000','64.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(376,17,'2012-10-24',64,'495.000000','0.000000','62.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(377,17,'2012-10-31',65,'496.000000','0.000000','61.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(378,17,'2012-11-07',66,'498.000000','0.000000','59.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(379,17,'2012-11-14',67,'499.000000','0.000000','58.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(380,17,'2012-11-21',68,'501.000000','0.000000','56.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(381,17,'2012-11-28',69,'502.000000','0.000000','55.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(382,17,'2012-12-05',70,'504.000000','0.000000','53.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(383,17,'2012-12-12',71,'505.000000','0.000000','52.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(384,17,'2012-12-19',72,'506.000000','0.000000','51.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(385,17,'2012-12-26',73,'508.000000','0.000000','49.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(386,17,'2013-01-02',74,'509.000000','0.000000','48.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(387,17,'2013-01-09',75,'511.000000','0.000000','46.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(388,17,'2013-01-16',76,'512.000000','0.000000','45.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(389,17,'2013-01-23',77,'514.000000','0.000000','43.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(390,17,'2013-01-30',78,'515.000000','0.000000','42.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(391,17,'2013-02-06',79,'517.000000','0.000000','40.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(392,17,'2013-02-13',80,'518.000000','0.000000','39.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(393,17,'2013-02-20',81,'520.000000','0.000000','37.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(394,17,'2013-02-27',82,'521.000000','0.000000','36.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(395,17,'2013-03-06',83,'523.000000','0.000000','34.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(396,17,'2013-03-13',84,'524.000000','0.000000','33.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(397,17,'2013-03-20',85,'526.000000','0.000000','31.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(398,17,'2013-03-27',86,'527.000000','0.000000','30.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(399,17,'2013-04-03',87,'529.000000','0.000000','28.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(400,17,'2013-04-10',88,'530.000000','0.000000','27.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(401,17,'2013-04-17',89,'532.000000','0.000000','25.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(402,17,'2013-04-24',90,'533.000000','0.000000','24.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(403,17,'2013-05-01',91,'535.000000','0.000000','22.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(404,17,'2013-05-08',92,'536.000000','0.000000','21.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(405,17,'2013-05-15',93,'538.000000','0.000000','19.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(406,17,'2013-05-22',94,'540.000000','0.000000','17.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(407,17,'2013-05-29',95,'541.000000','0.000000','16.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(408,17,'2013-06-05',96,'543.000000','0.000000','14.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(409,17,'2013-06-12',97,'544.000000','0.000000','13.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(410,17,'2013-06-19',98,'546.000000','0.000000','11.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(411,17,'2013-06-26',99,'547.000000','0.000000','10.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(412,17,'2013-07-03',100,'549.000000','0.000000','8.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(413,17,'2013-07-10',101,'551.000000','0.000000','6.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(414,17,'2013-07-17',102,'552.000000','0.000000','5.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(415,17,'2013-07-24',103,'554.000000','0.000000','3.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(416,17,'2013-07-31',104,'575.000000','0.000000','0.000000','0.000000','\0',1,'2012-08-03 22:16:56','2012-08-03 22:16:56',1,'0.000000'),(417,18,'2011-08-10',1,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(418,18,'2011-08-17',2,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(419,18,'2011-08-24',3,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(420,18,'2011-08-31',4,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(421,18,'2011-09-07',5,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(422,18,'2011-09-14',6,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(423,18,'2011-09-21',7,'481.000000','0.000000','144.000000','136.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(424,18,'2011-09-28',8,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(425,18,'2011-10-05',9,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(426,18,'2011-10-12',10,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(427,18,'2011-10-19',11,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(428,18,'2011-10-26',12,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(429,18,'2011-11-02',13,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(430,18,'2011-11-09',14,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(431,18,'2011-11-16',15,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(432,18,'2011-11-23',16,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(433,18,'2011-11-30',17,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(434,18,'2011-12-07',18,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(435,18,'2011-12-14',19,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(436,18,'2011-12-21',20,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(437,18,'2011-12-28',21,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(438,18,'2012-01-04',22,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(439,18,'2012-01-11',23,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(440,18,'2012-01-18',24,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(441,18,'2012-01-25',25,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(442,18,'2012-02-01',26,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(443,18,'2012-02-08',27,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(444,18,'2012-02-15',28,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(445,18,'2012-02-22',29,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(446,18,'2012-02-29',30,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(447,18,'2012-03-07',31,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(448,18,'2012-03-14',32,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(449,18,'2012-03-21',33,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(450,18,'2012-03-28',34,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(451,18,'2012-04-04',35,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(452,18,'2012-04-11',36,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(453,18,'2012-04-18',37,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(454,18,'2012-04-25',38,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(455,18,'2012-05-02',39,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(456,18,'2012-05-09',40,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(457,18,'2012-05-16',41,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(458,18,'2012-05-23',42,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(459,18,'2012-05-30',43,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(460,18,'2012-06-06',44,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(461,18,'2012-06-13',45,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(462,18,'2012-06-20',46,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(463,18,'2012-06-27',47,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(464,18,'2012-07-04',48,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(465,18,'2012-07-11',49,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(466,18,'2012-07-18',50,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(467,18,'2012-07-25',51,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(468,18,'2012-08-01',52,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(469,18,'2012-08-08',53,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:11:48',1,'0.000000'),(470,18,'2012-08-15',54,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(471,18,'2012-08-22',55,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(472,18,'2012-08-29',56,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(473,18,'2012-09-05',57,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(474,18,'2012-09-12',58,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(475,18,'2012-09-19',59,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(476,18,'2012-09-26',60,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(477,18,'2012-10-03',61,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(478,18,'2012-10-10',62,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(479,18,'2012-10-17',63,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(480,18,'2012-10-24',64,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(481,18,'2012-10-31',65,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(482,18,'2012-11-07',66,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(483,18,'2012-11-14',67,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(484,18,'2012-11-21',68,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(485,18,'2012-11-28',69,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(486,18,'2012-12-05',70,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(487,18,'2012-12-12',71,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(488,18,'2012-12-19',72,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(489,18,'2012-12-26',73,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(490,18,'2013-01-02',74,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(491,18,'2013-01-09',75,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(492,18,'2013-01-16',76,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(493,18,'2013-01-23',77,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(494,18,'2013-01-30',78,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(495,18,'2013-02-06',79,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(496,18,'2013-02-13',80,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(497,18,'2013-02-20',81,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(498,18,'2013-02-27',82,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(499,18,'2013-03-06',83,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(500,18,'2013-03-13',84,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(501,18,'2013-03-20',85,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(502,18,'2013-03-27',86,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(503,18,'2013-04-03',87,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(504,18,'2013-04-10',88,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(505,18,'2013-04-17',89,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-07 08:10:31',1,'0.000000'),(506,18,'2013-04-24',90,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(507,18,'2013-05-01',91,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(508,18,'2013-05-08',92,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(509,18,'2013-05-15',93,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(510,18,'2013-05-22',94,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(511,18,'2013-05-29',95,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(512,18,'2013-06-05',96,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(513,18,'2013-06-12',97,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(514,18,'2013-06-19',98,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(515,18,'2013-06-26',99,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(516,18,'2013-07-03',100,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(517,18,'2013-07-10',101,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(518,18,'2013-07-17',102,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(519,18,'2013-07-24',103,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(520,18,'2013-07-31',104,'457.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:20:59','2012-08-03 22:20:59',1,'0.000000'),(521,19,'2011-08-10',1,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(522,19,'2011-08-17',2,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(523,19,'2011-08-24',3,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(524,19,'2011-08-31',4,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(525,19,'2011-09-07',5,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(526,19,'2011-09-14',6,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(527,19,'2011-09-21',7,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(528,19,'2011-09-28',8,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(529,19,'2011-10-05',9,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(530,19,'2011-10-12',10,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(531,19,'2011-10-19',11,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(532,19,'2011-10-26',12,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(533,19,'2011-11-02',13,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(534,19,'2011-11-09',14,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(535,19,'2011-11-16',15,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(536,19,'2011-11-23',16,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(537,19,'2011-11-30',17,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(538,19,'2011-12-07',18,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(539,19,'2011-12-14',19,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(540,19,'2011-12-21',20,'481.000000','0.000000','144.000000','144.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(541,19,'2011-12-28',21,'481.000000','0.000000','144.000000','120.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:31:46',1,'0.000000'),(542,19,'2012-01-04',22,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(543,19,'2012-01-11',23,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(544,19,'2012-01-18',24,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(545,19,'2012-01-25',25,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(546,19,'2012-02-01',26,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(547,19,'2012-02-08',27,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(548,19,'2012-02-15',28,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(549,19,'2012-02-22',29,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(550,19,'2012-02-29',30,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(551,19,'2012-03-07',31,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(552,19,'2012-03-14',32,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(553,19,'2012-03-21',33,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(554,19,'2012-03-28',34,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(555,19,'2012-04-04',35,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(556,19,'2012-04-11',36,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(557,19,'2012-04-18',37,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(558,19,'2012-04-25',38,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(559,19,'2012-05-02',39,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(560,19,'2012-05-09',40,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(561,19,'2012-05-16',41,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(562,19,'2012-05-23',42,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(563,19,'2012-05-30',43,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(564,19,'2012-06-06',44,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(565,19,'2012-06-13',45,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(566,19,'2012-06-20',46,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(567,19,'2012-06-27',47,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(568,19,'2012-07-04',48,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(569,19,'2012-07-11',49,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(570,19,'2012-07-18',50,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(571,19,'2012-07-25',51,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(572,19,'2012-08-01',52,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(573,19,'2012-08-08',53,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(574,19,'2012-08-15',54,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(575,19,'2012-08-22',55,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(576,19,'2012-08-29',56,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(577,19,'2012-09-05',57,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(578,19,'2012-09-12',58,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(579,19,'2012-09-19',59,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(580,19,'2012-09-26',60,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(581,19,'2012-10-03',61,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(582,19,'2012-10-10',62,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(583,19,'2012-10-17',63,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(584,19,'2012-10-24',64,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(585,19,'2012-10-31',65,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(586,19,'2012-11-07',66,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(587,19,'2012-11-14',67,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(588,19,'2012-11-21',68,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(589,19,'2012-11-28',69,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(590,19,'2012-12-05',70,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(591,19,'2012-12-12',71,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(592,19,'2012-12-19',72,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(593,19,'2012-12-26',73,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(594,19,'2013-01-02',74,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(595,19,'2013-01-09',75,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(596,19,'2013-01-16',76,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(597,19,'2013-01-23',77,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(598,19,'2013-01-30',78,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(599,19,'2013-02-06',79,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(600,19,'2013-02-13',80,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(601,19,'2013-02-20',81,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(602,19,'2013-02-27',82,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(603,19,'2013-03-06',83,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(604,19,'2013-03-13',84,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(605,19,'2013-03-20',85,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(606,19,'2013-03-27',86,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(607,19,'2013-04-03',87,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(608,19,'2013-04-10',88,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(609,19,'2013-04-17',89,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(610,19,'2013-04-24',90,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(611,19,'2013-05-01',91,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(612,19,'2013-05-08',92,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(613,19,'2013-05-15',93,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(614,19,'2013-05-22',94,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(615,19,'2013-05-29',95,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(616,19,'2013-06-05',96,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(617,19,'2013-06-12',97,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(618,19,'2013-06-19',98,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(619,19,'2013-06-26',99,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(620,19,'2013-07-03',100,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(621,19,'2013-07-10',101,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(622,19,'2013-07-17',102,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(623,19,'2013-07-24',103,'481.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(624,19,'2013-07-31',104,'457.000000','0.000000','144.000000','0.000000','\0',1,'2012-08-03 22:30:37','2012-08-03 22:30:37',1,'0.000000'),(625,20,'2012-09-17',1,'1974.000000','0.000000','1864.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(626,20,'2012-10-17',2,'2011.000000','0.000000','1827.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(627,20,'2012-11-17',3,'2048.000000','0.000000','1790.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(628,20,'2012-12-17',4,'2086.000000','0.000000','1752.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(629,20,'2013-01-17',5,'2125.000000','0.000000','1713.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(630,20,'2013-02-17',6,'2165.000000','0.000000','1673.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(631,20,'2013-03-17',7,'2205.000000','0.000000','1633.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(632,20,'2013-04-17',8,'2246.000000','0.000000','1592.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(633,20,'2013-05-17',9,'2288.000000','0.000000','1550.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(634,20,'2013-06-17',10,'2331.000000','0.000000','1507.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(635,20,'2013-07-17',11,'2374.000000','0.000000','1464.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(636,20,'2013-08-17',12,'2418.000000','0.000000','1420.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(637,20,'2013-09-17',13,'2464.000000','0.000000','1374.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(638,20,'2013-10-17',14,'2510.000000','0.000000','1328.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(639,20,'2013-11-17',15,'2556.000000','0.000000','1282.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(640,20,'2013-12-17',16,'2604.000000','0.000000','1234.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(641,20,'2014-01-17',17,'2652.000000','0.000000','1186.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(642,20,'2014-02-17',18,'2702.000000','0.000000','1136.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(643,20,'2014-03-17',19,'2752.000000','0.000000','1086.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(644,20,'2014-04-17',20,'2804.000000','0.000000','1034.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(645,20,'2014-05-17',21,'2856.000000','0.000000','982.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(646,20,'2014-06-17',22,'2909.000000','0.000000','929.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(647,20,'2014-07-17',23,'2963.000000','0.000000','875.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(648,20,'2014-08-17',24,'3019.000000','0.000000','819.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(649,20,'2014-09-17',25,'3075.000000','0.000000','763.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(650,20,'2014-10-17',26,'3132.000000','0.000000','706.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(651,20,'2014-11-17',27,'3191.000000','0.000000','647.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(652,20,'2014-12-17',28,'3250.000000','0.000000','588.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(653,20,'2015-01-17',29,'3311.000000','0.000000','527.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(654,20,'2015-02-17',30,'3372.000000','0.000000','466.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(655,20,'2015-03-17',31,'3435.000000','0.000000','403.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(656,20,'2015-04-17',32,'3499.000000','0.000000','339.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(657,20,'2015-05-17',33,'3564.000000','0.000000','274.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(658,20,'2015-06-17',34,'3631.000000','0.000000','207.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(659,20,'2015-07-17',35,'3699.000000','0.000000','139.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(660,20,'2015-08-17',36,'3779.000000','0.000000','67.000000','0.000000','\0',1,'2012-08-17 12:59:57','2012-08-17 12:59:57',1,'0.000000'),(662,22,'2012-09-25',1,'8267.000000','0.000000','146.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(663,22,'2012-10-25',2,'8279.000000','0.000000','134.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(664,22,'2012-11-25',3,'8291.000000','0.000000','122.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(665,22,'2012-12-25',4,'8303.000000','0.000000','110.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(666,22,'2013-01-25',5,'8315.000000','0.000000','98.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(667,22,'2013-02-25',6,'8328.000000','0.000000','85.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(668,22,'2013-03-25',7,'8340.000000','0.000000','73.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(669,22,'2013-04-25',8,'8352.000000','0.000000','61.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(670,22,'2013-05-25',9,'8364.000000','0.000000','49.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(671,22,'2013-06-25',10,'8376.000000','0.000000','37.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(672,22,'2013-07-25',11,'8389.000000','0.000000','24.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(673,22,'2013-08-25',12,'8396.000000','0.000000','11.000000','0.000000','\0',1,'2012-08-25 09:09:47','2012-08-25 09:09:47',1,'0.000000'),(674,23,'2012-10-30',1,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(675,23,'2012-11-30',2,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(676,23,'2012-12-30',3,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(677,23,'2013-01-30',4,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(678,23,'2013-02-28',5,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(679,23,'2013-03-28',6,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(680,23,'2013-04-28',7,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(681,23,'2013-05-28',8,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(682,23,'2013-06-28',9,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(683,23,'2013-07-28',10,'3500.000000','0.000000','700.000000','0.000000','\0',1,'2012-08-31 16:04:35','2012-08-31 16:04:35',1,'0.000000'),(684,24,'2012-09-30',1,'833.330000','0.000000','75.000000','0.000000','\0',1,'2012-08-31 16:06:04','2012-08-31 16:06:04',1,'0.000000'),(685,24,'2012-10-30',2,'833.330000','0.000000','75.000000','0.000000','\0',1,'2012-08-31 16:06:04','2012-08-31 16:06:04',1,'0.000000'),(686,24,'2012-11-30',3,'833.330000','0.000000','75.000000','0.000000','\0',1,'2012-08-31 16:06:04','2012-08-31 16:06:04',1,'0.000000'),(687,24,'2012-12-30',4,'833.330000','0.000000','75.000000','0.000000','\0',1,'2012-08-31 16:06:04','2012-08-31 16:06:04',1,'0.000000'),(688,24,'2013-01-30',5,'833.330000','0.000000','75.000000','0.000000','\0',1,'2012-08-31 16:06:04','2012-08-31 16:06:04',1,'0.000000'),(689,24,'2013-02-28',6,'833.350000','0.000000','75.000000','0.000000','\0',1,'2012-08-31 16:06:04','2012-08-31 16:06:04',1,'0.000000');
/*!40000 ALTER TABLE `m_loan_repayment_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_loan_transaction`
--

DROP TABLE IF EXISTS `m_loan_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_loan_transaction` (
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
  `principal_portion_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  `interest_portion_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  `interest_waived_derived` decimal(19,6) NOT NULL DEFAULT '0.000000',
  PRIMARY KEY (`id`),
  KEY `FKCFCEA42640BE0710` (`loan_id`),
  KEY `FKCFCEA426FC69F3F1` (`contra_id`),
  CONSTRAINT `FKCFCEA42640BE0710` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FKCFCEA426FC69F3F1` FOREIGN KEY (`contra_id`) REFERENCES `m_loan_transaction` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_loan_transaction`
--

LOCK TABLES `m_loan_transaction` WRITE;
/*!40000 ALTER TABLE `m_loan_transaction` DISABLE KEYS */;
INSERT INTO `m_loan_transaction` VALUES (3,5,1,NULL,'2012-04-17','700000.000000',1,'2012-07-19 17:55:54','2012-07-19 17:55:54',1,'0.000000','0.000000','0.000000'),(4,2,1,NULL,'2012-07-17','35000.000000',1,'2012-07-20 08:56:30','2012-07-20 08:56:30',1,'0.000000','0.000000','0.000000'),(5,2,2,NULL,'2012-07-19','3589.460000',1,'2012-07-20 10:37:14','2012-07-20 10:37:14',1,'0.000000','0.000000','0.000000'),(6,10,1,NULL,'2012-07-23','100000.000000',5,'2012-07-23 04:52:24','2012-07-23 04:52:24',5,'0.000000','0.000000','0.000000'),(7,11,1,NULL,'2012-07-31','100000.000000',1,'2012-07-31 12:54:24','2012-07-31 12:54:24',1,'0.000000','0.000000','0.000000'),(8,11,2,NULL,'2012-07-31','121000.000000',1,'2012-07-31 12:54:51','2012-07-31 12:54:51',1,'100000.000000','21000.000000','0.000000'),(9,12,1,NULL,'2012-08-02','100000.000000',1,'2012-08-02 04:15:43','2012-08-02 04:15:43',1,'0.000000','0.000000','0.000000'),(10,12,2,NULL,'2012-08-02','10223.000000',1,'2012-08-02 04:15:56','2012-08-02 04:15:56',1,'9819.000000','404.000000','0.000000'),(11,13,1,NULL,'2012-08-02','50000.000000',1,'2012-08-03 21:40:08','2012-08-03 21:40:08',1,'0.000000','0.000000','0.000000'),(12,13,2,32,'2012-08-02','10000.000000',1,'2012-08-03 21:40:38','2012-08-07 07:41:41',1,'2500.000000','7500.000000','0.000000'),(13,14,1,NULL,'2012-08-02','50000.000000',1,'2012-08-03 21:46:11','2012-08-03 21:46:11',1,'0.000000','0.000000','0.000000'),(14,14,2,NULL,'2012-08-03','300.000000',1,'2012-08-03 21:47:27','2012-08-03 21:47:27',1,'156.000000','144.000000','0.000000'),(15,15,1,NULL,'2011-08-03','50000.000000',1,'2012-08-03 21:49:12','2012-08-03 21:49:12',1,'0.000000','0.000000','0.000000'),(16,15,2,NULL,'2012-08-02','1500.000000',1,'2012-08-03 21:49:35','2012-08-03 21:49:35',1,'1068.000000','432.000000','0.000000'),(17,16,1,NULL,'2011-08-03','50000.000000',1,'2012-08-03 22:14:30','2012-08-03 22:14:30',1,'0.000000','0.000000','0.000000'),(18,16,2,20,'2011-08-24','400.000000',1,'2012-08-03 22:15:01','2012-08-03 22:15:14',1,'0.000000','400.000000','0.000000'),(19,16,2,NULL,'2011-08-24','1100.000000',1,'2012-08-03 22:15:14','2012-08-03 22:15:14',1,'449.000000','651.000000','0.000000'),(20,16,3,18,'2011-08-24','-400.000000',1,'2012-08-03 22:15:14','2012-08-03 22:15:14',1,'0.000000','0.000000','0.000000'),(21,17,1,NULL,'2011-08-03','50000.000000',1,'2012-08-03 22:17:08','2012-08-03 22:17:08',1,'0.000000','0.000000','0.000000'),(22,17,2,24,'2012-08-03','400.000000',1,'2012-08-03 22:17:42','2012-08-03 22:18:29',1,'0.000000','400.000000','0.000000'),(23,17,2,NULL,'2012-08-03','1300.000000',1,'2012-08-03 22:18:29','2012-08-03 22:18:29',1,'0.000000','1300.000000','0.000000'),(24,17,3,22,'2012-08-03','-400.000000',1,'2012-08-03 22:18:29','2012-08-03 22:18:29',1,'0.000000','0.000000','0.000000'),(25,18,1,NULL,'2011-08-03','50000.000000',1,'2012-08-03 22:21:11','2012-08-03 22:21:11',1,'0.000000','0.000000','0.000000'),(26,18,2,28,'2012-08-03','600.000000',1,'2012-08-03 22:21:29','2012-08-03 22:21:43',1,'0.000000','600.000000','0.000000'),(27,18,2,40,'2012-08-03','1100.000000',1,'2012-08-03 22:21:42','2012-08-07 08:08:44',1,'0.000000','1100.000000','0.000000'),(28,18,3,26,'2012-08-03','-600.000000',1,'2012-08-03 22:21:42','2012-08-03 22:21:42',1,'0.000000','0.000000','0.000000'),(29,19,1,NULL,'2011-08-03','50000.000000',1,'2012-08-03 22:31:29','2012-08-03 22:31:29',1,'0.000000','0.000000','0.000000'),(30,19,2,NULL,'2012-08-02','3000.000000',1,'2012-08-03 22:31:46','2012-08-03 22:31:46',1,'0.000000','3000.000000','0.000000'),(31,13,2,34,'2012-08-02','20000.000000',1,'2012-08-07 07:41:41','2012-08-07 07:42:04',1,'12500.000000','7500.000000','0.000000'),(32,13,3,12,'2012-08-02','-10000.000000',1,'2012-08-07 07:41:41','2012-08-07 07:41:41',1,'0.000000','0.000000','0.000000'),(33,13,2,36,'2012-08-02','40000.000000',1,'2012-08-07 07:42:04','2012-08-07 07:43:06',1,'25000.000000','15000.000000','0.000000'),(34,13,3,31,'2012-08-02','-20000.000000',1,'2012-08-07 07:42:04','2012-08-07 07:42:04',1,'0.000000','0.000000','0.000000'),(35,13,2,38,'2012-08-02','70000.000000',1,'2012-08-07 07:43:06','2012-08-07 07:43:47',1,'50000.000000','15000.000000','0.000000'),(36,13,3,33,'2012-08-02','-40000.000000',1,'2012-08-07 07:43:06','2012-08-07 07:43:06',1,'0.000000','0.000000','0.000000'),(37,13,2,NULL,'2012-08-02','10000.000000',1,'2012-08-07 07:43:47','2012-08-07 07:43:47',1,'2500.000000','7500.000000','0.000000'),(38,13,3,35,'2012-08-02','-70000.000000',1,'2012-08-07 07:43:47','2012-08-07 07:43:47',1,'0.000000','0.000000','0.000000'),(39,18,2,42,'2012-08-03','50000.000000',1,'2012-08-07 08:08:44','2012-08-07 08:10:31',1,'42368.000000','7632.000000','0.000000'),(40,18,3,27,'2012-08-03','-1100.000000',1,'2012-08-07 08:08:44','2012-08-07 08:08:44',1,'0.000000','0.000000','0.000000'),(41,18,2,44,'2012-08-03','20000.000000',1,'2012-08-07 08:10:31','2012-08-07 08:11:15',1,'12368.000000','7632.000000','0.000000'),(42,18,3,39,'2012-08-03','-50000.000000',1,'2012-08-07 08:10:31','2012-08-07 08:10:31',1,'0.000000','0.000000','0.000000'),(43,18,2,46,'2012-08-03','11000.000000',1,'2012-08-07 08:11:15','2012-08-07 08:11:48',1,'3368.000000','7632.000000','0.000000'),(44,18,3,41,'2012-08-03','-20000.000000',1,'2012-08-07 08:11:15','2012-08-07 08:11:15',1,'0.000000','0.000000','0.000000'),(45,18,2,NULL,'2012-08-03','1000.000000',1,'2012-08-07 08:11:48','2012-08-07 08:11:48',1,'0.000000','1000.000000','0.000000'),(46,18,3,43,'2012-08-03','-11000.000000',1,'2012-08-07 08:11:48','2012-08-07 08:11:48',1,'0.000000','0.000000','0.000000');
/*!40000 ALTER TABLE `m_loan_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_loan_xAdditional Information`
--

DROP TABLE IF EXISTS `m_loan_xAdditional Information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_loan_xAdditional Information` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Business Location` varchar(50) DEFAULT NULL,
  `Business Location Other` varchar(50) DEFAULT NULL,
  `Business` varchar(10) DEFAULT NULL,
  `Business Description` mediumtext,
  `Business Title` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `portfolio_loan_extra_Additional Information_fk1` FOREIGN KEY (`id`) REFERENCES `m_loan` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_loan_xAdditional Information`
--

LOCK TABLES `m_loan_xAdditional Information` WRITE;
/*!40000 ALTER TABLE `m_loan_xAdditional Information` DISABLE KEYS */;
INSERT INTO `m_loan_xAdditional Information` VALUES (1,'East Sikkim',NULL,'New',NULL,NULL);
/*!40000 ALTER TABLE `m_loan_xAdditional Information` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_note`
--

DROP TABLE IF EXISTS `m_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_note` (
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
  CONSTRAINT `FK7C9708924D26803` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  CONSTRAINT `FK7C9708940BE0710` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK7C97089541F0A56` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK7C970897179A0CB` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK7C970898F889C3F` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_note`
--

LOCK TABLES `m_note` WRITE;
/*!40000 ALTER TABLE `m_note` DISABLE KEYS */;
INSERT INTO `m_note` VALUES (1,8,NULL,NULL,100,'Just a quick\r\nnote','2012-07-17 01:22:13',1,'2012-07-17 01:22:13',1),(2,1,NULL,NULL,100,'First note\r\n\r\nedited','2012-07-17 05:15:05',1,'2012-07-17 05:15:17',1),(3,11,NULL,NULL,100,'one','2012-07-17 05:19:42',1,'2012-07-17 05:19:42',1),(4,2,NULL,NULL,100,'Declan has just started','2012-07-17 05:20:22',1,'2012-07-17 05:20:22',1),(5,13,3,NULL,200,'ras','2012-07-19 17:03:50',1,'2012-07-19 17:03:50',1),(6,13,3,NULL,200,'ras','2012-07-19 17:04:16',1,'2012-07-19 17:04:16',1),(7,13,4,NULL,200,'ras','2012-07-19 17:07:53',1,'2012-07-19 17:07:53',1),(8,13,4,NULL,200,'ras','2012-07-19 17:16:05',1,'2012-07-19 17:16:05',1),(9,13,4,NULL,200,'ras','2012-07-19 17:16:39',1,'2012-07-19 17:16:39',1),(10,13,4,NULL,200,'ras','2012-07-19 17:49:52',1,'2012-07-19 17:49:52',1),(11,13,4,NULL,200,'ras','2012-07-19 17:50:01',1,'2012-07-19 17:50:01',1),(12,13,4,NULL,200,'ras','2012-07-19 17:50:15',1,'2012-07-19 17:50:15',1),(13,13,5,NULL,200,'ras','2012-07-19 17:55:40',1,'2012-07-19 17:55:40',1),(14,13,5,NULL,200,'ras','2012-07-19 17:55:54',1,'2012-07-19 17:55:54',1),(15,2,6,NULL,200,'my note of approval','2012-08-12 15:38:54',1,'2012-08-12 15:38:54',1),(16,16,NULL,NULL,100,'test adding first note','2012-08-21 09:28:15',1,'2012-08-21 09:28:15',1);
/*!40000 ALTER TABLE `m_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_office`
--

DROP TABLE IF EXISTS `m_office`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_office` (
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
  CONSTRAINT `FK2291C477E2551DCC` FOREIGN KEY (`parent_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_office`
--

LOCK TABLES `m_office` WRITE;
/*!40000 ALTER TABLE `m_office` DISABLE KEYS */;
INSERT INTO `m_office` VALUES (1,NULL,'.','1','Head Office','2009-01-01',NULL,NULL,'2012-07-13 17:04:20',1),(2,1,'.2.','2','sub branch 1','2012-01-02',1,'2012-04-14 05:42:40','2012-04-16 11:47:05',1),(3,1,'.3.','3','sub branch 2','2012-03-01',1,'2012-04-16 04:12:02','2012-04-16 11:47:14',1),(4,1,'.4.',NULL,'sub branch 3','2012-04-17',1,'2012-04-17 06:01:10','2012-04-17 06:01:10',1),(5,3,'.3.5.','old 44334','Eastern Branch','2012-07-04',1,'2012-07-17 01:23:31','2012-07-17 01:23:31',1),(6,4,'.4.6.',NULL,'new branch','2012-07-01',1,'2012-07-17 03:19:27','2012-07-17 03:19:27',1),(7,10,'.10.7.','Mawi a','Myanmar 1','2012-07-19',1,'2012-07-19 15:05:35','2012-07-19 15:29:20',1),(9,1,'.9.','Heavens Family Kenya','Saboti','2012-07-19',1,'2012-07-19 15:09:45','2012-07-19 15:14:15',1),(10,1,'.10.','Myanmar','Myanmar Regional Office','2012-07-19',1,'2012-07-19 15:24:14','2012-07-19 15:24:14',1),(11,1,'.11.','Kenya','Kenya Regional Office','2012-07-19',1,'2012-07-19 15:24:59','2012-07-19 15:24:59',1),(12,1,'.12.','HGT','Hugo Technologies','2012-07-23',1,'2012-07-23 04:22:19','2012-07-23 04:22:19',1),(13,12,'.12.13.','HGTRO','HGT RO','2012-07-23',1,'2012-07-23 04:22:57','2012-07-23 04:22:57',1),(14,13,'.12.13.14.','HGTRetail','HGT Retail','2012-07-23',1,'2012-07-23 04:23:59','2012-07-23 04:23:59',1),(15,13,'.12.13.15.','HGTWS','HGT Wholesale','2012-07-23',1,'2012-07-23 04:24:24','2012-07-23 04:24:24',1),(16,15,'.12.13.15.16.','HGTGroup1','HGT Group One','2012-07-23',1,'2012-07-23 04:24:59','2012-07-23 04:26:19',1),(17,15,'.12.13.15.17.','HGTGroup2','HGT Group Two','2012-07-23',1,'2012-07-23 04:26:47','2012-07-23 04:26:47',1);
/*!40000 ALTER TABLE `m_office` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_office_transaction`
--

DROP TABLE IF EXISTS `m_office_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_office_transaction` (
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
  CONSTRAINT `FK1E37728B783C5C25` FOREIGN KEY (`from_office_id`) REFERENCES `m_office` (`id`),
  CONSTRAINT `FK1E37728B93C6C1B6` FOREIGN KEY (`to_office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_office_transaction`
--

LOCK TABLES `m_office_transaction` WRITE;
/*!40000 ALTER TABLE `m_office_transaction` DISABLE KEYS */;
INSERT INTO `m_office_transaction` VALUES (1,NULL,1,'USD',2,'55555.000000','2012-07-17',NULL,'2012-07-17 03:07:09',1,'2012-07-17 03:07:09',1),(2,1,3,'LBP',2,'34000.000000','2012-06-06',NULL,'2012-07-17 03:38:09',1,'2012-07-17 03:38:09',1),(3,1,9,'USD',2,'2000.000000','2012-08-09','hey dude','2012-08-22 17:12:09',1,'2012-08-22 17:12:09',1);
/*!40000 ALTER TABLE `m_office_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_organisation_currency`
--

DROP TABLE IF EXISTS `m_organisation_currency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_organisation_currency` (
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
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_organisation_currency`
--

LOCK TABLES `m_organisation_currency` WRITE;
/*!40000 ALTER TABLE `m_organisation_currency` DISABLE KEYS */;
INSERT INTO `m_organisation_currency` VALUES (30,'ZAR',2,'South African Rand','R','currency.ZAR',1,'2012-08-06 08:58:08','2012-08-06 08:58:08',1),(31,'KES',2,'Kenyan Shilling','KSh','currency.KES',1,'2012-08-06 08:58:08','2012-08-06 08:58:08',1),(32,'BND',2,'Brunei Dollar','B$','currency.BND',1,'2012-08-06 08:58:08','2012-08-06 08:58:08',1),(33,'LBP',2,'Lebanese Pound','L£','currency.LBP',1,'2012-08-06 08:58:08','2012-08-06 08:58:08',1),(34,'GHC',2,'Ghana Cedi','GHc','currency.GHC',1,'2012-08-06 08:58:08','2012-08-06 08:58:08',1),(35,'USD',2,'US Dollar','$','currency.USD',1,'2012-08-06 08:58:08','2012-08-06 08:58:08',1),(36,'XOF',0,'CFA Franc BCEAO','CFA','currency.XOF',1,'2012-08-06 08:58:08','2012-08-06 08:58:08',1),(37,'MMK',2,'Myanmar Kyat','K','currency.MMK',1,'2012-08-06 08:58:08','2012-08-06 08:58:08',1);
/*!40000 ALTER TABLE `m_organisation_currency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_permission`
--

DROP TABLE IF EXISTS `m_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_enum` smallint(5) NOT NULL,
  `code` varchar(100) NOT NULL,
  `default_description` varchar(500) NOT NULL,
  `default_name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_permission`
--

LOCK TABLES `m_permission` WRITE;
/*!40000 ALTER TABLE `m_permission` DISABLE KEYS */;
INSERT INTO `m_permission` VALUES (1,1,'USER_ADMINISTRATION_SUPER_USER_ROLE','An application user will have permission to execute all tasks related to user administration.','User administration ALL'),(2,2,'ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE','An application user will have permission to execute all tasks related to organisation administration.','Organisation adminsitration ALL'),(3,3,'PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE','An application user will have permission to execute all tasks related to portfolio management.','Portfolio management ALL'),(4,4,'REPORTING_SUPER_USER_ROLE','An application user will have permission to execute and view all reports.','Reporting ALL'),(5,3,'CAN_SUBMIT_NEW_LOAN_APPLICATION_ROLE','Allows an application user to sumit new loan application.','Can submit new loan application'),(6,3,'CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE','Allows an application user to sumit new loan application where the submitted on date is in the past.','Can submit historic loan application'),(7,3,'CAN_APPROVE_LOAN_ROLE','Allows an application user to approve a loan application.','Can approve loan application'),(8,3,'CAN_APPROVE_LOAN_IN_THE_PAST_ROLE','Allows an application user to approve a loan application where the approval date is in the past.','Can approve loan application in the past'),(9,3,'CAN_REJECT_LOAN_ROLE','Allows an application user to reject a loan application.','Can reject loan application'),(10,3,'CAN_REJECT_LOAN_IN_THE_PAST_ROLE','Allows an application user to reject a loan application where the rejected date is in the past.','Can reject loan application in the past'),(11,3,'CAN_WITHDRAW_LOAN_ROLE','Allows an application user to mark loan application as withdrawn by client.','Can withdraw loan application'),(12,3,'CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE','Allows an application user to mark loan application as withdrawn by client where the withdran on date is in the past.','Can withdraw loan application in the past'),(13,3,'CAN_DELETE_LOAN_THAT_IS_SUBMITTED_AND_NOT_APPROVED','Allows an application user to complete delete the loan application if it is submitted but not approved.','Can delete submitted loan application'),(14,3,'CAN_UNDO_LOAN_APPROVAL_ROLE','Allows an application user to undo a loan approval.','Can undo loan approval'),(15,3,'CAN_DISBURSE_LOAN_ROLE','Allows an application user to disburse a loan application.','Can disburse loan'),(16,3,'CAN_DISBURSE_LOAN_IN_THE_PAST_ROLE','Allows an application user to disburse a loan where the disbursement date is in the past.','Can disburse loan in the past'),(17,3,'CAN_UNDO_LOAN_DISBURSAL_ROLE','Allows an application user to undo a loan disbursal if not payments already made.','Can undo loan disbursal'),(18,3,'CAN_MAKE_LOAN_REPAYMENT_LOAN_ROLE','Allows an application user to enter a repayment on the loan.','Can enter a repayment against a loan'),(19,3,'CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE','Allows an application user to enter a repayment on the loan where the repayment date is in the past.','Can enter a repayment against a loan in the past'),(20,3,'CAN_ENROLL_NEW_CLIENT_ROLE','Allows an application user to add a new client.','Can add a new client.'),(21,3,'CAN_RUN_Client Listing','CAN_RUN_Client Listing','CAN_RUN_Client Listing'),(22,3,'CAN_RUN_Client Listing (Pentaho)','CAN_RUN_Client Listing (Pentaho)','CAN_RUN_Client Listing (Pentaho)'),(23,3,'CAN_RUN_Client Listing - Additional and Unlikely','CAN_RUN_Client Listing - Additional and Unlikely','CAN_RUN_Client Listing - Additional and Unlikely'),(24,3,'CAN_RUN_Client Loans Listing','CAN_RUN_Client Loans Listing','CAN_RUN_Client Loans Listing'),(25,3,'CAN_RUN_Client Loans Listing - Additional','CAN_RUN_Client Loans Listing - Additional','CAN_RUN_Client Loans Listing - Additional'),(26,3,'CAN_RUN_Clients by Surname (a bit silly)','CAN_RUN_Clients by Surname (a bit silly)','CAN_RUN_Clients by Surname (a bit silly)'),(27,3,'CAN_RUN_Funds Disbursed Summary - Currency','CAN_RUN_Funds Disbursed Summary - Currency','CAN_RUN_Funds Disbursed Summary - Currency'),(28,3,'CAN_RUN_Funds Disbursed Summary - Office, Currency','CAN_RUN_Funds Disbursed Summary - Office, Currency','CAN_RUN_Funds Disbursed Summary - Office, Currency'),(29,3,'CAN_RUN_Loan Disbursals in Previous Year','CAN_RUN_Loan Disbursals in Previous Year','CAN_RUN_Loan Disbursals in Previous Year'),(30,3,'CAN_RUN_Loan Repayments in Previous Year','CAN_RUN_Loan Repayments in Previous Year','CAN_RUN_Loan Repayments in Previous Year'),(31,3,'CAN_RUN_Loans - Active Loans Portfolio Status','CAN_RUN_Loans - Active Loans Portfolio Status','CAN_RUN_Loans - Active Loans Portfolio Status'),(32,3,'CAN_RUN_Loans Awaiting Disbursal','CAN_RUN_Loans Awaiting Disbursal','CAN_RUN_Loans Awaiting Disbursal'),(33,3,'CAN_RUN_Portfolio at Risk','CAN_RUN_Portfolio at Risk','CAN_RUN_Portfolio at Risk'),(34,3,'CAN_RUN_Upcoming Repayments','CAN_RUN_Upcoming Repayments','CAN_RUN_Upcoming Repayments'),(35,3,'CAN_RUN_Upcoming Repayments Summary - Month, Currency, Loan Risk','CAN_RUN_Upcoming Repayments Summary - Month, Currency, Loan Risk','CAN_RUN_Upcoming Repayments Summary - Month, Currency, Loan Risk'),(36,3,'CAN_READ_m_client_xAdditional Information','CAN_READ_m_client_xAdditional Information','CAN_READ_m_client_xAdditional Information'),(37,3,'CAN_READ_m_client_xHighly Improbable Info','CAN_READ_m_client_xHighly Improbable Info','CAN_READ_m_client_xHighly Improbable Info'),(38,3,'CAN_READ_m_loan_xAdditional Information','CAN_READ_m_loan_xAdditional Information','CAN_READ_m_loan_xAdditional Information'),(39,3,'CAN_UPDATE_m_client_xAdditional Information','CAN_UPDATE_m_client_xAdditional Information','CAN_UPDATE_m_client_xAdditional Information'),(40,3,'CAN_UPDATE_m_client_xHighly Improbable Info','CAN_UPDATE_m_client_xHighly Improbable Info','CAN_UPDATE_m_client_xHighly Improbable Info'),(41,3,'CAN_UPDATE_m_loan_xAdditional Information','CAN_UPDATE_m_loan_xAdditional Information','CAN_UPDATE_m_loan_xAdditional Information');
/*!40000 ALTER TABLE `m_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_product_deposit`
--

DROP TABLE IF EXISTS `m_product_deposit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_product_deposit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `external_id` varchar(100) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `minimum_balance` decimal(19,6) DEFAULT NULL,
  `maximum_balance` decimal(19,6) DEFAULT NULL,
  `tenure_months` int(11) NOT NULL,
  `interest_compounded_every` smallint(5) NOT NULL DEFAULT '1',
  `interest_compounded_every_period_enum` smallint(5) NOT NULL DEFAULT '2',
  `maturity_default_interest_rate` decimal(19,6) NOT NULL,
  `maturity_min_interest_rate` decimal(19,6) NOT NULL,
  `maturity_max_interest_rate` decimal(19,6) NOT NULL,
  `is_renewal_allowed` tinyint(1) NOT NULL DEFAULT '0',
  `is_preclosure_allowed` tinyint(1) NOT NULL DEFAULT '0',
  `pre_closure_interest_rate` decimal(19,6) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_deposit_product` (`name`),
  UNIQUE KEY `externalid_deposit_product` (`external_id`),
  KEY `FKJPW0000000000003` (`createdby_id`),
  KEY `FKJPW0000000000004` (`lastmodifiedby_id`),
  CONSTRAINT `FKJPX0000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FKJPX0000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_product_deposit`
--

LOCK TABLES `m_product_deposit` WRITE;
/*!40000 ALTER TABLE `m_product_deposit` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_product_deposit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_product_loan`
--

DROP TABLE IF EXISTS `m_product_loan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_product_loan` (
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
  `loan_transaction_strategy_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKAUD0000000000003` (`createdby_id`),
  KEY `FKAUD0000000000004` (`lastmodifiedby_id`),
  KEY `FKA6A8A7D77240145` (`fund_id`),
  KEY `FK_ltp_strategy` (`loan_transaction_strategy_id`),
  CONSTRAINT `FK_ltp_strategy` FOREIGN KEY (`loan_transaction_strategy_id`) REFERENCES `ref_loan_transaction_processing_strategy` (`id`),
  CONSTRAINT `FKA6A8A7D77240145` FOREIGN KEY (`fund_id`) REFERENCES `m_fund` (`id`),
  CONSTRAINT `FKAUD0000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FKAUD0000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_product_loan`
--

LOCK TABLES `m_product_loan` WRITE;
/*!40000 ALTER TABLE `m_product_loan` DISABLE KEYS */;
INSERT INTO `m_product_loan` VALUES (1,'XOF',0,'100000.000000','1000.000000','Agricultural Loan','An agricultural loan given to farmers to help buy crop, stock and machinery. With an arrears tolerance setting of 1,000 CFA, loans are not marked as \'in arrears\' or \'in bad standing\' if the amount outstanding is less than this. Interest rate is described using monthly percentage rate (MPR) even though the loan typically lasts a year and requires one repayment (typically at time when farmer sells crop)',1,'1.750000',2,'21.000000',0,1,12,2,1,1,'\0','\0',1,'2012-04-12 22:14:34','2012-07-17 00:34:01',1,1),(2,'LBP',2,'35000.000000','10.000000','Different Loan Type',NULL,NULL,'1.000000',2,'12.000000',0,1,2,1,10,1,'\0','\0',1,'2012-07-17 03:20:34','2012-07-17 03:20:34',1,1),(3,'BND',2,'5000.000000','10.000000','Further Loan Product',NULL,2,'1.500000',2,'18.000000',0,1,1,2,6,0,'\0','\0',1,'2012-07-17 03:21:38','2012-07-17 03:21:38',1,1),(4,'MMK',0,'50000.000000','0.000000','General','General micro-loan',3,'1.250000',2,'36.000000',1,1,1,2,12,1,'\0','\0',1,'2012-07-19 15:34:27','2012-07-19 17:13:59',1,1),(5,'MMK',0,'50000.000000','0.000000','Agricultural','Agricultural micro-loan',4,'1.250000',2,'36.000000',1,1,12,2,2,1,'\0','\0',1,'2012-07-19 15:36:16','2012-07-19 17:14:15',1,1),(6,'MMK',2,'100.000000','10.000000','testloan','testloan from client app',4,'10.000000',2,'120.000000',0,1,1,2,10,0,'\0','\0',1,'2012-08-01 07:35:46','2012-08-01 07:35:46',1,1),(7,'XOF',0,'500.000000','2000.000000','new-lp-test-1','test creating new lp',3,'2.000000',2,'24.000000',0,1,1,2,12,1,'\0','\0',1,'2012-09-02 10:47:10','2012-09-02 10:47:20',1,NULL),(8,'GHC',2,'30000.000000','0.000000','another-test-lp','another test updated.',1,'2.000000',2,'24.000000',0,1,1,2,12,1,'\0','\0',1,'2012-09-02 11:04:41','2012-09-02 11:08:24',1,NULL);
/*!40000 ALTER TABLE `m_product_loan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_product_loan_charge`
--

DROP TABLE IF EXISTS `m_product_loan_charge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_product_loan_charge` (
  `product_loan_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_loan_id`,`charge_id`),
  KEY `charge_id` (`charge_id`),
  CONSTRAINT `m_product_loan_charge_ibfk_1` FOREIGN KEY (`charge_id`) REFERENCES `m_charge` (`id`),
  CONSTRAINT `m_product_loan_charge_ibfk_2` FOREIGN KEY (`product_loan_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_product_loan_charge`
--

LOCK TABLES `m_product_loan_charge` WRITE;
/*!40000 ALTER TABLE `m_product_loan_charge` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_product_loan_charge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_product_savings`
--

DROP TABLE IF EXISTS `m_product_savings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_product_savings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `currency_code` varchar(3) DEFAULT NULL,
  `currency_digits` smallint(5) DEFAULT NULL,
  `interest_rate` decimal(19,6) DEFAULT NULL,
  `minimum_balance` decimal(19,6) DEFAULT NULL,
  `maximum_balance` decimal(19,6) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKJPW0000000000003` (`createdby_id`),
  KEY `FKJPW0000000000004` (`lastmodifiedby_id`),
  CONSTRAINT `FKJPW0000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FKJPW0000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_product_savings`
--

LOCK TABLES `m_product_savings` WRITE;
/*!40000 ALTER TABLE `m_product_savings` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_product_savings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_role`
--

DROP TABLE IF EXISTS `m_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_role`
--

LOCK TABLES `m_role` WRITE;
/*!40000 ALTER TABLE `m_role` DISABLE KEYS */;
INSERT INTO `m_role` VALUES (1,'Super user','This role provides all application permissions.',NULL,NULL,NULL,NULL),(2,'Field officer','A field officer role allows the user to add client and loans and view reports but nothing else.',1,'2012-04-12 15:59:48','2012-04-12 15:59:48',1),(3,'Data Entry (Portfolio only)','This role allows a user full permissions around client and loan functionality but nothing else.',1,'2012-04-12 16:01:25','2012-04-12 16:01:25',1),(4,'Credit Committe Member','This role allows a user to approve reject or withdraw loans (with reporting).',1,'2012-04-12 16:11:25','2012-04-12 16:11:25',1),(5,'Manager','This role allows a manager to do anything related to portfolio management and also view all reports.',1,'2012-04-12 17:02:11','2012-04-12 17:02:11',1),(6,'Rola1','aaa',1,'2012-08-02 13:02:57','2012-08-02 13:02:57',1),(7,'Read-only-example','User with this role should be only able to view portfolio & reporting information.',1,'2012-08-21 21:56:05','2012-08-21 21:56:05',1);
/*!40000 ALTER TABLE `m_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_role_permission`
--

DROP TABLE IF EXISTS `m_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_role_permission` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FK8DEDB04815CEC7AB` (`role_id`),
  KEY `FK8DEDB048103B544B` (`permission_id`),
  CONSTRAINT `FK8DEDB048103B544B` FOREIGN KEY (`permission_id`) REFERENCES `m_permission` (`id`),
  CONSTRAINT `FK8DEDB04815CEC7AB` FOREIGN KEY (`role_id`) REFERENCES `m_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_role_permission`
--

LOCK TABLES `m_role_permission` WRITE;
/*!40000 ALTER TABLE `m_role_permission` DISABLE KEYS */;
INSERT INTO `m_role_permission` VALUES (1,1),(1,2),(1,3),(1,4),(2,4),(2,5),(2,6),(2,13),(2,20),(3,3),(4,4),(4,7),(4,8),(4,9),(4,10),(4,11),(4,12),(5,3),(5,4),(6,2),(7,4);
/*!40000 ALTER TABLE `m_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `m_staff`
--

DROP TABLE IF EXISTS `m_staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `m_staff` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `firstname` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `display_name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `display_name` (`display_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `m_staff`
--

LOCK TABLES `m_staff` WRITE;
/*!40000 ALTER TABLE `m_staff` DISABLE KEYS */;
/*!40000 ALTER TABLE `m_staff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `r_enum_value`
--

DROP TABLE IF EXISTS `r_enum_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `r_enum_value` (
  `enum_name` varchar(100) NOT NULL,
  `enum_id` int(11) NOT NULL,
  `enum_message_property` varchar(100) NOT NULL,
  `enum_value` varchar(100) NOT NULL,
  PRIMARY KEY (`enum_name`,`enum_id`),
  UNIQUE KEY `enum_message_property` (`enum_name`,`enum_message_property`),
  UNIQUE KEY `enum_value` (`enum_name`,`enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `r_enum_value`
--

LOCK TABLES `r_enum_value` WRITE;
/*!40000 ALTER TABLE `r_enum_value` DISABLE KEYS */;
INSERT INTO `r_enum_value` VALUES ('loan_status_id',100,'Submitted and awaiting approval','Submitted and awaiting approval'),('loan_status_id',200,'Approved','Approved'),('loan_status_id',300,'Active','Active'),('loan_status_id',400,'Withdrawn by client','Withdrawn by client'),('loan_status_id',500,'Rejected','Rejected'),('loan_status_id',600,'Closed','Closed'),('loan_status_id',700,'Overpaid','Overpaid'),('loan_transaction_strategy_id',1,'mifos-standard-strategy','Mifos style'),('loan_transaction_strategy_id',2,'heavensfamily-strategy','Heavensfamily'),('loan_transaction_strategy_id',3,'creocore-strategy','Creocore'),('loan_transaction_strategy_id',4,'rbi-india-strategy','RBI (India)');
/*!40000 ALTER TABLE `r_enum_value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ref_loan_transaction_processing_strategy`
--

DROP TABLE IF EXISTS `ref_loan_transaction_processing_strategy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ref_loan_transaction_processing_strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(100) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ltp_strategy_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ref_loan_transaction_processing_strategy`
--

LOCK TABLES `ref_loan_transaction_processing_strategy` WRITE;
/*!40000 ALTER TABLE `ref_loan_transaction_processing_strategy` DISABLE KEYS */;
INSERT INTO `ref_loan_transaction_processing_strategy` VALUES (1,'mifos-standard-strategy','Mifos style',NULL,NULL,NULL,NULL),(2,'heavensfamily-strategy','Heavensfamily',NULL,NULL,NULL,NULL),(3,'creocore-strategy','Creocore',NULL,NULL,NULL,NULL),(4,'rbi-india-strategy','RBI (India)',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `ref_loan_transaction_processing_strategy` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchy_parameter`
--

LOCK TABLES `stretchy_parameter` WRITE;
/*!40000 ALTER TABLE `stretchy_parameter` DISABLE KEYS */;
INSERT INTO `stretchy_parameter` VALUES (3,'FullReportList',NULL,'n/a','n/a','n/a','n/a','Y',NULL,NULL,'select r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\r\nrp.parameter_id, rp.report_parameter_name, p.parameter_name\r\nfrom stretchy_report r\r\nleft join stretchy_report_parameter rp on rp.report_id = r.report_id\r\nleft join stretchy_parameter p on p.parameter_id = rp.parameter_id\r\nwhere r.use_report is true\r\norder by r.report_name, rp.parameter_id'),(4,'FullParameterList',NULL,'n/a','n/a','n/a','n/a','Y',NULL,NULL,'select parameter_name, parameter_variable, parameter_label, parameter_displayType, parameter_FormatType, parameter_default, selectOne,  selectAll\r\nfrom stretchy_parameter p\r\nwhere special is null\r\norder by parameter_id'),(5,'selectOfficeId','officeId','Office','select','number','0',NULL,'Y',NULL,'select id, \r\nconcat(substring(\"........................................\", 1, \r\n   ((LENGTH(`hierarchy`) - LENGTH(REPLACE(`hierarchy`, \'.\', \'\')) - 1) * 4)), \r\n   `name`) as tc\r\nfrom m_office\r\norder by hierarchy'),(6,'currencyIdSelectAll','currencyId','Currency','select','number','0',NULL,'Y','Y','select `code`, `name`\r\nfrom m_organisation_currency\r\norder by `code`'),(7,'currencyIdSelectOne','currencyId','Currency','select','number','0',NULL,'Y',NULL,'select `code`, `name`\r\nfrom m_organisation_currency\r\norder by `code`'),(10,'fundIdSelectAll','fundId','Fund','select','number','0',NULL,'Y','Y','(select id, `name`\r\nfrom m_fund\r\norder by `name`)\r\nunion all\r\n(select -10, \'-\')'),(80,'selectStartDate','startDate','startDate','date','date','today',NULL,NULL,NULL,NULL),(81,'selectEndDate','endDate','endDate','date','date','today',NULL,NULL,NULL,NULL),(82,'reportCategoryList',NULL,'n/a','n/a','n/a','n/a','Y',NULL,NULL,'select r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\r\nrp.parameter_id, rp.report_parameter_name, p.parameter_name\r\nfrom stretchy_report r\r\nleft join stretchy_report_parameter rp on rp.report_id = r.report_id\r\nleft join stretchy_parameter p on p.parameter_id = rp.parameter_id\r\nwhere r.report_category = \'${reportCategory}\'\r\nand r.use_report is true\r\norder by r.report_name, rp.parameter_id');
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
  `description` text,
  `core_report` tinyint(1) DEFAULT '0',
  `use_report` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `report_name_UNIQUE` (`report_name`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchy_report`
--

LOCK TABLES `stretchy_report` WRITE;
/*!40000 ALTER TABLE `stretchy_report` DISABLE KEYS */;
INSERT INTO `stretchy_report` VALUES (29,'Client Listing','Table',NULL,'Client','select ounder.`name` as \"Office/Branch\",  concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\", \r\nc.joining_date as \"Joining Date\"\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\nwhere o.id = ${officeId}\r\nand c.is_deleted=0\r\norder by ounder.hierarchy','Simple sample screen report.\r\nNot really expected to be part of core reports.',0,1),(30,'Client Loans Listing','Table',NULL,'Client','select ounder.`name` as \"Office/Branch\", concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\",c.joining_date as \"Joining Date\",\r\np.name as Loan, st.enum_message_property as \"Status\",  l.number_of_repayments as \"Expected No. Repayments\",\r\nl.annual_nominal_interest_rate as \" Annual Nominal Interest Rate\", \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  l.principal_amount,date( l.submittedon_date) as Submitted,\r\ndate(l.approvedon_date) Approved, l.expected_disbursedon_date As \"Expected Disbursal\",\r\ndate(l.expected_firstrepaymenton_date) as \"Expected First Repayment\",\r\ndate(l.expected_maturedon_date) \"Expected Maturity\",\r\ndate(l.disbursedon_date) as Disbursed, date(l.closedon_date) as Closed,\r\ndate(l.withdrawnon_date) as Withdrawn, date(l.rejectedon_date) as Rejected,\r\ndate(l.rescheduledon_date) Rescheduled, date(l.writtenoffon_date) \"Written Off\"\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\nleft join m_loan l on l.client_id = c.id\r\nleft join m_product_loan p on p.id = l.product_id\r\nleft join r_enum_value st on st.enum_name = \'loan_status_id\' and st.enum_id = l.loan_status_id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\norder by ounder.hierarchy, \"Name\" , l.id','Can be a basic core loan report.',1,1),(31,'Loans Awaiting Disbursal','Table',NULL,'Loan Portfolio','SELECT ounder.`name` as \"Office/Branch\", concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\", \r\nl.id as \"Loan ID\", pl.`name` as \"Product\", \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nl.principal_amount as Principal,  \r\ndate(l.approvedon_date) \"Approved\", l.expected_disbursedon_date \"Expected Disbursal\"\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_product_loan pl on pl.id = l.product_id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\nand l.loan_status_id = 200\r\norder by ounder.hierarchy, l.expected_disbursedon_date, \"Name\"',NULL,0,1),(32,'Upcoming Repayments','Table',NULL,'Loan Portfolio','SELECT r.duedate \"Due Date\", ounder.`name` as \"Office/Branch\",   ifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nc.id as \"Client ID\",\r\nconcat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\", \r\nl.id as \"Loan ID\", pl.`name` as \"Product\", \r\n\r\n(select\r\n    if(if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) > 90, \"High\", \r\n        if (if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) > 30, \"Medium\", \r\n            if (if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) = 0, \r\n                if (min(r.installment) = 1, \"Low\", \"Very Low\"),\r\n                if (min(r.installment), \"Medium\", \"Low\")))) \r\nfrom m_loan lr\r\nleft join m_loan_repayment_schedule r on r.loan_id = lr.id\r\n                                        and r.completed_derived is false\r\nwhere lr.id = l.id) as \"Loan Risk\",\r\n\r\n r.installment as Installment, \r\n(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Due\",\r\n(r.interest_amount - ifnull(r.interest_completed_derived, 0)) as \"Interest Due\" ,\r\n((r.principal_amount - ifnull(r.principal_completed_derived, 0)) +\r\n(r.interest_amount - ifnull(r.interest_completed_derived, 0))) as \"Total Due\"\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_product_loan pl on pl.id = l.product_id\r\njoin m_loan_repayment_schedule r on r.loan_id = l.id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\" )\r\nand l.loan_status_id = 300\r\nand r.duedate >= curdate()\r\norder by r.duedate, \"Name\"','Unlikely to be generally useful.  Used as an example for Creocore',0,1),(34,'Loans - Active Loans Portfolio Status','Table',NULL,'Loan','select  x.`Office/Branch`, x.Currency, x.`Name`, x.Loan, x.`Loan ID`, x.Disbursed, \r\nx.`Principal Outstanding`, x.`Interest Outstanding`, x.`Days Overdue`,\r\nx.`Principal Overdue`, x.`Interest Overdue`, \r\n\r\nif(x.`Days Overdue` > 90, \"High\", \r\n        if (x.`Days Overdue` > 30, \"Medium\", \r\n            if (`Days Overdue` = 0, \r\n                if (`First Overdue Installment` = 1, \"Low\", \"Very Low\"),\r\n                if (`First Overdue Installment` = 1, \"Medium\", \"Low\"))))\r\n\r\nas \"Loan Risk\", \r\nx.`First Overdue Installment`, x.`First Overdue Installment Date`\r\nfrom\r\n(select ounder.hierarchy, ounder.`name` as \"Office/Branch\", concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\", p.`name` as Loan,\r\nl.id as \"Loan ID\", l.disbursedon_date as Disbursed, ifnull(cur.display_symbol, l.currency_code) as Currency,\r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Outstanding\",\r\nsum(r.interest_amount - ifnull(r.interest_completed_derived, 0)) as \"Interest Outstanding\",\r\nif(datediff(curdate(), min(duedate)) < 0, 0, datediff(curdate(), min(duedate))) as \"Days Overdue\",   \r\nmin(installment) as \"First Overdue Installment\",\r\nmin(duedate) as \"First Overdue Installment Date\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) as \"Principal Overdue\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.interest_amount - ifnull(r.interest_completed_derived, 0))\r\n            , 0)) as \"Interest Overdue\"\r\n\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nleft join m_product_loan p on p.id = l.product_id\r\nleft join m_loan_repayment_schedule r on r.loan_id = l.id\r\n                                        and r.completed_derived is false\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand l.loan_status_id = 300\r\ngroup by l.id) x\r\norder by x.hierarchy, x.Currency, x.`Name`, x.Loan, x.`Loan ID`',NULL,0,1),(35,'Portfolio at Risk','Table',NULL,'Loan Portfolio','select  \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  \r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Outstanding\",\r\nsum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) as \"Principal Overdue\",\r\n            \r\n    cast(round(\r\n    (sum(if(r.duedate <= curdate(), \r\n        (r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 0)) * 100) / \r\n            sum(r.principal_amount - ifnull(r.principal_completed_derived, 0))\r\n            , 2) as char)\r\n            as \"Portfolio at Risk %\"\r\n            \r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin  m_loan l on l.client_id = c.id\r\nleft join m_product_loan p on p.id = l.product_id\r\nleft join m_loan_repayment_schedule r on r.loan_id = l.id\r\n                                        and r.completed_derived is false\r\nleft join m_currency cur on cur.code = l.currency_code\r\n\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\")\r\nand l.loan_status_id = 300\r\ngroup by l.currency_code\r\norder by l.currency_code',NULL,0,1),(38,'Upcoming Repayments Summary - Month, Currency, Loan Risk','Table',NULL,'Loan Portfolio','SELECT DATE_FORMAT(r.duedate,\'%Y-%m\') \"Due Month\",  ifnull(cur.display_symbol, l.currency_code) as Currency,  \r\n\r\n(select\r\n    if(if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) > 90, \"4 High\", \r\n        if (if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) > 30, \"3 Medium\", \r\n            if (if(datediff(curdate(), min(r.duedate)) < 0, 0, datediff(curdate(), min(r.duedate))) = 0, \r\n                if (min(r.installment) = 1, \"Low\", \"1 Very Low\"),\r\n                if (min(r.installment), \"3 Medium\", \"2 Low\")))) \r\nfrom m_loan lr\r\nleft join m_loan_repayment_schedule r on r.loan_id = lr.id\r\n                                        and r.completed_derived is false\r\nwhere lr.id = l.id) as \"Loan Risk\",\r\n\r\nsum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) as \"Principal Due\",\r\nsum(r.interest_amount - ifnull(r.interest_completed_derived, 0)) as \"Interest Due\",\r\n\r\n(sum(r.principal_amount - ifnull(r.principal_completed_derived, 0)) +\r\nsum(r.interest_amount - ifnull(r.interest_completed_derived, 0))) as \"Total Due\"\r\n\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_loan_repayment_schedule r on r.loan_id = l.id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\nand (l.currency_code = \"${currencyId}\" or \"-1\" = \"${currencyId}\" )\r\nand l.loan_status_id = 300\r\nand r.duedate >= curdate()\r\ngroup by `Due Month`, `Currency`, `Loan Risk`\r\norder by `Due Month`,  `Loan Risk`',NULL,0,1),(39,'Loan Disbursals in Previous Year','Chart','Bar','Loan Portfolio','select prevYr.startMonth as \"Disbursed Month\", ifnull(monthlyDisb.disbursed,0) as Disbursed\r\nfrom\r\n(SELECT DATE_FORMAT(@startDate := date_add(@startDate, interval 1 Month),\'%Y-%m\') as startMonth\r\nFROM rpt_sequence s, (SELECT @startDate := date_add(curdate(), interval -13 Month)) d\r\nWHERE s.id <= 13) prevYr\r\nleft join \r\n(select DATE_FORMAT(lt.transaction_date,\'%Y-%m\') as txnMonth, sum(lt.amount) as disbursed\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_loan_transaction lt on lt.loan_id = l.id\r\nwhere o.id = ${officeId}\r\nand l.currency_code = \"${currencyId}\"\r\nand lt.transaction_type_enum = 1\r\ngroup by `txnMonth`) monthlyDisb on monthlyDisb.txnMonth = prevYr.startMonth',NULL,0,1),(40,'Loan Repayments in Previous Year','Chart','Bar','Loan Portfolio','select prevYr.startMonth as \"Repayment Month\", ifnull(monthlyR.repaid,0) as Repaid\r\nfrom\r\n(SELECT DATE_FORMAT(@startDate := date_add(@startDate, interval 1 Month),\'%Y-%m\') as startMonth\r\nFROM rpt_sequence s, (SELECT @startDate := date_add(curdate(), interval -13 Month)) d\r\nWHERE s.id <= 13) prevYr\r\nleft join \r\n(select DATE_FORMAT(lt.transaction_date,\'%Y-%m\') as txnMonth, sum(lt.amount) as repaid\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_loan_transaction lt on lt.loan_id = l.id\r\nwhere o.id = ${officeId}\r\nand l.currency_code = \"${currencyId}\"\r\nand lt.transaction_type_enum = 2 and lt.contra_id is null\r\ngroup by `txnMonth`) monthlyR on monthlyR.txnMonth = prevYr.startMonth\r\norder by prevYr.startMonth',NULL,0,1),(41,'Clients by Surname (a bit silly)','Chart','Pie','Client','select c.lastname, count(*)  as thecount\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\nwhere o.id = ${officeId}\r\nand c.is_deleted=0\r\ngroup by c.lastname',NULL,0,1),(42,'Client Listing - Additional and Unlikely','Table',NULL,'Client','select ounder.`name` as \"Office/Branch\",  concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\",  \r\na1.`Ethnic Group`, a1.`Ethnic Group Other`, a1.`Household Location`, a1. `Household Location Other`,\r\na1.Religion, a1.`Religion Other`, a1.`Knowledge of Person`, a1.Gender, a1.whois,\r\na2.`Fathers Favourite Team`, a2.`Mothers Favourite Team`, a2.`Fathers DOB`, a2.`Mothers DOB`,\r\na2.`Fathers Education`, a2.`Mothers Education`, a2.`Number of Children`,\r\na2.`Favourite Town`, a2.`Closing Comments`, a2.`Annual Family Income`\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\nleft join `m_client_xAdditional Information` a1 on a1.id = c.id\r\nleft join `m_client_xHighly Improbable Info` a2 on a2.id = c.id\r\nwhere o.id = ${officeId}\r\nand c.is_deleted=0\r\norder by ounder.hierarchy',NULL,0,1),(43,'Client Loans Listing - Additional','Table',NULL,'Client','select ounder.`name` as \"Office/Branch\", concat(c.lastname, if(c.firstname > \"\", concat(\", \", c.firstname), \"\")) as \"Name\",\r\np.name as Loan, st.enum_message_property as \"Status\", \r\nl.annual_nominal_interest_rate as \" Annual Nominal Interest Rate\", \r\nifnull(cur.display_symbol, l.currency_code) as Currency,  l.principal_amount,\r\nl1.`Business Location`, l1.`Business Location Other`, l1.`Business` as \"Business Type\", \r\nl1.`Business Description`, l1.`Business Title`\r\nfrom m_office o \r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\nleft join m_loan l on l.client_id = c.id\r\nleft join `m_loan_xAdditional Information` l1 on l1.id = l.id\r\nleft join m_product_loan p on p.id = l.product_id\r\nleft join r_enum_value st on st.enum_name = \'loan_status_id\' and st.enum_id = l.loan_status_id\r\nleft join m_currency cur on cur.code = l.currency_code\r\nwhere o.id = ${officeId}\r\norder by ounder.hierarchy, \"Name\" , l.id',NULL,0,1),(44,'Client Listing (Pentaho)','Pentaho',NULL,'Client',NULL,'Just a sample Pentaho report. \r\nNot production quality',0,1),(45,'Funds Disbursed Summary - Currency','Table',NULL,'Fund','select ifnull(f.`name`, \'-\') as Fund,  ifnull(cur.display_symbol, l.currency_code) as Currency, round(sum(l.principal_amount), 4) as disbursed_amount\r\nfrom m_loan l\r\njoin m_currency cur on cur.`code` = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\nwhere disbursedon_date between \'${startDate}\' and \'${endDate}\'\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\nand (l.currency_code = \'${currencyId}\' or \'-1\' = \'${currencyId}\')\r\ngroup by ifnull(f.`name`, \'-\') , ifnull(cur.display_symbol, l.currency_code)\r\norder by ifnull(f.`name`, \'-\') , ifnull(cur.display_symbol, l.currency_code)',NULL,0,1),(46,'Funds Disbursed Summary - Office, Currency','Table',NULL,'Fund','select ounder.`name` as \"Office/Branch\", ifnull(f.`name`, \'-\') as Fund,  ifnull(cur.display_symbol, l.currency_code) as Currency, round(sum(l.principal_amount), 4) as disbursed_amount\r\nfrom m_office o\r\njoin m_office ounder on ounder.hierarchy like concat(o.hierarchy, \'%\')\r\njoin m_client c on c.office_id = ounder.id\r\njoin m_loan l on l.client_id = c.id\r\njoin m_currency cur on cur.`code` = l.currency_code\r\nleft join m_fund f on f.id = l.fund_id\r\nwhere disbursedon_date between \'${startDate}\' and \'${endDate}\'\r\nand o.id = ${officeId}\r\nand (ifnull(l.fund_id, -10) = ${fundId} or -1 = ${fundId})\r\nand (l.currency_code = \'${currencyId}\' or \'-1\' = \'${currencyId}\')\r\ngroup by ounder.`name`,  ifnull(f.`name`, \'-\') , ifnull(cur.display_symbol, l.currency_code)\r\norder by ounder.`name`,  ifnull(f.`name`, \'-\') , ifnull(cur.display_symbol, l.currency_code)',NULL,0,1);
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
INSERT INTO `stretchy_report_parameter` VALUES (29,5,NULL),(30,5,NULL),(31,5,NULL),(32,5,NULL),(32,6,NULL),(34,5,NULL),(34,6,NULL),(35,5,NULL),(35,6,NULL),(38,5,NULL),(38,6,NULL),(39,5,NULL),(39,7,NULL),(40,5,NULL),(40,7,NULL),(41,5,NULL),(42,5,NULL),(43,5,NULL),(44,5,'officeId'),(45,6,NULL),(45,10,NULL),(45,80,NULL),(45,81,NULL),(46,5,NULL),(46,6,NULL),(46,10,NULL),(46,80,NULL),(46,81,NULL);
/*!40000 ALTER TABLE `stretchy_report_parameter` ENABLE KEYS */;
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
  `code_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `stretchydata_dataset_fields_fk1` (`dataset_id`,`name`),
  KEY `stretchydata_dataset_fields_fk2` (`code_id`),
  CONSTRAINT `stretchydata_dataset_fields_fk2` FOREIGN KEY (`code_id`) REFERENCES `m_code` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `stretchydata_dataset_fields_fk1` FOREIGN KEY (`dataset_id`) REFERENCES `stretchydata_dataset` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stretchydata_dataset_fields`
--

LOCK TABLES `stretchydata_dataset_fields` WRITE;
/*!40000 ALTER TABLE `stretchydata_dataset_fields` DISABLE KEYS */;
INSERT INTO `stretchydata_dataset_fields` VALUES (2,'Business Location','String',50,'List',8,7),(3,'Business Location Other','String',50,NULL,8,NULL),(4,'Business','String',10,'List',8,1),(5,'Business Description','Text',NULL,NULL,8,NULL),(6,'Ethnic Group','String',50,'List',1,3),(7,'Ethnic Group Other','String',50,NULL,1,NULL),(8,'Household Location','String',50,'List',1,7),(9,'Household Location Other','String',50,NULL,1,NULL),(10,'Religion','String',50,'List',1,8),(11,'Religion Other','String',50,NULL,1,NULL),(12,'Knowledge of Person','String',50,'List',1,6),(13,'Gender','String',10,'List',1,5),(14,'Business Title','String',100,NULL,8,NULL),(15,'Whois','Text',NULL,NULL,1,NULL),(60,'Fathers Favourite Team','String',50,'List',9,4),(61,'Mothers Favourite Team','String',50,'List',9,4),(62,'Fathers DOB','Date',NULL,NULL,9,NULL),(63,'Mothers DOB','Date',NULL,NULL,9,NULL),(64,'Fathers Education','String',50,'List',9,2),(65,'Mothers Education','String',50,'List',9,2),(67,'Number of Children','Integer',NULL,NULL,9,NULL),(68,'Favourite Town','String',30,NULL,9,NULL),(69,'Closing Comments','Text',NULL,NULL,9,NULL),(70,'Annual Family Income','Decimal',NULL,NULL,9,NULL);
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
INSERT INTO `stretchydata_datasettype` VALUES (1,'m_client'),(2,'m_loan');
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

-- Dump completed on 2012-09-02 12:14:11
