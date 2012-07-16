-- example of a DDL script to support customised 'additional data' for clients and loans.

DROP TABLE IF EXISTS `portfolio_client_extra_additional information`;
DROP TABLE IF EXISTS `portfolio_client_extra_highly improbable info`;
DROP TABLE IF EXISTS `portfolio_loan_extra_additional information`;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `portfolio_loan_extra_Additional Information` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Business Location` varchar(50) DEFAULT NULL,
  `Business Location Other` varchar(50) DEFAULT NULL,
  `Business` varchar(10) DEFAULT NULL,
  `Business Description` mediumtext,
  `Business Title` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `portfolio_loan_extra_Additional Information_fk1` FOREIGN KEY (`id`) REFERENCES `portfolio_loan` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;