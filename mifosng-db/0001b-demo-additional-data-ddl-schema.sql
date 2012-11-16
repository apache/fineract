DROP TABLE IF EXISTS `additional client fields data`;
DROP TABLE IF EXISTS `extra family details data`;
DROP TABLE IF EXISTS `additional loan fields data`;

CREATE TABLE `additional client fields data` (
  `client_id` bigint(20) NOT NULL,
  `Business Description` varchar(100) DEFAULT NULL,
  `Years in Business` int(11) DEFAULT NULL,
  `Gender_cd` int(11) DEFAULT NULL,
  `Education_cv` varchar(60) DEFAULT NULL,
  `Next Visit` date DEFAULT NULL,
  `Highest Rate Paid` decimal(19,6) DEFAULT NULL,
  `Comment` text,
  PRIMARY KEY (`client_id`),
  CONSTRAINT `FK_Additional Client Fields Data_1` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `extra family details data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `Name` varchar(40) DEFAULT NULL,
  `Date of Birth` date DEFAULT NULL,
  `Points Score` int(11) DEFAULT NULL,
  `Education_cdHighest` int(11) DEFAULT NULL,
  `Other Notes` text,
  PRIMARY KEY (`id`),
  KEY `FK_Extra Family Details Data_1` (`client_id`),
  CONSTRAINT `FK_Extra Family Details Data_1` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE `additional loan fields data` (
  `loan_id` bigint(20) NOT NULL,
  `Business Description` varchar(100) DEFAULT NULL,
  `Years in Business` int(11) DEFAULT NULL,
  `Gender_cd` int(11) DEFAULT NULL,
  `Education_cv` varchar(60) DEFAULT NULL,
  `Next Visit` date DEFAULT NULL,
  `Highest Rate Paid` decimal(19,6) DEFAULT NULL,
  `Comment` text,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `FK_Additional Loan Fields Data_1` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;