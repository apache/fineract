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

INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES
('PaymentType',1);

/*Update payment detail to add foreign key relationship to user defined Code Value*/
update m_loan_transaction set payment_detail_id=null;
ALTER TABLE `m_payment_detail`
	ALTER `payment_type_enum` DROP DEFAULT;
ALTER TABLE `m_payment_detail`
	CHANGE COLUMN `payment_type_enum` `payment_type_cv_id` INT(11) NULL AFTER `id`;
delete from m_payment_detail;
ALTER TABLE `m_payment_detail`
	ADD CONSTRAINT `FK_m_payment_detail_m_code_value` FOREIGN KEY (`payment_type_cv_id`) REFERENCES `m_code_value` (`id`);

/*Map Different Payment Channels to payment Types*/
ALTER TABLE `acc_product_mapping`
	ADD COLUMN `payment_type` INT(11) NULL DEFAULT NULL AFTER `product_type`,
	ADD CONSTRAINT `FK_acc_product_mapping_m_code_value` FOREIGN KEY (`payment_type`) REFERENCES `m_code_value` (`id`);
