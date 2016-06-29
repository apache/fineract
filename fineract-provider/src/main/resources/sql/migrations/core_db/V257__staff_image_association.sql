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

ALTER TABLE `m_staff` 
	ADD COLUMN `image_id` BIGINT(20) NULL,
	ADD CONSTRAINT `FK_m_staff_m_image` FOREIGN KEY (`image_id`) REFERENCES `m_image` (`id`);

INSERT INTO m_permission (
grouping ,
code ,
entity_name ,
action_name ,
can_maker_checker
) VALUES
('portfolio', 'READ_STAFFIMAGE', 'STAFFIMAGE', 'READ', '0'),
('portfolio', 'CREATE_STAFFIMAGE', 'STAFFIMAGE', 'CREATE', '1'),
('portfolio', 'CREATE_STAFFIMAGE_CHECKER', 'STAFFIMAGE', 'CREATE', '0'),
('portfolio', 'DELETE_STAFFIMAGE', 'STAFFIMAGE', 'DELETE', '1'),
('portfolio', 'DELETE_STAFFIMAGE_CHECKER', 'STAFFIMAGE', 'DELETE', '0');