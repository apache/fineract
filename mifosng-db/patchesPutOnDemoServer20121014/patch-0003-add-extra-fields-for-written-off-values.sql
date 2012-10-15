alter table m_loan_repayment_schedule add column principal_writtenoff_derived decimal(19,6) DEFAULT NULL after principal_completed_derived;
alter table m_loan_repayment_schedule add column interest_writtenoff_derived decimal(19,6) DEFAULT NULL after interest_completed_derived;

