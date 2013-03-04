
update m_loan
join 
(SELECT ml.id AS loanId, 
SUM(mr.principal_amount) as principal_disbursed_derived, 
SUM(IFNULL(mr.principal_completed_derived,0)) as principal_repaid_derived, 
SUM(IFNULL(mr.principal_writtenoff_derived,0)) as principal_writtenoff_derived,

SUM(IFNULL(mr.interest_amount,0)) as interest_charged_derived, 
SUM(IFNULL(mr.interest_completed_derived,0)) as interest_repaid_derived, 
SUM(IFNULL(mr.interest_waived_derived,0)) as interest_waived_derived, 
SUM(IFNULL(mr.interest_writtenoff_derived,0)) as interest_writtenoff_derived,

SUM(IFNULL(mr.fee_charges_amount,0)) as fee_charges_charged_derived, 
SUM(IFNULL(mr.fee_charges_completed_derived,0)) as fee_charges_repaid_derived, 
SUM(IFNULL(mr.fee_charges_waived_derived,0)) as fee_charges_waived_derived, 
SUM(IFNULL(mr.fee_charges_writtenoff_derived,0)) as fee_charges_writtenoff_derived,

SUM(IFNULL(mr.penalty_charges_amount,0)) as penalty_charges_charged_derived, 
SUM(IFNULL(mr.penalty_charges_completed_derived,0)) as penalty_charges_repaid_derived, 
SUM(IFNULL(mr.penalty_charges_waived_derived,0)) as penalty_charges_waived_derived, 
SUM(IFNULL(mr.penalty_charges_writtenoff_derived,0)) as penalty_charges_writtenoff_derived
			  
FROM m_loan ml
INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id
WHERE ml.disbursedon_date is not null
GROUP BY ml.id) x on x.loanId = m_loan.id

set m_loan.principal_disbursed_derived = x.principal_disbursed_derived,
	m_loan.principal_repaid_derived = x.principal_repaid_derived,
	m_loan.principal_writtenoff_derived = x.principal_writtenoff_derived,
	m_loan.principal_outstanding_derived = (x.principal_disbursed_derived -
		(x.principal_repaid_derived + x.principal_writtenoff_derived)),

	m_loan.interest_charged_derived = x.interest_charged_derived,
	m_loan.interest_repaid_derived = x.interest_repaid_derived,
	m_loan.interest_waived_derived = x.interest_waived_derived,
	m_loan.interest_writtenoff_derived = x.interest_writtenoff_derived,
	m_loan.interest_outstanding_derived = (x.interest_charged_derived -
		(x.interest_repaid_derived + x.interest_waived_derived + x.interest_writtenoff_derived)),

	m_loan.fee_charges_charged_derived = x.fee_charges_charged_derived,
	m_loan.fee_charges_repaid_derived = x.fee_charges_repaid_derived,
	m_loan.fee_charges_waived_derived = x.fee_charges_waived_derived,
	m_loan.fee_charges_writtenoff_derived = x.fee_charges_writtenoff_derived,
	m_loan.fee_charges_outstanding_derived = (x.fee_charges_charged_derived -
		(x.fee_charges_repaid_derived + x.fee_charges_waived_derived + x.fee_charges_writtenoff_derived)),

	m_loan.penalty_charges_charged_derived = x.penalty_charges_charged_derived,
	m_loan.penalty_charges_repaid_derived = x.penalty_charges_repaid_derived,
	m_loan.penalty_charges_waived_derived = x.penalty_charges_waived_derived,
	m_loan.penalty_charges_writtenoff_derived = x.penalty_charges_writtenoff_derived,
	m_loan.fee_charges_outstanding_derived = (x.penalty_charges_charged_derived -
		(x.penalty_charges_repaid_derived + x.penalty_charges_waived_derived + x.penalty_charges_writtenoff_derived)),

	m_loan.total_expected_repayment_derived = (x.principal_disbursed_derived + x.interest_charged_derived +
		x.fee_charges_charged_derived + x.penalty_charges_charged_derived),
	m_loan.total_repayment_derived = (x.principal_repaid_derived + x.interest_repaid_derived +
		x.fee_charges_repaid_derived + x.penalty_charges_repaid_derived),
	m_loan.total_expected_costofloan_derived = (x.interest_charged_derived +
		x.fee_charges_charged_derived + x.penalty_charges_charged_derived),
	m_loan.total_costofloan_derived = (x.interest_repaid_derived +
		x.fee_charges_repaid_derived + x.penalty_charges_repaid_derived),
	m_loan.total_waived_derived = (x.interest_waived_derived +
		x.fee_charges_waived_derived + x.penalty_charges_waived_derived),
	m_loan.total_writtenoff_derived = (x.interest_writtenoff_derived +
		x.fee_charges_writtenoff_derived + x.penalty_charges_writtenoff_derived),
	m_loan.total_outstanding_derived = 
		(x.principal_disbursed_derived -
			(x.principal_repaid_derived + x.principal_writtenoff_derived)) + 
		(x.interest_charged_derived -
			(x.interest_repaid_derived + x.interest_waived_derived + x.interest_writtenoff_derived)) +
		(x.fee_charges_charged_derived -
			(x.fee_charges_repaid_derived + x.fee_charges_waived_derived + x.fee_charges_writtenoff_derived)) + 
		(x.penalty_charges_charged_derived -
			(x.penalty_charges_repaid_derived + x.penalty_charges_waived_derived + x.penalty_charges_writtenoff_derived))




