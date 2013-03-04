/*
This patch (or something like it) needs to run daily (after midnight and before next day opening)
against each tenant to keep the arrears values accurate
*/

truncate table m_loan_arrears_aging;

INSERT INTO m_loan_arrears_aging
(`loan_id`,
`principal_overdue_derived`,
`interest_overdue_derived`,
`fee_charges_overdue_derived`,
`penalty_charges_overdue_derived`,
`total_overdue_derived`,
`overdue_since_date_derived`)
select ml.id as loanId,
SUM(
	(ifnull(mr.principal_amount,0) - ifnull(mr.principal_completed_derived, 0))
		) as principal_overdue_derived,
SUM(
	(ifnull(mr.interest_amount,0)  - ifnull(mr.interest_completed_derived, 0))
		) as interest_overdue_derived,
SUM(
	(ifnull(mr.fee_charges_amount,0)  - ifnull(mr.fee_charges_completed_derived, 0))
		) as fee_charges_overdue_derived,
SUM(
	(ifnull(mr.penalty_charges_amount,0)  - ifnull(mr.penalty_charges_completed_derived, 0))
		) as penalty_charges_overdue_derived,


SUM(
	(ifnull(mr.principal_amount,0) - ifnull(mr.principal_completed_derived, 0))
		) +
SUM(
	(ifnull(mr.interest_amount,0)  - ifnull(mr.interest_completed_derived, 0))
		) +
SUM(
	(ifnull(mr.fee_charges_amount,0)  - ifnull(mr.fee_charges_completed_derived, 0))
		) +
SUM(
	(ifnull(mr.penalty_charges_amount,0)  - ifnull(mr.penalty_charges_completed_derived, 0))
		) as total_overdue_derived,


MIN(mr.duedate) as overdue_since_date_derived
			  

FROM   m_loan ml  
INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id
WHERE ml.loan_status_id = 300 /*active*/
and mr.completed_derived is false
and mr.duedate < CURDATE()
GROUP BY ml.id;