CREATE TABLE `portfolio_group_client` (
  `group_id` bigint(20) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  PRIMARY KEY (`group_id`,`client_id`),
  FOREIGN KEY (`group_id`) REFERENCES `portfolio_group` (`id`),
  FOREIGN KEY (`client_id`) REFERENCES `portfolio_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

