DROP TABLE if exists `m_acc_coa`;
CREATE TABLE `m_acc_coa` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `gl_code` varchar(45) NOT NULL,
  `is_disabled` tinyint(1) NOT NULL DEFAULT '0',
  `manual_entries_allowed` tinyint(1) NOT NULL DEFAULT '0',
  `category` varchar(45) NOT NULL,
  `ledger_type` varchar(45) NOT NULL,
  `description` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `acc_gl_code` (`gl_code`),
  KEY `FK_ACC_0000000001` (`parent_id`),
  CONSTRAINT `FK_ACC_0000000001` FOREIGN KEY (`parent_id`) REFERENCES `m_acc_coa` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

