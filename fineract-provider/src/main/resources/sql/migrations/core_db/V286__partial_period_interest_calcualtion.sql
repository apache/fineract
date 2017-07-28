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

ALTER TABLE `m_product_loan`
	ADD COLUMN `allow_partial_period_interest_calcualtion` TINYINT(1) NOT NULL DEFAULT '0' AFTER `interest_calculated_in_period_enum`;
	
ALTER TABLE `m_loan`
	ADD COLUMN `allow_partial_period_interest_calcualtion` TINYINT(1) NOT NULL DEFAULT '0' AFTER `interest_calculated_in_period_enum`;
	
UPDATE m_product_loan mpl inner join (select mp.id as productId from m_product_loan mp where mp.interest_calculated_in_period_enum = 1 and  (mp.interest_recalculation_enabled = 1 or mp.allow_multiple_disbursals = 1 or mp.is_linked_to_floating_interest_rates =1 or mp.allow_variabe_installments =1)) x on x.productId = mpl.id SET mpl.allow_partial_period_interest_calcualtion = 1;	
	
UPDATE m_loan ml inner join (select loan.id as loanId from m_product_loan mp inner join m_loan loan on loan.product_id = mp.id where mp.allow_partial_period_interest_calcualtion = 1) x on x.loanId = ml.id SET ml.allow_partial_period_interest_calcualtion=1;
	
