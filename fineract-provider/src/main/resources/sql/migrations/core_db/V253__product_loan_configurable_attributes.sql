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

CREATE TABLE `m_product_loan_configurable_attributes` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `loan_product_id` BIGINT NOT NULL,
  `amortization_method_enum` TINYINT NOT NULL DEFAULT '1',
  `interest_method_enum` TINYINT NOT NULL DEFAULT '1',
  `loan_transaction_strategy_id` TINYINT NOT NULL DEFAULT '1',
  `interest_calculated_in_period_enum` TINYINT NOT NULL DEFAULT '1',
  `arrearstolerance_amount` TINYINT NOT NULL DEFAULT '1',
  `repay_every` TINYINT NOT NULL DEFAULT '1',
  `moratorium` TINYINT NOT NULL DEFAULT '1',
  `grace_on_arrears_ageing` TINYINT NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_m_product_loan_configurable_attributes_0001` (`loan_product_id`),
  CONSTRAINT `fk_m_product_loan_configurable_attributes_0001` FOREIGN KEY (`loan_product_id`) REFERENCES `m_product_loan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


 INSERT into `m_product_loan_configurable_attributes` 
(loan_product_id,amortization_method_enum,interest_method_enum,loan_transaction_strategy_id,
interest_calculated_in_period_enum,arrearstolerance_amount,repay_every,moratorium,grace_on_arrears_ageing)
(select pl.id,'1','1','1','1','1','1','1','1' from `m_product_loan` pl );