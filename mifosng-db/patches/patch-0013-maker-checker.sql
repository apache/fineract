drop table `m_maker_checker`;
CREATE TABLE `m_maker_checker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_name` varchar(100) NOT NULL,
  `task_json` varchar(1000) NOT NULL,
  `maker_id` bigint(20) NOT NULL,
  `made_on_date` date NOT NULL,
  `checker_id` bigint(20) DEFAULT NULL,
  `checked_on_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_maker_m_appuser` (`maker_id`),
  KEY `FK_m_checker_m_appuser` (`checker_id`),
  CONSTRAINT `FK_m_maker_m_appuser` FOREIGN KEY (`maker_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `FK_m_checker_m_appuser` FOREIGN KEY (`checker_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;