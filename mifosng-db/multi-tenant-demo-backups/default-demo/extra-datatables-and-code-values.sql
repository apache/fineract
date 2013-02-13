-- add code and code values for datatables dropdowns

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Male', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Gender";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Female', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Gender";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.Yes', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "YesNo";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'option.No', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "YesNo";

INSERT INTO `m_code`
(`code_name`, `is_system_defined`) 
VALUES 
('Education',1);

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Primary', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Education";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Secondary', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Education";

INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'University', ifnull(max(mv.id), 1)
from m_code mc
join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "Education";

-- ========= datatables=======

DROP TABLE IF EXISTS `extra_client_details`;
CREATE TABLE `extra_client_details` (
  `client_id` bigint(20) NOT NULL,
  `Business Description` varchar(100) DEFAULT NULL,
  `Years in Business` int(11) DEFAULT NULL,
  `Gender_cd` int(11) DEFAULT NULL,
  `Education_cv` varchar(60) DEFAULT NULL,
  `Next Visit` date DEFAULT NULL,
  `Highest Rate Paid` decimal(19,6) DEFAULT NULL,
  `Comment` text,
  PRIMARY KEY (`client_id`),
  CONSTRAINT `FK_extra_client_details` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `extra_family_details`;
CREATE TABLE `extra_family_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `Name` varchar(40) DEFAULT NULL,
  `Date of Birth` date DEFAULT NULL,
  `Points Score` int(11) DEFAULT NULL,
  `Education_cd_Highest` int(11) DEFAULT NULL,
  `Other Notes` text,
  PRIMARY KEY (`id`),
  KEY `FK_Extra Family Details Data_1` (`client_id`),
  CONSTRAINT `FK_family_details` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `extra_loan_details`;
CREATE TABLE `extra_loan_details` (
  `loan_id` bigint(20) NOT NULL,
  `Business Description` varchar(100) DEFAULT NULL,
  `Years in Business` int(11) DEFAULT NULL,
  `Gender_cd` int(11) DEFAULT NULL,
  `Education_cv` varchar(60) DEFAULT NULL,
  `Next Visit` date DEFAULT NULL,
  `Highest Rate Paid` decimal(19,6) DEFAULT NULL,
  `Comment` text,
  PRIMARY KEY (`loan_id`),
  CONSTRAINT `FK_extra_loan_details` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- datatables mapping
INSERT INTO `x_registered_table`
(`registered_table_name`,`application_table_name`)
VALUES
('extra_client_details', 'm_client'),
('extra_family_details', 'm_loan'),
('extra_loan_details', 'm_loan');