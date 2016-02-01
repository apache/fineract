/* initialises m_loan_paid_in_advance table... same sql is run in daily batch job */

truncate m_loan_paid_in_advance;

INSERT INTO m_loan_paid_in_advance(loan_id, principal_in_advance_derived, interest_in_advance_derived,
fee_charges_in_advance_derived, penalty_charges_in_advance_derived, total_in_advance_derived)
select ml.id as loanId,SUM(ifnull(mr.principal_completed_derived, 0)) as principal_in_advance_derived,
SUM(ifnull(mr.interest_completed_derived, 0)) as interest_in_advance_derived,
SUM(ifnull(mr.fee_charges_completed_derived, 0)) as fee_charges_in_advance_derived,
SUM(ifnull(mr.penalty_charges_completed_derived, 0)) as penalty_charges_in_advance_derived,

(SUM(ifnull(mr.principal_completed_derived, 0)) + SUM(ifnull(mr.interest_completed_derived, 0)) +
SUM(ifnull(mr.fee_charges_completed_derived, 0)) + SUM(ifnull(mr.penalty_charges_completed_derived, 0))) as total_in_advance_derived
FROM m_loan ml
INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id
WHERE ml.loan_status_id = 300 and mr.duedate >= CURDATE()
GROUP BY ml.id
HAVING (SUM(ifnull(mr.principal_completed_derived, 0)) + SUM(ifnull(mr.interest_completed_derived, 0)) +
SUM(ifnull(mr.fee_charges_completed_derived, 0)) + SUM(ifnull(mr.penalty_charges_completed_derived, 0))) > 0.0