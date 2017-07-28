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

INSERT INTO `c_configuration` (`id`, `name`, `value`, `enabled`) VALUES (NULL, 'force-password-reset-days', '0', '0');

ALTER TABLE  `m_appuser` ADD  `last_time_password_updated` DATE NOT NULL ,
ADD INDEX (  `last_time_password_updated` ) ;

UPDATE  `m_appuser` SET  `last_time_password_updated` =  NOW() WHERE  `m_appuser`.`last_time_password_updated` ='0000-00-00';

CREATE TABLE IF NOT EXISTS `m_appuser_previous_password` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `removal_date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

ALTER TABLE m_appuser_previous_password
ADD FOREIGN KEY (user_id) REFERENCES m_appuser(id);