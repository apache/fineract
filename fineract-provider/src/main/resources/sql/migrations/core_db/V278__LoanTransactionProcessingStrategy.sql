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

UPDATE `ref_loan_transaction_processing_strategy` SET `name`='Penalties, Fees, Interest, Principal order'
WHERE code='mifos-standard-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `name`='Overdue/Due Fee/Int,Principal' WHERE code='rbi-india-strategy' ; 
UPDATE `ref_loan_transaction_processing_strategy` SET `name`='HeavensFamily Unique' WHERE code='heavensfamily-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `name`='Creocore Unique' WHERE code='creocore-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `name`='Principal, Interest, Penalties, Fees Order' 
WHERE code='principal-interest-penalties-fees-order-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `name`='Interest, Principal, Penalties, Fees Order' 
WHERE code='interest-principal-penalties-fees-order-strategy' ;

ALTER TABLE `ref_loan_transaction_processing_strategy`ADD `sort_order` INT(4) ;

UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=1 WHERE code='mifos-standard-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=2 WHERE code='rbi-india-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=3 WHERE code='principal-interest-penalties-fees-order-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=4 WHERE code='interest-principal-penalties-fees-order-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=5 WHERE code='early-repayment-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=6 WHERE code='heavensfamily-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=7 WHERE code='creocore-strategy' ;



