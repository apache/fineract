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

CREATE TABLE `client_device_registration` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT(20) NOT NULL,
	`updatedon_date` DATETIME NOT NULL,
	`registration_id` VARCHAR(255) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `registration_key_client_device_registration` (`registration_id`),
	UNIQUE INDEX `client_key_client_device_registration` (`client_id`),
	INDEX `FK1_client_device_registration_m_client` (`client_id`),
	CONSTRAINT `FK1_client_device_registration_m_client` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
);

INSERT INTO `c_external_service` (`name`) VALUES ('NOTIFICATION');

INSERT INTO `c_external_service_properties` (`name`, `value`, `external_service_id`) VALUES ('server_key', 
'AAAAToBmqQQ:APA91bEodkE12CwFl8VHqanUbeJYg1E05TiheVz59CZZYrBnCq3uM40UYhHfdP-JfeTQ0L0zoLqS8orjvW_ze0_VF8DSuyyqkrDibflhtUainsI0lwgVz5u1YP3q3c3erqjlySEuRShS', 
(select id from `c_external_service` where name ='NOTIFICATION')
),('gcm_end_point','https://gcm-http.googleapis.com/gcm/send',(select id from `c_external_service` where name ='NOTIFICATION')
),('fcm_end_point','https://fcm.googleapis.com/fcm/send',(select id from `c_external_service` where name ='NOTIFICATION')
);

ALTER TABLE sms_campaign 
MODIFY COLUMN provider_id BIGINT(20) NULL DEFAULT NULL,
ADD COLUMN is_notification TINYINT(1) NULL DEFAULT '0'; 

ALTER TABLE sms_messages_outbound 
MODIFY COLUMN mobile_no VARCHAR(50) NULL DEFAULT NULL,
ADD COLUMN is_notification TINYINT(1) NOT NULL DEFAULT '0'

