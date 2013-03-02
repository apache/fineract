CREATE TABLE IF NOT EXISTS `m_calendar` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_id` bigint(20) NOT NULL DEFAULT '0',
  `entity_type_enum` smallint(5) NOT NULL DEFAULT '0',
  `title` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `location` varchar(50) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `duration` smallint(6) DEFAULT NULL,
  `calendar_type_enum` smallint(5) NOT NULL,
  `repeating` tinyint(1) NOT NULL DEFAULT '0',
  `recurrence` varchar(100) NOT NULL,
  `remind_by_enum` smallint(5) DEFAULT NULL,
  `first_reminder` smallint(11) NOT NULL,
  `second_reminder` smallint(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
