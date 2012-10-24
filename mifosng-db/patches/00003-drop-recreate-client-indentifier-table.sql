drop table `m_client_identifier`;

CREATE TABLE `m_client_identifier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `document_type_id` int(11) NOT NULL,
  `document_key` varchar(50) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_identifier_key` (`document_type_id`, `document_key`),
  UNIQUE KEY `unique_client_identifier` (`client_id`,`document_type_id`),
  KEY `FK_m_client_document_m_client` (`client_id`),
  KEY `FK_m_client_document_m_code_value` (`document_type_id`),
  CONSTRAINT `FK_m_client_document_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `FK_m_client_document_m_code_value` FOREIGN KEY (`document_type_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;