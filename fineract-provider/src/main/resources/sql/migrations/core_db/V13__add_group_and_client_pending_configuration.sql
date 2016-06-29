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



/*
default client and group status is active 300.  If you want to be able to have pending as the initial client stat
*/
INSERT INTO c_configuration (`name`, `enabled`) VALUES ('allow-pending-client-status', '0');
INSERT INTO c_configuration (`name`, `enabled`) VALUES ('allow-pending-group-status', '0');


INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('status_id', '0', 'Invalid', 'Invalid');
INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('status_id', '100', 'Pending', 'Pending');
INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('status_id', '300', 'Active', 'Active');
INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('status_id', '600', 'Closed', 'Closed');
INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('loan_status_id', '0', 'Invalid', 'Invalid');