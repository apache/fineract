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


CREATE TABLE `m_creditbureau_token` (
  `id` INT(128) NOT NULL AUTO_INCREMENT,
  `username` varchar(128) DEFAULT NULL,
  `token` MEDIUMTEXT DEFAULT NULL,
  `tokenType` varchar(128) DEFAULT NULL,
  `expiresIn` varchar(128) DEFAULT NULL,
  `issued` varchar(128) DEFAULT NULL,
  `expiryDate` DATE DEFAULT NULL,
   PRIMARY KEY (`id`)
);

CREATE TABLE `m_creditreport` (
  `id` INT(128) NOT NULL AUTO_INCREMENT,
  `creditBureauId` BIGINT(128) DEFAULT NULL,
  `nationalId` varchar(128) DEFAULT NULL,
  `creditReports` BLOB DEFAULT NULL,
   PRIMARY KEY (`id`),
   CONSTRAINT `cbId` FOREIGN KEY (`creditBureauId`) REFERENCES `m_creditbureau` (`id`)
);


ALTER TABLE m_creditbureau_configuration
DROP FOREIGN KEY cbConfigfk1;

ALTER TABLE m_creditbureau_configuration
ADD CONSTRAINT cbConfigfk1
FOREIGN KEY (organisation_creditbureau_id) REFERENCES `m_creditbureau` (`id`);

ALTER TABLE m_creditbureau_configuration MODIFY COLUMN value longtext;

ALTER TABLE m_organisation_creditbureau CHANGE is_active isActive TINYINT(4) NOT NULL;
ALTER TABLE m_creditbureau_loanproduct_mapping CHANGE is_active isActive TINYINT(4) NULL;

-- Integrated credit bureau added

INSERT INTO `m_creditbureau` (`id`, `name`, `product`, `country`, `implementationKey`) VALUES ('1', 'THITSAWORKS', '1', 'Myanmar', '1');


-- permissions added

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'GET_CREDITREPORT', 'CREDITREPORT', 'GET', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'CREATE_CREDITBUREAU_CONFIGURATION', 'CREDITBUREAU_CONFIGURATION', 'CREATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'UPDATE_CREDITBUREAU_CONFIGURATION', 'CREDITBUREAU_CONFIGURATION', 'UPDATE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'SAVE_CREDITREPORT', 'CREDITREPORT', 'SAVE', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('configuration', 'DELETE_CREDITREPORT', 'CREDITREPORT', 'DELETE', 0);

-- configkeys added

INSERT INTO `m_creditbureau_configuration` (`id`, `configkey`, `value`, `organisation_creditbureau_id`, `description`) VALUES ('1', 'Password', '', '1', '');
INSERT INTO `m_creditbureau_configuration` (`id`, `configkey`, `value`, `organisation_creditbureau_id`, `description`) VALUES ('2', 'SubscriptionId', '', '1', '');
INSERT INTO `m_creditbureau_configuration` (`id`, `configkey`, `value`, `organisation_creditbureau_id`, `description`) VALUES ('3', 'SubscriptionKey', '', '1', '');
INSERT INTO `m_creditbureau_configuration` (`id`, `configkey`, `value`, `organisation_creditbureau_id`, `description`) VALUES ('4', 'Username', '', '1', '');
INSERT INTO `m_creditbureau_configuration` (`id`, `configkey`, `value`, `organisation_creditbureau_id`, `description`) VALUES ('5', 'tokenurl', '', '1', '');
INSERT INTO `m_creditbureau_configuration` (`id`, `configkey`, `value`, `organisation_creditbureau_id`, `description`) VALUES ('6', 'searchurl', '', '1', '');
INSERT INTO `m_creditbureau_configuration` (`id`, `configkey`, `value`, `organisation_creditbureau_id`, `description`) VALUES ('7', 'creditReporturl', '', '1', '');
INSERT INTO `m_creditbureau_configuration` (`id`, `configkey`, `value`, `organisation_creditbureau_id`, `description`) VALUES ('8', 'addCreditReporturl', '', '1', '');
