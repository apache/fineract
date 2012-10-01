DROP TABLE IF EXISTS `x_registered_table`;
/* used to link MySql tables to Mifos X application tables for additional data needs */
CREATE TABLE `x_registered_table` (
  `registered_table_name` varchar(50) NOT NULL,
  `application_table_name` varchar(50) NOT NULL,
  PRIMARY KEY (`registered_table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
