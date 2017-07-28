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

INSERT INTO stretchy_parameter ( parameter_name, parameter_variable, parameter_label, parameter_displayType, parameter_FormatType, parameter_default, special, selectOne, selectAll, parameter_sql, parent_id) VALUES ('transactionId', 'transactionId', 'transactionId', 'text', 'string', 'n/a', NULL, NULL, NULL, NULL, NULL);

INSERT INTO stretchy_report ( report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report) VALUES ( 'Savings Transaction Receipt', 'Pentaho', NULL, NULL, NULL, NULL, 0, 1);

INSERT INTO stretchy_report_parameter ( report_id, parameter_id, report_parameter_name) 
VALUES ((select sr.id from stretchy_report sr where sr.report_name='Savings Transaction Receipt'),
 (select sp.id from stretchy_parameter sp where sp.parameter_name='transactionId'), 
 'transactionId');