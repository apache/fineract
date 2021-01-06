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

CREATE TABLE `ccpayment` (
  `savings_account_transaction_id` bigint(20) NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `descripiton` text,
  `imgUrl` varchar(100) DEFAULT NULL,
  `mcc` int(11) DEFAULT NULL,
  `operationAmount` int(11) DEFAULT NULL,
  `commissionRate` int(11) DEFAULT NULL,
  `hold` bit(1) DEFAULT NULL,
  `currencyCode` int(11) DEFAULT NULL,
  `time` int(11) DEFAULT NULL,
  `balancePersonal` int(11) DEFAULT NULL,
  `balanceCredit` int(11) DEFAULT NULL,
  `balanceCurrencyCode` int(11) DEFAULT NULL,
  `balanceType` varchar(100) DEFAULT NULL,
  `merchantId` varchar(100) DEFAULT NULL,
  `merchantTitle` varchar(100) DEFAULT NULL,
  `merchantDescripiton` text,
  `merchantAddress` text,
  `merchantimageUrl` varchar(100) DEFAULT NULL,
  `merchantType` varchar(100) DEFAULT NULL,
  `payment` varchar(100) DEFAULT NULL,
  `receipt` varchar(100) DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`savings_account_transaction_id`),
  CONSTRAINT `fk_ccpayment_savings_account_transaction_id` FOREIGN KEY (`savings_account_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- datatables mapping
INSERT INTO `x_registered_table` (`registered_table_name`, `application_table_name`) VALUES ('ccpayment', 'm_savings_account_transaction');
