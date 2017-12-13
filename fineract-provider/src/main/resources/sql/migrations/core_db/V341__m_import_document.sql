--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

CREATE TABLE `m_import_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `document_id` int(20) NOT NULL,
  `import_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `entity_type` tinyint(3) NOT NULL,
  `completed` tinyint(2) DEFAULT 0,
  `total_records` bigint(20) DEFAULT 0,
  `success_count` bigint(20) DEFAULT 0,
  `failure_count` bigint(20) DEFAULT 0,
  `createdby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `import_document_id` (`document_id`),
  CONSTRAINT `FK_m_import_m_document` FOREIGN KEY (`document_id`) REFERENCES `m_document` (`id`),
  CONSTRAINT `FK_m_import_m_appuser` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`)
);

INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES
('infrastructure','READ_IMPORT','IMPORT','READ', 0);