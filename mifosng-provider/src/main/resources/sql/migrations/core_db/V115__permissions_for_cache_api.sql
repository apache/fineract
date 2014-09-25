DROP TABLE IF EXISTS `c_cache`;
CREATE TABLE `c_cache` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cache_type_enum` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO `c_cache`
(`id`,`cache_type_enum`)
VALUES
(1, 1);


INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'READ_CACHE', 'CACHE', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'UPDATE_CACHE', 'CACHE', 'UPDATE', 0);