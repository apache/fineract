alter table m_loan_charge 
add column due_for_collection_as_of_date date DEFAULT NULL after charge_time_enum;

alter table m_loan_repayment_schedule
add column fee_charges_amount decimal(19,6) DEFAULT NULL after interest_writtenoff_derived;

alter table m_loan_repayment_schedule
add column fee_charges_completed_derived decimal(19,6) DEFAULT NULL after fee_charges_amount;

alter table m_loan_repayment_schedule
add column fee_charges_writtenoff_derived decimal(19,6) DEFAULT NULL after fee_charges_completed_derived;

alter table m_loan_repayment_schedule
add column fee_charges_waived_derived decimal(19,6) DEFAULT NULL after fee_charges_writtenoff_derived;


alter table m_loan_repayment_schedule
add column penalty_charges_amount decimal(19,6) DEFAULT NULL after fee_charges_waived_derived;

alter table m_loan_repayment_schedule
add column penalty_charges_completed_derived decimal(19,6) DEFAULT NULL after penalty_charges_amount;

alter table m_loan_repayment_schedule
add column penalty_charges_writtenoff_derived decimal(19,6) DEFAULT NULL after penalty_charges_completed_derived;

alter table m_loan_repayment_schedule
add column penalty_charges_waived_derived decimal(19,6) DEFAULT NULL after penalty_charges_writtenoff_derived;

/**
modify existing columns
*/
alter table `m_loan_repayment_schedule` MODIFY `duedate` date NOT NULL;
alter table `m_loan_repayment_schedule` MODIFY `installment` smallint(5) NOT NULL;
alter table `m_loan_repayment_schedule` MODIFY `interest_waived_derived` decimal(19,6) DEFAULT NULL;