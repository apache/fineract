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

CREATE TABLE `scheduled_email_campaign` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`campaign_name` VARCHAR(100) NOT NULL,
	`campaign_type` INT(11) NOT NULL,
	`businessRule_id` INT(11) NOT NULL,
	`param_value` TEXT NULL,
	`status_enum` INT(11) NOT NULL,
	`email_subject` VARCHAR(100) NOT NULL, 
	`email_message` TEXT NOT NULL,
	`email_attachment_file_format` VARCHAR(10) NOT NULL,
	`stretchy_report_id` INT(11) NOT NULL,
	`stretchy_report_param_map` TEXT NULL DEFAULT NULL,
	`closedon_date` DATE NULL DEFAULT NULL,
	`closedon_userid` BIGINT(20) NULL DEFAULT NULL,
	`submittedon_date` DATE NULL DEFAULT NULL,
	`submittedon_userid` BIGINT(20) NULL DEFAULT NULL,
	`approvedon_date` DATE NULL DEFAULT NULL,
	`approvedon_userid` BIGINT(20) NULL DEFAULT NULL,
	`recurrence` VARCHAR(100) NULL DEFAULT NULL,
	`next_trigger_date` DATETIME NULL DEFAULT NULL,
	`last_trigger_date` DATETIME NULL DEFAULT NULL,
	`recurrence_start_date` DATETIME NULL DEFAULT NULL,
	`is_visible` TINYINT(1) NULL DEFAULT '1',
	`previous_run_error_log` TEXT NULL DEFAULT NULL,
	`previous_run_error_message` TEXT NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `scheduled_email_campaign_ibfk_1` FOREIGN KEY (`stretchy_report_id`) REFERENCES `stretchy_report` (`id`)	
);

CREATE TABLE IF NOT EXISTS scheduled_email_messages_outbound (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `email_campaign_id` bigint(20) DEFAULT NULL,
  `status_enum` int(5) NOT NULL DEFAULT '100',
  `email_address` varchar(50) NOT NULL,
  `email_subject` varchar(50) NOT NULL,
  `message` text NOT NULL,
  `campaign_name` varchar(200) DEFAULT NULL,
  `submittedon_date` date,
  `error_message` text,
  PRIMARY KEY (`id`),
  KEY `SEFKGROUP000000001` (`group_id`),
  KEY `SEFKCLIENT00000001` (`client_id`),
  key `SEFKSTAFF000000001` (`staff_id`),
  CONSTRAINT `SEFKGROUP000000001` FOREIGN KEY (`group_id`) REFERENCES `m_group` (`id`),
  CONSTRAINT `SEFKCLIENT00000001` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `SEFKSTAFF000000001` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`),
  CONSTRAINT `fk_schedule_email_campign1` FOREIGN KEY (`email_campaign_id`) REFERENCES `scheduled_email_campaign` (`id`)
);

create table if not exists scheduled_email_configuration (
id int primary key auto_increment,
name varchar(50) not null,
`value` varchar(200) null,
constraint unique_name unique (name)
);

DELETE FROM `m_permission` WHERE `code`='READ_EMAIL';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'READ_EMAIL', 'EMAIL', 'READ', 0);

DELETE FROM `m_permission` WHERE `code`='CREATE_EMAIL';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'CREATE_EMAIL', 'EMAIL', 'CREATE', 0);

DELETE FROM `m_permission` WHERE `code`='CREATE_EMAIL_CHECKER';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'CREATE_EMAIL_CHECKER', 'EMAIL', 'CREATE_CHECKER', 0);

DELETE FROM `m_permission` WHERE `code`='UPDATE_EMAIL';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'UPDATE_EMAIL', 'EMAIL', 'UPDATE', 0);

DELETE FROM `m_permission` WHERE `code`='UPDATE_EMAIL_CHECKER';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'UPDATE_EMAIL_CHECKER', 'EMAIL', 'UPDATE_CHECKER', 0);

DELETE FROM `m_permission` WHERE `code`='DELETE_EMAIL';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'DELETE_EMAIL', 'EMAIL', 'DELETE', 0);

DELETE FROM `m_permission` WHERE `code`='DELETE_EMAIL_CHECKER';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'DELETE_EMAIL_CHECKER', 'EMAIL', 'DELETE_CHECKER', 0);

DELETE FROM `m_permission` WHERE `code`='READ_EMAIL_CAMPAIGN';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'READ_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'READ', 0);

DELETE FROM `m_permission` WHERE `code`='CREATE_EMAIL_CAMPAIGN';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'CREATE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'CREATE', 0);

DELETE FROM `m_permission` WHERE `code`='CREATE_EMAIL_CAMPAIGN_CHECKER';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'CREATE_EMAIL_CAMPAIGN_CHECKER', 'EMAIL_CAMPAIGN', 'CREATE_CHECKER', 0);

DELETE FROM `m_permission` WHERE `code`='UPDATE_EMAIL_CAMPAIGN';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'UPDATE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'UPDATE', 0);

DELETE FROM `m_permission` WHERE `code`='UPDATE_EMAIL_CAMPAIGN_CHECKER';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'UPDATE_EMAIL_CAMPAIGN_CHECKER', 'EMAIL_CAMPAIGN', 'UPDATE_CHECKER', 0);

DELETE FROM `m_permission` WHERE `code`='DELETE_EMAIL_CAMPAIGN';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'DELETE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'DELETE', 0);

DELETE FROM `m_permission` WHERE `code`='DELETE_EMAIL_CAMPAIGN_CHECKER';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'DELETE_EMAIL_CAMPAIGN_CHECKER', 'EMAIL_CAMPAIGN', 'DELETE_CHECKER', 0);

DELETE FROM `m_permission` WHERE `code`='CLOSE_EMAIL_CAMPAIGN';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'CLOSE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'CLOSE', 0);

DELETE FROM `m_permission` WHERE `code`='ACTIVATE_EMAIL_CAMPAIGN';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'ACTIVATE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'ACTIVATE', 0);

DELETE FROM `m_permission` WHERE `code`='REACTIVATE_EMAIL_CAMPAIGN';
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'REACTIVATE_EMAIL_CAMPAIGN', 'EMAIL_CAMPAIGN', 'REACTIVATE', 0);


INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('organisation', 'READ_EMAIL_CONFIGURATION', 'EMAIL_CONFIGURATION', 'READ', 0),
('organisation', 'UPDATE_EMAIL_CONFIGURATION', 'EMAIL_CONFIGURATION', 'UPDATE', 0);

Alter table m_client
ADD Column email_address varchar(150);

Alter table m_staff
ADD Column email_address varchar(150);


insert into job (name, display_name, cron_expression, create_time)
values ('Execute Email', 'Execute Email', '0 0/10 * * * ?', NOW());

insert into job (name, display_name, cron_expression, create_time)
values ('Update Email Outbound with campaign message', 'Update Email Outbound with campaign message', '0 0/15 * * * ?', NOW());

INSERT INTO `scheduled_email_configuration` (`name`)
VALUES ('SMTP_SERVER'),
('SMTP_PORT'),('SMTP_USERNAME'), ('SMTP_PASSWORD');

