DROP TABLE IF EXISTS `c_configuration`;
CREATE TABLE `c_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `c_configuration`
(`name`, `enabled`)
VALUES 
('maker-checker', 0);

INSERT INTO `m_permission`
(
`grouping`,
`code`,
`entity_name`,
`action_name`,
`can_maker_checker`)
VALUES
('configuration','UPDATE_CONFIGURATION',
'CONFIGURATION','UPDATE',1),
('configuration','READ_CONFIGURATION',
'CONFIGURATION','READ',1);