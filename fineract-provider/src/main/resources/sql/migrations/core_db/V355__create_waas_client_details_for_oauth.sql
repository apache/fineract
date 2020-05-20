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

insert into `oauth_client_details`
(`client_id`, `client_secret`, `scope`, `authorized_grant_types`, `access_token_validity`, `refresh_token_validity`)
values
('waas', '{SHA-256}33abcb0a5738e28ab37b6dd53fdd4da3e5a78e2bd14332d42ce92dd071b7562b', 'all', 'password,refresh_token', 60, 300);