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

create table if not exists m_report_mailing_job (
id bigint primary key auto_increment,
name varchar(100) not null,
description text null,
start_datetime datetime not null,
recurrence varchar(100) null,
created_date date not null,
createdby_id bigint not null,
lastmodified_date date null,
lastmodifiedby_id bigint null,
email_recipients text not null,
email_subject varchar(100) not null,
email_message text not null,
email_attachment_file_format varchar(10) not null,
stretchy_report_id int not null,
stretchy_report_param_map text null,
previous_run_datetime datetime null,
next_run_datetime datetime null,
previous_run_status varchar(10) null,
previous_run_error_log text null,
previous_run_error_message text null,
number_of_runs int not null default 0,
is_active tinyint(1) not null default 0,
is_deleted tinyint(1) not null default 0,
run_as_userid bigint not null,
foreign key (createdby_id) references m_appuser(id),
foreign key (lastmodifiedby_id) references m_appuser(id),
foreign key (stretchy_report_id) references stretchy_report(id),
foreign key (run_as_userid) references m_appuser(id),
constraint unique_name unique (name)
);

create table if not exists m_report_mailing_job_run_history (
id bigint primary key auto_increment,
job_id bigint not null,
start_datetime datetime not null,
end_datetime datetime not null,
status varchar(10) not null,
error_message text null,
error_log text null,
foreign key (job_id) references m_report_mailing_job (id)
);

create table if not exists m_report_mailing_job_configuration (
id int primary key auto_increment,
name varchar(50) not null,
`value` varchar(200) not null,
constraint unique_name unique (name)
);

insert into m_permission (`grouping`, code, entity_name, action_name, can_maker_checker)
values ('jobs', 'CREATE_REPORTMAILINGJOB', 'REPORTMAILINGJOB', 'CREATE', 0), 
('jobs', 'UPDATE_REPORTMAILINGJOB', 'REPORTMAILINGJOB', 'UPDATE', 0), 
('jobs', 'DELETE_REPORTMAILINGJOB', 'REPORTMAILINGJOB', 'DELETE', 0), 
('jobs', 'READ_REPORTMAILINGJOB', 'REPORTMAILINGJOB', 'READ', 0);

insert into m_report_mailing_job_configuration (name, `value`)
values ('GMAIL_SMTP_SERVER', 'smtp.gmail.com'), ('GMAIL_SMTP_PORT', 587), ('GMAIL_SMTP_USERNAME', ''), ('GMAIL_SMTP_PASSWORD', '');

insert into job (name, display_name, cron_expression, create_time)
values ('Execute Report Mailing Jobs', 'Execute Report Mailing Jobs', '0 0/15 * * * ?', NOW());
