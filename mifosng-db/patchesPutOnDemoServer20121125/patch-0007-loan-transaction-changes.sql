alter table m_loan_transaction
drop column `charges_portion_derived`;

alter table m_loan_transaction
add column `fee_charges_portion_derived` decimal(19,6) DEFAULT NULL after `interest_portion_derived`;

alter table m_loan_transaction
add column `penalty_charges_portion_derived` decimal(19,6) DEFAULT NULL after `fee_charges_portion_derived`;