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

-- -----------------------------------------------------
-- Table `m_hook_templates`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `m_hook_templates` (
  `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB DEFAULT CHARSET=utf8;


-- -----------------------------------------------------
-- Table `m_hook`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `m_hook` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `template_id` SMALLINT(6) NOT NULL,
  `is_active` SMALLINT(3) NOT NULL DEFAULT 1,
  `name` VARCHAR(45) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_template_id_idx` (`template_id` ASC),
  CONSTRAINT `fk_template_id`
    FOREIGN KEY (`template_id`)
    REFERENCES `m_hook_templates` (`id`))
ENGINE = InnoDB DEFAULT CHARSET=utf8;


-- -----------------------------------------------------
-- Table `m_hook_schema`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `m_hook_schema` (
  `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
  `hook_template_id` SMALLINT(6) NOT NULL,
  `field_type` VARCHAR(45) NOT NULL,
  `field_name` VARCHAR(100) NOT NULL,
  `placeholder` VARCHAR(100) DEFAULT NULL,
  `optional` TINYINT(3) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `fk_hook_template_id_idx` (`hook_template_id` ASC),
  CONSTRAINT `fk_hook_template_id`
    FOREIGN KEY (`hook_template_id`)
    REFERENCES `m_hook_templates` (`id`))
ENGINE = InnoDB DEFAULT CHARSET=utf8;


-- -----------------------------------------------------
-- Table `m_hook_registered_events`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `m_hook_registered_events` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `hook_id` BIGINT(20) NOT NULL,
  `entity_name` VARCHAR(45) NOT NULL,
  `action_name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_hook_id_idx` (`hook_id` ASC),
  CONSTRAINT `fk_hook_idc`
    FOREIGN KEY (`hook_id`)
    REFERENCES `m_hook` (`id`))
ENGINE = InnoDB DEFAULT CHARSET=utf8;


-- -----------------------------------------------------
-- Table `m_hook_configuration`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `m_hook_configuration` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `hook_id` BIGINT(20) NULL,
  `field_type` VARCHAR(45) NOT NULL,
  `field_name` VARCHAR(100) NOT NULL,
  `field_value` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_hook_id_idx` (`hook_id` ASC),
  CONSTRAINT `fk_hook_id_cfg`
    FOREIGN KEY (`hook_id`)
    REFERENCES `m_hook` (`id`))
ENGINE = InnoDB DEFAULT CHARSET=utf8;

insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'CREATE_HOOK', 'HOOK', 'CREATE', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'READ_HOOK', 'HOOK', 'READ', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'UPDATE_HOOK', 'HOOK', 'UPDATE', 0);
insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES ('configuration', 'DELETE_HOOK', 'HOOK', 'DELETE', 0);

insert into m_hook_templates values(1, "Web");
insert into m_hook_templates values(2, "SMS Bridge");

INSERT INTO `m_hook_schema` (`hook_template_id`, `field_type`, `field_name`, `optional`)
VALUES (1, 'string', 'Payload URL', 0);
INSERT INTO `m_hook_schema` (`hook_template_id`, `field_type`, `field_name`, `placeholder`, `optional`)
VALUES (1, 'string', 'Content Type', 'json / form', 0);
INSERT INTO `m_hook_schema` (`hook_template_id`, `field_type`, `field_name`, `optional`)
VALUES (2, 'string', 'Payload URL', 0);
INSERT INTO `m_hook_schema` (`hook_template_id`, `field_type`, `field_name`, `optional`)
VALUES (2, 'string', 'SMS Provider', 0);
INSERT INTO `m_hook_schema` (`hook_template_id`, `field_type`, `field_name`, `optional`)
VALUES (2, 'string', 'Phone Number', 0);
INSERT INTO `m_hook_schema` (`hook_template_id`, `field_type`, `field_name`, `optional`)
VALUES (2, 'string', 'SMS Provider Token', 0);
INSERT INTO `m_hook_schema` (`hook_template_id`, `field_type`, `field_name`, `optional`)
VALUES (2, 'string', 'SMS Provider Account Id', 0);


