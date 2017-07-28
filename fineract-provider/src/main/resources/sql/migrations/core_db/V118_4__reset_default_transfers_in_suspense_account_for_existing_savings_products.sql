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

/***Delete previously set defaults**/
delete from acc_product_mapping where financial_account_type=10 and product_type=2;

/***Set the proper defaults**/
INSERT INTO `acc_product_mapping` (`gl_account_id`,`product_id`,`product_type`,`payment_type`,`charge_id`,`financial_account_type`)
select mapping.gl_account_id,mapping.product_id,mapping.product_type,mapping.payment_type,mapping.charge_id, 10
from acc_product_mapping mapping
where mapping.financial_account_type = 2 and mapping.product_type=2;