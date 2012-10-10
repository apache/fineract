alter table m_product_loan drop column flexible_repayment_schedule;
alter table m_product_loan drop column interest_rebate;

alter table m_loan drop column flexible_repayment_schedule;
alter table m_loan drop column interest_rebate;
alter table m_loan drop column interest_rebate_amount;