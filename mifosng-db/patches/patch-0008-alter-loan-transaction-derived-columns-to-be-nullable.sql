alter table m_loan_transaction modify column principal_portion_derived decimal(19,6);

alter table m_loan_transaction modify column interest_portion_derived decimal(19,6);

alter table m_loan_transaction modify column charges_portion_derived decimal(19,6);


alter table m_product_loan modify column arrearstolerance_amount decimal(19,6);