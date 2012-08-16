CREATE TABLE `r_code` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_name` (`code_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `r_code_value` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code_id` int NOT NULL,
  `code_value` varchar(100) DEFAULT NULL,
  `order_position` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_value` (`code_id`, `code_value`),
  KEY `FKCFCEA42640BE071Z` (`code_id`),
  CONSTRAINT `FKCFCEA42640BE071Z` FOREIGN KEY (`code_id`) REFERENCES `r_code` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*not a major table - just intended for database reporting use for enums and values that would be hidden in java*/
CREATE TABLE `r_enum_value` (
  `enum_name` varchar(100) NOT NULL,
  `enum_id` int NOT NULL,
  `enum_message_property` varchar(100) NOT NULL,
  `enum_value` varchar(100) NOT NULL,
  PRIMARY KEY (`enum_name`, `enum_id`),
  UNIQUE KEY `enum_message_property` (`enum_name`, `enum_message_property`),
  UNIQUE KEY `enum_value` (`enum_name`, `enum_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;