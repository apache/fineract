/**Remove all existing apply interest transaction Types**/
update m_loan_transaction set interest_portion_derived=amount where transaction_type_enum=11;
update m_loan_transaction set transaction_type_enum=10 where transaction_type_enum=11;
