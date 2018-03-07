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

ALTER TABLE `m_loan_term_variations`
	ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT '1' AFTER `applied_on_loan_status`;
	
ALTER TABLE `m_loan_term_variations`
	ADD COLUMN `parent_id` BIGINT(20) NULL DEFAULT NULL AFTER `is_active`;

ALTER TABLE `m_loan_term_variations`
	ADD COLUMN `reshedule_request_id` BIGINT(20) NULL DEFAULT NULL AFTER `parent_id`;

insert into m_loan_term_variations(`loan_id`, `term_type`, `applicable_date`, `decimal_value`, `date_value` , `is_specific_to_installment`, `applied_on_loan_status`, `reshedule_request_id` , `is_active` , `parent_id`)
(	
select
mlrr.loan_id,
 7 term_type,
 ifnull(mlrr.adjusted_due_date,mlrr.reschedule_from_date) applicable_date,
 mlrr.grace_on_interest decimal_value,
 null date_value,
 1 is_specific_to_installment,
 300 applied_on_loan_status_id,
 mlrr.id reshedule_request_id,
 if(mlrr.status_enum = 200 ,1,0) is_active,
 null parent_id
from m_loan_reschedule_request mlrr 
where mlrr.grace_on_interest is not null);

insert into m_loan_term_variations(`loan_id`, `term_type`, `applicable_date`, `decimal_value`, `date_value` , `is_specific_to_installment`, `applied_on_loan_status`, `reshedule_request_id` , `is_active` , `parent_id`)
(	
select
mlrr.loan_id,
 8 term_type,
 ifnull(mlrr.adjusted_due_date,mlrr.reschedule_from_date) applicable_date,
 mlrr.grace_on_principal decimal_value,
 null date_value,
 1 is_specific_to_installment,
 300 applied_on_loan_status_id,
 mlrr.id reshedule_request_id,
 if(mlrr.status_enum = 200 ,1,0) is_active,
 null parent_id
from m_loan_reschedule_request mlrr 
where mlrr.grace_on_principal is not null);

insert into m_loan_term_variations(`loan_id`, `term_type`, `applicable_date`, `decimal_value`, `date_value` , `is_specific_to_installment`, `applied_on_loan_status`, `reshedule_request_id` , `is_active` , `parent_id`)
(	
select
mlrr.loan_id,
 4 term_type,
 mlrr.reschedule_from_date applicable_date,
 null decimal_value,
 mlrr.adjusted_due_date date_value,
 1 is_specific_to_installment,
 300 applied_on_loan_status_id,
 mlrr.id reshedule_request_id,
 if(mlrr.status_enum = 200 ,1,0) is_active,
 null parent_id
from m_loan_reschedule_request mlrr 
where mlrr.adjusted_due_date is not null);

insert into m_loan_term_variations(`loan_id`, `term_type`, `applicable_date`, `decimal_value`, `date_value` , `is_specific_to_installment`, `applied_on_loan_status`, `reshedule_request_id` , `is_active` , `parent_id`)
(	
select
mlrr.loan_id,
 9 term_type,
 ifnull(mlrr.adjusted_due_date,mlrr.reschedule_from_date) applicable_date,
 mlrr.extra_terms decimal_value,
 null date_value,
 1 is_specific_to_installment,
 300 applied_on_loan_status_id,
 mlrr.id reshedule_request_id,
 if(mlrr.status_enum = 200 ,1,0) is_active,
 null parent_id
from m_loan_reschedule_request mlrr 
where mlrr.extra_terms is not null);

insert into m_loan_term_variations(`loan_id`, `term_type`, `applicable_date`, `decimal_value`, `date_value` , `is_specific_to_installment`, `applied_on_loan_status`, `reshedule_request_id` , `is_active` , `parent_id`)
(	
select
mlrr.loan_id,
 2 term_type,
 ifnull(mlrr.adjusted_due_date,mlrr.reschedule_from_date) applicable_date,
 mlrr.interest_rate decimal_value,
 null date_value,
 1 is_specific_to_installment,
 300 applied_on_loan_status_id,
 mlrr.id reshedule_request_id,
 if(mlrr.status_enum = 200 ,1,0) is_active,
 null parent_id
from m_loan_reschedule_request mlrr 
where mlrr.interest_rate is not null);

insert into m_loan_term_variations(`loan_id`, `term_type`, `applicable_date`, `decimal_value`, `date_value` , `is_specific_to_installment`, `applied_on_loan_status`, `reshedule_request_id` , `is_active` , `parent_id`)
(	
select
mlrr.loan_id,
 9 term_type,
 mlrr.reschedule_from_date applicable_date,
 if(ifnull(mlrr.grace_on_principal,0) > ifnull(mlrr.grace_on_interest,0),mlrr.grace_on_principal,mlrr.grace_on_interest) decimal_value,
 null date_value,
 1 is_specific_to_installment,
 300 applied_on_loan_status_id,
 mlrr.id reshedule_request_id,
 if(mlrr.status_enum = 200 ,1,0) is_active,
 (select distinct mlt.id
 from m_loan_term_variations mlt 
 where mlt.reshedule_request_id is not null
 and (mlt.term_type =8 or mlt.term_type = 7)
 and mlt.reshedule_request_id = mlrr.id
 group by mlt.id
 order by mlt.term_type desc limit 1) parent_id
from m_loan_reschedule_request mlrr 
where (mlrr.grace_on_interest is not null or mlrr.grace_on_principal is not null));
	
ALTER TABLE `m_loan_reschedule_request`
	DROP COLUMN `grace_on_principal`,
	DROP COLUMN `grace_on_interest`,
	DROP COLUMN `extra_terms`,
	DROP COLUMN `interest_rate`,
	DROP COLUMN `adjusted_due_date`;

CREATE TABLE `m_loan_reschedule_request_term_variations_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_reschedule_request_id` BIGINT(20) NOT NULL,
	`loan_term_variations_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK__m_loan_reschedule_request` (`loan_reschedule_request_id`),
	INDEX `FK__m_loan_term_variations` (`loan_term_variations_id`),
	CONSTRAINT `FK__m_loan_reschedule_request` FOREIGN KEY (`loan_reschedule_request_id`) REFERENCES `m_loan_reschedule_request` (`id`),
	CONSTRAINT `FK__m_loan_term_variations` FOREIGN KEY (`loan_term_variations_id`) REFERENCES `m_loan_term_variations` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

insert ignore into m_loan_reschedule_request_term_variations_mapping (`loan_reschedule_request_id`, `loan_term_variations_id`)
(
select distinct mltv.reshedule_request_id,mltv.id
from m_loan_term_variations mltv
where reshedule_request_id is not null
);

ALTER TABLE `m_loan_term_variations`
	DROP COLUMN `reshedule_request_id`;