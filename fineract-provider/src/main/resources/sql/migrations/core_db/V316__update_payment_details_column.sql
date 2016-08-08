update acc_gl_journal_entry as je
join m_loan_transaction as mlt on je.`loan_transaction_id` = mlt.`id`
set je.`payment_details_id` = mlt.`payment_detail_id`
where je.`loan_transaction_id` is not null;

update acc_gl_journal_entry as je
join `m_savings_account_transaction` as mlt on je.`savings_transaction_id` = mlt.`id`
set je.`payment_details_id` = mlt.`payment_detail_id`
where je.`savings_transaction_id` is not null;