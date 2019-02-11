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


INSERT INTO `c_external_service_properties` (`name`, `value`, `external_service_id`)

(SELECT
   'fromEmail' as name,
   value as value,
   c_external_service.id as external_service_id
 FROM c_external_service_properties
 JOIN c_external_service ON c_external_service_properties.external_service_id=c_external_service.id
WHERE c_external_service_properties.name='username'
  AND c_external_service.name='SMTP_Email_Account')

  UNION ALL

(SELECT
   'fromName' as name,
   value as value,
   c_external_service.id as external_service_id
 FROM c_external_service_properties
 JOIN c_external_service ON c_external_service_properties.external_service_id=c_external_service.id
WHERE c_external_service_properties.name='username'
  AND c_external_service.name='SMTP_Email_Account');
