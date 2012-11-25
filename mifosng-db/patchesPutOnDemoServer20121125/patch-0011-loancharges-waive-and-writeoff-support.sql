alter table m_loan_charge 
add column `amount_waived_derived` decimal(19,6) DEFAULT NULL after `amount_paid_derived`;

alter table m_loan_charge 
add column `amount_writtenoff_derived` decimal(19,6) DEFAULT NULL after `amount_waived_derived`;

