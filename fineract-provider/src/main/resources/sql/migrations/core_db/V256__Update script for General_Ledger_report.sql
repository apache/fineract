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

update stretchy_report_parameter 
set report_parameter_name='account'
where report_id = (select stretchy_report.id from stretchy_report where report_name='GeneralLedgerReport')
and parameter_id=(select p.id from stretchy_parameter p where parameter_name='SelectGLAccountNO');


update stretchy_report_parameter
set report_parameter_name='fromDate'
where report_id =(select stretchy_report.id from stretchy_report where report_name='GeneralLedgerReport')
and parameter_id=(select p.id from stretchy_parameter p where parameter_name='startDateSelect');

update stretchy_report_parameter
set report_parameter_name='toDate'
where report_id=(select stretchy_report.id from stretchy_report where report_name='GeneralLedgerReport')
and parameter_id=(select p.id from stretchy_parameter p where parameter_name='endDateSelect');

update stretchy_report_parameter
set report_parameter_name='branch'
where report_id=(select stretchy_report.id from stretchy_report where report_name='GeneralLedgerReport')
and parameter_id=(select p.id from stretchy_parameter p where parameter_name='OfficeIdSelectOne');

INSERT INTO `m_permission` (
`grouping` ,
`code` ,
`entity_name` ,
`action_name` ,
`can_maker_checker`
) VALUES ('report', 'READ_General Ledger Report', 'General Ledger Report', 'READ', 0);

update `stretchy_parameter` 
set `parameter_sql` = 'select id aid,name aname\r\nfrom acc_gl_account'
where stretchy_parameter.parameter_name='SelectGLAccountNO';