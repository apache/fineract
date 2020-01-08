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

CREATE TABLE IF NOT EXISTS `job_parameters` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT(20) NOT NULL,
  `parameter_name` VARCHAR(100) NOT NULL,
  `parameter_value` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_job_id` (`job_id`),
  CONSTRAINT `FK_job_id` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)

);

INSERT IGNORE INTO `job_parameters`(`job_id`,`parameter_name`,`parameter_value`) VALUES(17,'thread-pool-size',10);
INSERT IGNORE INTO `job_parameters`(`job_id`,`parameter_name`,`parameter_value`) VALUES(17,'batch-size',100);
INSERT IGNORE INTO `job_parameters`(`job_id`,`parameter_name`,`parameter_value`) VALUES(17,'officeId',1);