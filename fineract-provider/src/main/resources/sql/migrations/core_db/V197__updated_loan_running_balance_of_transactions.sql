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

CREATE TABLE `m_loan_transaction_temp` (
	`id` BIGINT(20) NOT NULL,
	`loan_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL DEFAULT '0',
	`transaction_date` DATE NOT NULL
);

INSERT INTO m_loan_transaction_temp(`id`,`loan_id`,`transaction_date`,`amount`) select lt.id, lt.loan_id,lt.transaction_date,if(lt.transaction_type_enum = 1 , IFNULL(lt.amount,0),IFNULL(-lt.principal_portion_derived,0)) from m_loan_transaction lt where lt.is_reversed=0;


UPDATE m_loan_transaction lt SET lt.outstanding_loan_balance_derived = (select sum(ltt.amount) from m_loan_transaction_temp ltt where ((ltt.transaction_date = lt.transaction_date and ltt.id  <= lt.id) or ltt.transaction_date < lt.transaction_date) and ltt.loan_id = lt.loan_id) where lt.transaction_type_enum != 10 and lt.is_reversed = 0;

DROP TABLE `m_loan_transaction_temp`;
