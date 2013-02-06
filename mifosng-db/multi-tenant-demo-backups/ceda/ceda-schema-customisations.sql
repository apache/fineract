DELETE FROM `mifostenant-ceda`.`ref_loan_transaction_processing_strategy` WHERE id in (1, 3, 4);

INSERT INTO `mifostenant-ceda`.`m_code` (`id`,`code_name`, `is_system_defined`)
VALUES
(2,'Gender', '0'),
(3,'YesNo', '0'),
(4,'FieldOfEmployment', '0'),
(5,'EducationLevel', '0'),
(6,'MaritalStatus', '0'),
(7,'PovertyStatus', '0'),
(8,'PurposeOfLoan', '0'),
(9,'CollateralType', '0');

DELETE FROM `mifostenant-ceda`.`m_code_value` WHERE id > 0;

INSERT INTO `mifostenant-ceda`.`m_code_value`
(`code_id`,`code_value`,`order_position`)
VALUES
(1, 'Passport', 1),
(1, 'Id', 2),
(1, 'Drivers License', 3),
(1, 'Any Other Id Type', 4),
(2, 'gender.Male', 1),
(2, 'gender.Female', 2),
(3, 'option.Yes', 1),
(3, 'option.No', 2),
(4, 'option.Banker', 1),
(4, 'option.SoftwareDeveloper', 2),
(5, 'option.University', 1),
(5, 'option.Secondary', 2),
(5, 'option.Primary', 3),
(6, 'option.Married', 1),
(6, 'option.Single', 2),
(6, 'option.Divorced', 3),
(6, 'option.Widow', 4),
(7, 'option.PovertyStatus.Band1', 1),
(7, 'option.PovertyStatus.Band2', 2),
(7, 'option.PovertyStatus.Band3', 3),
(8, 'option.Agriculture', 1),
(8, 'option.Manufacturing', 2),
(8, 'option.HousingImprovement', 3),
(9, 'option.House', 1),
(9, 'option.Television', 2),
(9, 'option.Gold', 3);

DROP TABLE IF EXISTS `mifostenant-ceda`.`client additional data`;
CREATE TABLE `mifostenant-ceda`.`client additional data` (
  `client_id` bigint(20) NOT NULL,
  `Gender_cd` int(11) NOT NULL,
  `Date of Birth` date NOT NULL,
  `Home address` text NOT NULL,
  `Telephone number` varchar(20) NOT NULL,
  `Telephone number (2nd)` varchar(20) NOT NULL,
  `Email address` varchar(50) NOT NULL,
  `EducationLevel_cd` int(11) NOT NULL,
  `MaritalStatus_cd` int(11) NOT NULL,
  `Number of children` int(11) NOT NULL,
  `Citizenship` varchar(50) NOT NULL,
  `PovertyStatus_cd` int(11) NOT NULL,
  `YesNo_cd_Employed` int(11) NOT NULL,
  `FieldOfEmployment_cd_Field of employment` int(11) DEFAULT NULL,
  `Employer name` varchar(50) DEFAULT NULL,
  `Number of years` int(11) DEFAULT NULL,
  `Monthly salary` decimal(19,6) DEFAULT NULL,
  `YesNo_cd_Self employed` int(11) NOT NULL,
  `FieldOfEmployment_cd_Field of self-employment` int(11) DEFAULT NULL,
  `Business address` text,
  `Number of employees` int(11) DEFAULT NULL,
  `Monthly salaries paid` decimal(19,6) DEFAULT NULL,
  `Monthly net income of business activity` decimal(19,6) DEFAULT NULL,
  `Monthly rent` decimal(19,6) DEFAULT NULL,
  `Other income generating activities` varchar(100) DEFAULT NULL,
  `YesNo_cd_Bookkeeping` int(11) DEFAULT NULL,
  `YesNo_cd_Loans with other institutions` int(11) NOT NULL,
  `From whom` varchar(100) DEFAULT NULL,
  `Amount` decimal(19,6) DEFAULT NULL,
  `Interest rate pa` decimal(19,6) DEFAULT NULL,
  `Number of people depending on overal income` int(11) NOT NULL,
  `YesNo_cd_Bank account` int(11) NOT NULL,
  `YesNo_cd_Business plan provided` int(11) NOT NULL,
  `YesNo_cd_Access to internet` int(11) DEFAULT NULL,
  `Introduced by` varchar(100) DEFAULT NULL,
  `Known to introducer since` date NOT NULL,
  `Last visited by` varchar(100) DEFAULT NULL,
  `Last visited on` date NOT NULL,
  PRIMARY KEY (`client_id`),
  CONSTRAINT `FK_client_additional_data` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `mifostenant-ceda`.`impact measurement`;
CREATE TABLE `mifostenant-ceda`.`impact measurement` (
  `loan_id` bigint(20) NOT NULL,
  `YesNo_cd_RepaidOnSchedule` int(11) NOT NULL,
  `ReasonNotRepaidOnSchedule` varchar(200) DEFAULT NULL,
  `How was Loan Amount Invested` varchar(200) NOT NULL,
  `Additional Income Generated` decimal(19,6) NOT NULL,
  `Additional Income Used For` text NOT NULL,
  `YesNo_cd_NewJobsCreated` int(11) NOT NULL,
  `Number of Jobs Created` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `FK_impact measurement` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `mifostenant-ceda`.`loan additional data`;
CREATE TABLE `mifostenant-ceda`.`loan additional data` (
  `loan_id` bigint(20) NOT NULL,
  `PurposeOfLoan_cd` int(11) NOT NULL,
  `CollateralType_cd` int(11) NOT NULL,
  `Collateral notes` text NOT NULL,
  `YesNo_cd_Guarantor` int(11) NOT NULL,
  `Guarantor name` varchar(100) DEFAULT NULL,
  `Guarantor relation` varchar(100) DEFAULT NULL,
  `Guarantor address` varchar(100) DEFAULT NULL,
  `Guarantor telephone number` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `FK_loan_additional_data` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;