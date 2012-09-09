
/* used to link MySql tables to Mifos X application tables for additional data needs */

DROP TABLE `x_registered_table`;

CREATE TABLE `x_registered_table` (
  `registered_table_name` varchar(50) NOT NULL,
  `registered_table_label` varchar(80) NOT NULL,
  `application_table_name` varchar(50) NOT NULL,
  PRIMARY KEY (`registered_table_name`),
  UNIQUE KEY `registered_table_label` (`registered_table_label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;