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

UPDATE `scheduled_email_configuration`
    SET name='EMAIL_SMTP_SERVER' WHERE name='SMTP_SERVER';

UPDATE `scheduled_email_configuration`
    SET name='EMAIL_SMTP_PORT' WHERE name='SMTP_PORT';

UPDATE `scheduled_email_configuration`
    SET name='EMAIL_SMTP_USERNAME' WHERE name='SMTP_USERNAME';

UPDATE `scheduled_email_configuration`
    SET name='EMAIL_SMTP_PASSWORD' WHERE name='SMTP_PASSWORD';

INSERT INTO `scheduled_email_configuration` (`name`)
    VALUES ('EMAIL_FROM_EMAIL'),
           ('EMAIL_FROM_NAME');