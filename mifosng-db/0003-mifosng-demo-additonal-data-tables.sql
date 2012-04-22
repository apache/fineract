
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
--
-- Dumping data for table `stretchydata_allowed_list`
--
LOCK TABLES `stretchydata_allowed_list` WRITE;
/*!40000 ALTER TABLE `stretchydata_allowed_list` DISABLE KEYS */;
INSERT INTO `stretchydata_allowed_list` VALUES (2,'Business'),(8,'Education'),(3,'Ethnic Group'),(7,'Football Team'),(4,'Gender'),(5,'Knowledge of Person'),(1,'Location'),(6,'Religion');
/*!40000 ALTER TABLE `stretchydata_allowed_list` ENABLE KEYS */;
UNLOCK TABLES;
--
-- Dumping data for table `stretchydata_allowed_value`
--
LOCK TABLES `stretchydata_allowed_value` WRITE;
/*!40000 ALTER TABLE `stretchydata_allowed_value` DISABLE KEYS */;
INSERT INTO `stretchydata_allowed_value` VALUES (1,'Bamako',1),(2,'Bla',1),(3,'Koutiala',1),(6,'Other',1),(4,'San',1),(5,'Segou',1),(8,'Existing',2),(7,'New',2),(15,'Bambara',3),(16,'Bobo',3),(18,'Fulani',3),(17,'Minianka',3),(19,'Other',3),(20,'Unknown',3),(21,'Female',4),(22,'Male',4),(26,'Friend of staff member',5),(27,'Not known by any staff member',5),(28,'Other',5),(25,'Son/daughter of staff member',5),(24,'Spouse of staff member',5),(23,'Staff member',5),(32,'Animist',6),(33,'Atheist',6),(30,'Catholic',6),(31,'Muslim',6),(34,'Other',6),(29,'Protestant',6),(35,'Unknown',6),(36,'AC Milan',7),(37,'Juventus',7),(39,'Manchester Utd',7),(45,'None, hates soccer',7),(38,'Sao Paulo',7),(44,'None',8),(40,'Primary',8),(41,'Secondary',8),(42,'Tertiary',8),(43,'Trade',8);
/*!40000 ALTER TABLE `stretchydata_allowed_value` ENABLE KEYS */;
UNLOCK TABLES;
--
-- Dumping data for table `stretchydata_dataset`
--
LOCK TABLES `stretchydata_dataset` WRITE;
/*!40000 ALTER TABLE `stretchydata_dataset` DISABLE KEYS */;
INSERT INTO `stretchydata_dataset` VALUES (1,'Additional Information',1),(9,'Highly Improbable Info',1),(8,'Additional Information',2);
/*!40000 ALTER TABLE `stretchydata_dataset` ENABLE KEYS */;
UNLOCK TABLES;
--
-- Dumping data for table `stretchydata_dataset_fields`
--
LOCK TABLES `stretchydata_dataset_fields` WRITE;
/*!40000 ALTER TABLE `stretchydata_dataset_fields` DISABLE KEYS */;
INSERT INTO `stretchydata_dataset_fields` VALUES (2,'Business Location','String',50,'List',8,1),(3,'Business Location Other','String',50,NULL,8,NULL),(4,'Business','String',10,'List',8,2),(5,'Business Description','Text',NULL,NULL,8,NULL),(6,'Ethnic Group','String',50,'List',1,3),(7,'Ethnic Group Other','String',50,NULL,1,NULL),(8,'Household Location','String',50,'List',1,1),(9,'Household Location Other','String',50,NULL,1,NULL),(10,'Religion','String',50,'List',1,6),(11,'Religion Other','String',50,NULL,1,NULL),(12,'Knowledge of Person','String',50,'List',1,5),(13,'Gender','String',10,'List',1,4),(14,'Business Title','String',100,NULL,8,NULL),(15,'Whois','Text',NULL,NULL,1,NULL),(60,'Fathers Favourite Team','String',50,'List',9,7),(61,'Mothers Favourite Team','String',50,'List',9,7),(62,'Fathers DOB','Date',NULL,NULL,9,NULL),(63,'Mothers DOB','Date',NULL,NULL,9,NULL),(64,'Fathers Education','String',50,'List',9,8),(65,'Mothers Education','String',50,'List',9,8),(67,'Number of Children','Integer',NULL,NULL,9,NULL),(68,'Favourite Town','String',30,NULL,9,NULL),(69,'Closing Comments','Text',NULL,NULL,9,NULL),(70,'Annual Family Income','Decimal',NULL,NULL,9,NULL);
/*!40000 ALTER TABLE `stretchydata_dataset_fields` ENABLE KEYS */;
UNLOCK TABLES;
--
-- Dumping data for table `stretchydata_datasettype`
--
LOCK TABLES `stretchydata_datasettype` WRITE;
/*!40000 ALTER TABLE `stretchydata_datasettype` DISABLE KEYS */;
INSERT INTO `stretchydata_datasettype` VALUES (1,'portfolio_client'),(2,'portfolio_loan');
/*!40000 ALTER TABLE `stretchydata_datasettype` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;