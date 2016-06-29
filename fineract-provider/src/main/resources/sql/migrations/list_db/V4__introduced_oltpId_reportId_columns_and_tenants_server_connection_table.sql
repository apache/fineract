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

RENAME TABLE tenants to temp_tenants;

create table tenant_server_connections(`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`schema_server` VARCHAR(100) NOT NULL DEFAULT 'localhost',
    `schema_name` VARCHAR(100) NOT NULL,
	`schema_server_port` VARCHAR(10) NOT NULL DEFAULT '3306',
	`schema_username` VARCHAR(100) NOT NULL DEFAULT 'root',
	`schema_password` VARCHAR(100) NOT NULL DEFAULT 'mysql',
	`auto_update` TINYINT(1) NOT NULL DEFAULT '1',
	`pool_initial_size` INT(5) NULL DEFAULT '5',
	`pool_validation_interval` INT(11) NULL DEFAULT '30000',
	`pool_remove_abandoned` TINYINT(1) NULL DEFAULT '1',
	`pool_remove_abandoned_timeout` INT(5) NULL DEFAULT '60',
	`pool_log_abandoned` TINYINT(1) NULL DEFAULT '1',
	`pool_abandon_when_percentage_full` INT(5) NULL DEFAULT '50',
	`pool_test_on_borrow` TINYINT(1) NULL DEFAULT '1',
	`pool_max_active` INT(5) NULL DEFAULT '40',
	`pool_min_idle` INT(5) NULL DEFAULT '20',
	`pool_max_idle` INT(5) NULL DEFAULT '10',
	`pool_suspect_timeout` INT(5) NULL DEFAULT '60',
	`pool_time_between_eviction_runs_millis` INT(11) NULL DEFAULT '34000',
	`pool_min_evictable_idle_time_millis` INT(11) NULL DEFAULT '60000',
	`deadlock_max_retries` INT(5) NULL DEFAULT '0',
	`deadlock_max_retry_interval` INT(5) NULL DEFAULT '1',
	PRIMARY KEY (`id`)
	)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1;


INSERT INTO `tenant_server_connections` (`id`,`schema_name`,`schema_server`, `schema_server_port`, `schema_username`, `schema_password`, `auto_update`, `pool_initial_size`, `pool_validation_interval`, `pool_remove_abandoned`, `pool_remove_abandoned_timeout`, `pool_log_abandoned`, `pool_abandon_when_percentage_full`, `pool_test_on_borrow`, `pool_max_active`, `pool_min_idle`, `pool_max_idle`, `pool_suspect_timeout`, `pool_time_between_eviction_runs_millis`, `pool_min_evictable_idle_time_millis`, `deadlock_max_retries`, `deadlock_max_retry_interval`)
SELECT `id`,`schema_name`,`schema_server`, `schema_server_port`, `schema_username`, `schema_password`, `auto_update`, `pool_initial_size`, `pool_validation_interval`, `pool_remove_abandoned`, `pool_remove_abandoned_timeout`, `pool_log_abandoned`, `pool_abandon_when_percentage_full`, `pool_test_on_borrow`, `pool_max_active`, `pool_min_idle`, `pool_max_idle`, `pool_suspect_timeout`, `pool_time_between_eviction_runs_millis`, `pool_min_evictable_idle_time_millis`, `deadlock_max_retries`, `deadlock_max_retry_interval` from temp_tenants;

	
CREATE TABLE tenants(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`identifier` VARCHAR(100) NOT NULL,
	`name` VARCHAR(100) NOT NULL,
	`timezone_id` VARCHAR(100) NOT NULL,
	`country_id` INT(11) NULL DEFAULT NULL,
	`joined_date` DATE NULL DEFAULT NULL,
	`created_date` DATETIME NULL DEFAULT NULL,
	`lastmodified_date` DATETIME NULL DEFAULT NULL,
	`oltp_id` BIGINT(20) NOT NULL,
	`report_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `fk_oltp_id` (`oltp_id`),
	INDEX `fk_report_id` (`report_id`),
	CONSTRAINT `fk_oltp_id` FOREIGN KEY (`oltp_id`) REFERENCES `tenant_server_connections` (`id`),
	CONSTRAINT `fk_report_id` FOREIGN KEY (`report_id`) REFERENCES `tenant_server_connections` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1;

INSERT INTO tenants(`id`,`identifier`,`name`,`timezone_id`,`country_id`,`joined_date`,`created_date`,`lastmodified_date`,`oltp_id`, `report_id`)
SELECT  `id`,`identifier`,`name`,`timezone_id`,`country_id`,`joined_date`,`created_date`,`lastmodified_date`,`id`, `id` from temp_tenants ;


DROP TABLE temp_tenants;

