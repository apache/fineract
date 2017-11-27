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

-- Access Token Table

CREATE TABLE `twofactor_access_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `token` varchar(32) NOT NULL,
  `appuser_id` bigint(20) NOT NULL,
  `valid_from` datetime NOT NULL,
  `valid_to` datetime NOT NULL,
  `enabled` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_appuser_UNIQUE` (`token`,`appuser_id`),
  KEY `user` (`appuser_id`),
  KEY `token` (`token`),
  CONSTRAINT `fk_2fa_access_token_user_id` FOREIGN KEY (`appuser_id`) REFERENCES `m_appuser` (`id`)
);

-- Configuration

CREATE TABLE `twofactor_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(40) NOT NULL,
  `value` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_UNIQUE` (`name`)
);

INSERT INTO `twofactor_configuration` (`name`, `value`) VALUES
  ('otp-delivery-email-enable', 'true'),
  ('otp-delivery-email-subject', 'Fineract Two-Factor Authentication Token'),
  ('otp-delivery-email-body', 'Hello {{username}}.\nYour OTP login token is {{token}}.'),
  ('otp-delivery-sms-enable', 'false'),
  ('otp-delivery-sms-provider', '1'),
  ('otp-delivery-sms-text', 'Your authentication token for Fineract is {{token}}.'),
  ('otp-token-live-time', '300'),
  ('otp-token-length', '5'),
  ('access-token-live-time', '86400'),
  ('access-token-live-time-extended', '604800');


INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES
  ('authorisation', 'INVALIDATE_TWOFACTOR_ACCESSTOKEN', 'TWOFACTOR_ACCESSTOKEN', 'INVALIDATE', '0'),
  ('configuration', 'READ_TWOFACTOR_CONFIGURATION', 'TWOFACTOR_CONFIGURATION', 'READ', '0'),
  ('configuration', 'UPDATE_TWOFACTOR_CONFIGURATION', 'TWOFACTOR_CONFIGURATION', 'UPDATE', '0'),
  ('special', 'BYPASS_TWOFACTOR', NULL, NULL, '0');