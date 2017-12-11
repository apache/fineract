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

CREATE TABLE IF NOT EXISTS `topic` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `enabled` TINYINT(1) NULL,
  `entity_id` BIGINT(20) NOT NULL,
  `entity_type` TEXT NOT NULL,
  `member_type` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `title_UNIQUE` (`title` ASC)
)ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `topic_subscriber` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `topic_id` BIGINT(20) NOT NULL,
  `user_id` BIGINT(20) NOT NULL,
  `subscription_date` DATE NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_topic_has_m_appuser_topic` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`),
  CONSTRAINT `fk_topic_has_m_appuser_m_appuser1` FOREIGN KEY (`user_id`) REFERENCES `m_appuser` (`id`)
) ENGINE = InnoDB;

INSERT INTO topic (enabled, entity_type, entity_id, member_type, title) SELECT true, 'OFFICE', o.id as entity_id, UPPER(r.name) as member_type, CONCAT(r.name, ' of ', o.name) as title FROM m_office o, m_role r WHERE o.parent_id IS NULL AND CONCAT(r.name, ' of ', o.name) NOT IN (SELECT title FROM topic);

INSERT INTO topic (enabled, entity_type, entity_id, member_type, title) SELECT true, 'BRANCH', o.id as entity_id, UPPER(r.name) as member_type, CONCAT(r.name, ' of ', o.name) as title FROM m_office o, m_role r WHERE o.parent_id IS NOT NULL AND CONCAT(r.name, ' of ', o.name) NOT IN (SELECT title FROM topic);

INSERT INTO topic_subscriber( user_id, topic_id, subscription_date ) SELECT u.id AS user_id, t.id AS topic_id, NOW() FROM topic t, m_appuser u, m_appuser_role ur, m_role r WHERE u.office_id = t.entity_id AND u.id = ur.appuser_id AND ur.role_id = r.id AND r.name = t.member_type AND NOT EXISTS (SELECT user_id, topic_id FROM topic_subscriber WHERE user_id = u.id AND topic_id = t.id);
