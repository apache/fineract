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

ALTER TABLE  `m_client` ADD  `submittedon_date` DATE NULL DEFAULT NULL ,
ADD  `submittedon_userid` BIGINT( 20 ) NULL DEFAULT NULL ,
ADD  `activatedon_userid` BIGINT( 20 ) NULL DEFAULT NULL ,
ADD  `closedon_userid` BIGINT( 20 ) NULL DEFAULT NULL ;

ALTER TABLE  `m_group` ADD  `activatedon_userid` BIGINT( 20 ) NULL ,
ADD  `submittedon_date` DATE NULL ,
ADD  `submittedon_userid` BIGINT( 20 ) NULL ,
ADD  `closedon_userid` BIGINT( 20 ) NULL ;