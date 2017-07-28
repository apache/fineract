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

create table m_loan_reschedule_request (
id bigint(20) primary key auto_increment,
loan_id bigint(20) not null,
status_enum smallint(5) not null,
reschedule_from_installment smallint(5) not null comment 'Rescheduling will start from this installment',
grace_on_principal smallint(5) null comment 'Number of installments that should be added with 0 principal amount',
grace_on_interest smallint(5) null comment 'Number of installments that should be added with 0 interest rate',
reschedule_from_date date not null comment 'Rescheduling will start from the installment with due date similar to this date.',
extra_terms smallint(5)	comment 'Number of extra terms to be added to the schedule',
interest_rate decimal(19,6)	null comment 'If provided, the interest rate for the unpaid installments will be recalculated',
recalculate_interest tinyint(1) null comment 'If set to 1, interest will be recalculated starting from the reschedule period.',
adjusted_due_date date null comment 'New due date for the first rescheduled installment',
reschedule_reason_cv_id int(11) null comment 'ID of code value of reason for rescheduling',
reschedule_reason_comment varchar(500) null comment 'Text provided in addition to the reason code value',
submitted_on_date date not null,
submitted_by_user_id bigint(20) not null,
approved_on_date date null,
approved_by_user_id bigint(20) null,
rejected_on_date date null,
rejected_by_user_id bigint(20) null,
foreign key (loan_id) references m_loan(id),
foreign key (reschedule_reason_cv_id) references m_code_value(id),
foreign key (submitted_by_user_id) references m_appuser(id),
foreign key (approved_by_user_id) references m_appuser(id),
foreign key (rejected_by_user_id) references m_appuser(id)
);

create table m_loan_repayment_schedule_history (
 id bigint(20) primary key auto_increment,
 loan_id bigint(20) not null,
 loan_reschedule_request_id bigint(20) null,
 fromdate date null,
 duedate date not null,
 installment smallint(5) not null,
 principal_amount decimal(19,6) null,
 principal_completed_derived decimal(19,6) null,
 principal_writtenoff_derived decimal(19,6) null,
 interest_amount decimal(19,6) null,
 interest_completed_derived decimal(19,6) null,
 interest_writtenoff_derived decimal(19,6) null,
 interest_waived_derived decimal(19,6) null,
 accrual_interest_derived decimal(19,6) null,
 fee_charges_amount decimal(19,6) null,
 fee_charges_completed_derived decimal(19,6) null,
 fee_charges_writtenoff_derived decimal(19,6) null,
 fee_charges_waived_derived decimal(19,6) null,
 accrual_fee_charges_derived decimal(19,6) null,
 penalty_charges_amount decimal(19,6) null,
 penalty_charges_completed_derived decimal(19,6) null,
 penalty_charges_writtenoff_derived decimal(19,6) null,
 penalty_charges_waived_derived decimal(19,6) null,
 accrual_penalty_charges_derived decimal(19,6) null,
 total_paid_in_advance_derived decimal(19,6) null,
 total_paid_late_derived decimal(19,6) null,
 completed_derived bit(1) not null,
 obligations_met_on_date date null,
 createdby_id bigint(20) null,
 created_date datetime null,
 lastmodified_date datetime null,
 lastmodifiedby_id bigint(20) null,
 foreign key (loan_id) references m_loan(id),
 foreign key (loan_reschedule_request_id) references m_loan_reschedule_request(id)
);

alter table m_loan add rescheduledon_userid bigint(20) null after rescheduledon_date;

insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) values ('loan_reschedule', 'READ_RESCHEDULELOAN', 'RESCHEDULELOAN', 'READ', '0');

insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) values ('loan_reschedule', 'CREATE_RESCHEDULELOAN', 'RESCHEDULELOAN', 'CREATE', '0');

insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) values ('loan_reschedule', 'REJECT_RESCHEDULELOAN', 'RESCHEDULELOAN', 'REJECT', '0');

insert into `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) values ('loan_reschedule', 'APPROVE_RESCHEDULELOAN', 'RESCHEDULELOAN', 'APPROVE', '0');
