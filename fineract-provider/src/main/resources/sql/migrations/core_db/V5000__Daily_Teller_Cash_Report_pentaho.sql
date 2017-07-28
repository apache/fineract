--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

DELIMITER $$
CREATE PROCEDURE `CashierTransactionSummary`(
	IN `officeId` BIGINT,
	IN `tellerId` BIGINT,
	IN `cashierId` BIGINT,
	IN `currencyCode` TEXT,
	IN `asOnDate` DATE
)
LANGUAGE SQL
NOT DETERMINISTIC
CONTAINS SQL
SQL SECURITY DEFINER
COMMENT ''
BEGIN


-- Create temporary table
CREATE TEMPORARY TABLE temp_cashier_transactions(
`transaction_date` DATE,
`transaction_type` VARCHAR(20), 
`amount` DECIMAL(19,6));

-- Insert result set into temporary table
INSERT INTO temp_cashier_transactions 
SELECT cashier_txn.txn_date AS transaction_date, 
CASE 
WHEN cashier_txn.txn_type = 101
	THEN 'cash_allocated'
WHEN cashier_txn.txn_type = 102
	THEN 'cash_settled'
END AS transaction_type,
	cashier_txn.txn_amount AS transaction_amount
FROM m_cashier_transactions cashier_txn
LEFT JOIN m_cashiers cashier ON cashier.id = cashier_txn.cashier_id
LEFT JOIN m_tellers teller ON teller.id = cashier.teller_id
WHERE cashier.teller_id = tellerId
	AND cashier_txn.cashier_id = cashierId
	AND cashier_txn.currency_code = currencyCode 

UNION ALL

SELECT savings_txn.transaction_date AS transaction_date, 
CASE 
WHEN (((savings_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) 
		AND acnttrans.id IS NULL)
	AND renum.enum_value IN ('deposit','withdrawal fee', 'Pay Charge', 'Annual Fee')) 
	THEN 'cash_in'
WHEN (((savings_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) 
		AND acnttrans.id IS NULL)
	AND renum.enum_value IN ('withdrawal', 'Waive Charge', 'Interest Posting', 'Overdraft Interest')) 
	THEN 'cash_out'
WHEN acnttrans.id IS NOT NULL AND acnttrans.from_savings_transaction_id IS NOT NULL
	THEN 'transfers'
WHEN ((payType.is_cash_payment = 0) 
	AND renum.enum_value IN ('deposit','withdrawal fee', 'Pay Charge', 'Annual Fee')) 
	THEN CONCAT(payType.value, '_in')
WHEN ((payType.is_cash_payment = 0) 
	AND renum.enum_value IN ('withdrawal', 'Waive Charge', 'Interest Posting', 'Overdraft Interest')) 
	THEN CONCAT(payType.value, '_out')
END AS transaction_type,
savings_txn.amount AS transaction_amount
FROM m_savings_account_transaction savings_txn
LEFT JOIN r_enum_value renum 
		ON savings_txn.transaction_type_enum = renum.enum_id 
		AND renum.enum_name = 'savings_transaction_type_enum'
LEFT JOIN m_payment_detail payDetails ON payDetails.id = savings_txn.payment_detail_id
LEFT JOIN m_payment_type payType ON payType.id = payDetails.payment_type_id
LEFT JOIN m_account_transfer_transaction acnttrans 
		ON (acnttrans.from_savings_transaction_id = savings_txn.id 
				OR acnttrans.to_savings_transaction_id = savings_txn.id)
LEFT JOIN m_savings_account savings ON savings_txn.savings_account_id = savings.id
LEFT JOIN m_appuser au ON savings_txn.appuser_id = au.id
LEFT JOIN m_staff s ON au.staff_id = s.id
LEFT JOIN m_cashiers c ON c.staff_id = s.id
LEFT JOIN m_tellers t ON t.id = c.teller_id
WHERE savings_txn.is_reversed = 0 
	AND c.teller_id = tellerId
	AND c.id = cashierId
	AND savings.currency_code = currencyCode 
	AND renum.enum_value IN ('deposit','withdrawal fee', 'Pay Charge', 'Annual Fee', 'withdrawal', 
										'Waive Charge', 'Interest Posting', 'Overdraft Interest')


UNION ALL


SELECT loan_txn.transaction_date AS transaction_date, 
CASE 
WHEN (((loan_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) 
		AND acnttrans.id IS NULL)
	AND renum.enum_value IN ('REPAYMENT_AT_DISBURSEMENT','REPAYMENT', 'RECOVERY_REPAYMENT', 'CHARGE_PAYMENT')) 
	THEN 'cash_in'
WHEN (((loan_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) 
		AND acnttrans.id IS NULL)
	AND renum.enum_value IN ('DISBURSEMENT', 'WAIVE_INTEREST', 'WRITEOFF', 'WAIVE_CHARGES')) 
	THEN 'cash_out'
WHEN acnttrans.id IS NOT NULL AND acnttrans.from_loan_transaction_id IS NOT NULL
	THEN 'transfers'
WHEN ((payType.is_cash_payment = 0) 
	AND renum.enum_value IN ('REPAYMENT_AT_DISBURSEMENT','REPAYMENT', 'RECOVERY_REPAYMENT', 'CHARGE_PAYMENT')) 
	THEN CONCAT(payType.value, '_in')
WHEN ((payType.is_cash_payment = 0) 
	AND renum.enum_value IN ('DISBURSEMENT', 'WAIVE_INTEREST', 'WRITEOFF', 'WAIVE_CHARGES')) 
	THEN CONCAT(payType.value, '_out')
END AS transaction_type,
loan_txn.amount AS transaction_amount
FROM m_loan_transaction loan_txn
LEFT JOIN r_enum_value renum ON loan_txn.transaction_type_enum = renum.enum_id 
	AND renum.enum_name = 'loan_transaction_type_enum'
LEFT JOIN m_payment_detail payDetails ON payDetails.id = loan_txn.payment_detail_id
LEFT JOIN m_payment_type payType ON payType.id = payDetails.payment_type_id
LEFT JOIN m_account_transfer_transaction acnttrans 
			ON (acnttrans.from_loan_transaction_id = loan_txn.id
					OR acnttrans.to_loan_transaction_id = loan_txn.id)
LEFT JOIN m_loan loan ON loan_txn.loan_id = loan.id
LEFT JOIN m_appuser au ON loan_txn.appuser_id = au.id
LEFT JOIN m_staff s ON au.staff_id = s.id
LEFT JOIN m_cashiers c ON c.staff_id = s.id
LEFT JOIN m_tellers t ON t.id = c.teller_id
WHERE loan_txn.is_reversed = 0 
	AND c.id = cashierId
	AND c.teller_id = tellerId
	AND loan.currency_code = currencyCode 
	AND renum.enum_value IN ('REPAYMENT_AT_DISBURSEMENT','REPAYMENT', 'RECOVERY_REPAYMENT', 
										'CHARGE_PAYMENT', 'DISBURSEMENT', 'WAIVE_INTEREST', 'WRITEOFF', 'WAIVE_CHARGES')


UNION ALL


SELECT client_txn.transaction_date AS transaction_date, 
CASE 
WHEN ((client_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) 
	AND renum.enum_value IN ('PAY_CHARGE')) 
	THEN 'cash_in' 
WHEN ((client_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) 
	AND renum.enum_value IN ('WAIVE_CHARGE')) 
	THEN 'cash_out' 
WHEN ((payType.is_cash_payment = 0) 
	AND renum.enum_value IN ('PAY_CHARGE')) 
	THEN CONCAT(payType.value, '_in') 
WHEN ((payType.is_cash_payment = 0) 
	AND renum.enum_value IN ('WAIVE_CHARGE')) 
	THEN CONCAT(payType.value, '_out') ELSE 'invalid'
END AS transaction_type, 
client_txn.amount AS transaction_amount
FROM m_client_transaction client_txn
LEFT JOIN r_enum_value renum ON client_txn.transaction_type_enum = renum.enum_id 
	AND renum.enum_name = 'client_transaction_type_enum'
LEFT JOIN m_payment_detail payDetails ON payDetails.id = client_txn.payment_detail_id
LEFT JOIN m_payment_type payType ON payType.id = payDetails.payment_type_id
LEFT JOIN m_appuser au ON client_txn.appuser_id = au.id
LEFT JOIN m_staff s ON au.staff_id = s.id
LEFT JOIN m_cashiers c ON c.staff_id = s.id
LEFT JOIN m_tellers t ON t.id = c.teller_id
WHERE client_txn.is_reversed = 0 
	AND c.id = cashierId
	AND c.teller_id = tellerId
	AND client_txn.currency_code = currencyCode 
	AND renum.enum_value IN ('PAY_CHARGE', 'WAIVE_CHARGE');

-- SELECT * FROM temp_cashier_transactions;


-- Create final temporary table one
CREATE TEMPORARY TABLE final_temp_cashier_report(
`Row Title` VARCHAR(50),
`Row Value` CHAR(50), 
`Verification` VARCHAR(20));


-- Insert office into final temporary table
INSERT INTO final_temp_cashier_report SELECT 'Office' AS '', 
o.name AS '', '' AS ''
FROM m_office o
WHERE o.id = officeId;


-- Insert teller into final temporary table
INSERT INTO final_temp_cashier_report SELECT 'Teller' AS '', 
t.name AS '', '' AS ''
FROM m_tellers t
WHERE t.id = tellerId;

-- Insert teller into final temporary table
INSERT INTO final_temp_cashier_report SELECT 'Cashier' AS '', 
s.display_name AS '', '' AS ''
FROM m_cashiers c
JOIN m_tellers mt ON mt.id = c.teller_id
JOIN m_staff s ON s.id = c.staff_id
WHERE c.teller_id = tellerId
AND c.id = cashierId;

-- Insert currency into final temporary table
INSERT INTO final_temp_cashier_report VALUES ('Currency', currencyCode, '');

-- Insert date into final temporary table
INSERT INTO final_temp_cashier_report VALUES ('As On Date', asOnDate, '');

-- Insert opening balance into final temporary table
INSERT INTO final_temp_cashier_report 
SELECT 'Beginning cash drawer balance' AS '', 
CAST(SUM(CASE
WHEN (transaction_type = 'cash_allocated' AND transaction_date < asOnDate) THEN amount
WHEN (transaction_type = 'cash_settled' AND transaction_date < asOnDate) THEN (-1 * amount)
WHEN (transaction_type = 'cash_in' AND transaction_date < asOnDate) THEN amount
WHEN (transaction_type = 'cash_out' AND transaction_date < asOnDate) THEN (-1 * amount)
ELSE 0
END) AS CHAR) AS '', '' AS '' 
FROM temp_cashier_transactions;

-- Insert ending balance into final temporary table
INSERT INTO final_temp_cashier_report 
SELECT 'Ending cash drawer balance' AS '', 
CAST(SUM(CASE
WHEN (transaction_type = 'cash_allocated' AND transaction_date <= asOnDate) THEN amount
WHEN (transaction_type = 'cash_settled' AND transaction_date <= asOnDate) THEN (-1 * amount)
WHEN (transaction_type = 'cash_in' AND transaction_date <= asOnDate) THEN amount
WHEN (transaction_type = 'cash_out' AND transaction_date <= asOnDate) THEN (-1 * amount)
ELSE 0
END) AS CHAR) AS '', '' AS '' 
FROM temp_cashier_transactions;

-- Insert cash-in into final temporary table
INSERT INTO final_temp_cashier_report 
SELECT 'Total cash disbursed' AS '', 
SUM(CASE
WHEN (transaction_type = 'cash_out' AND transaction_date BETWEEN asOnDate AND  asOnDate) THEN amount
ELSE 0
END) AS '', '' AS ''
FROM temp_cashier_transactions;

-- Insert cash-out into final temporary table
INSERT INTO final_temp_cashier_report 
SELECT 'Total cash received' AS '', 
SUM(CASE
WHEN (transaction_type = 'cash_in' AND transaction_date BETWEEN asOnDate AND  asOnDate) THEN amount
ELSE 0
END) AS '', '' AS ''
FROM temp_cashier_transactions;

-- Insert cash allocated into final temporary table
INSERT INTO final_temp_cashier_report SELECT 'Cash Allocated' AS '', 
SUM(CASE
WHEN (transaction_type = 'cash_allocated' AND transaction_date BETWEEN asOnDate AND  asOnDate) THEN amount
ELSE 0
END) AS '', '' AS ''
FROM temp_cashier_transactions;

-- Insert cash settled into final temporary table
INSERT INTO final_temp_cashier_report SELECT 'Cash Settled' AS '', 
SUM(CASE
WHEN (transaction_type = 'cash_settled' AND transaction_date BETWEEN asOnDate AND  asOnDate) THEN amount
ELSE 0
END) AS '', '' AS ''
FROM temp_cashier_transactions;

-- Insert cash settled into final temporary table
INSERT INTO final_temp_cashier_report 
SELECT 'Account Transfers' AS '', 
SUM(CASE
WHEN (transaction_type = 'transfers' AND transaction_date BETWEEN asOnDate AND  asOnDate) THEN amount
ELSE 0
END) AS '', '' AS ''
FROM temp_cashier_transactions;

-- Insert other payment type  into final temporary table
INSERT INTO final_temp_cashier_report 
SELECT replace(transaction_type, '_', ' ') AS '', 
SUM(CASE
WHEN (transaction_type LIKE '%_in') THEN amount
WHEN (transaction_type LIKE '%_out') THEN amount
ELSE 0
END) AS '', '' AS ''
FROM temp_cashier_transactions
WHERE transaction_type NOT IN ('cash_allocated', 'cash_settled', 'cash_in', 'cash_out', 'transfers') 
AND transaction_date BETWEEN asOnDate AND  asOnDate
GROUP BY transaction_type;

-- SELECT * FROM temp_cashier_transactions;
SELECT * FROM final_temp_cashier_report;

-- Dropping at the end
DROP TEMPORARY TABLE IF EXISTS temp_cashier_transactions;

-- Dropping at the end
DROP TEMPORARY TABLE IF EXISTS final_temp_cashier_report;

END $$
DELIMITER ;

INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('Daily Teller Cash Report (Pentaho)', 'Pentaho', NULL, NULL, NULL, 'Daily Teller Cash Report', 1, 1);

INSERT INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `selectOne`, `selectAll`, `parameter_sql`, `parent_id`) VALUES ('tellerIdSelectOne', 'tellerId', 'Teller', 'select', 'number', '0', 'Y', 'N', 'select id, name from m_tellers where office_id = ${officeId}', (select id from (select id from stretchy_parameter where parameter_name='OfficeIdSelectOne') as x));

INSERT INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `selectOne`, `selectAll`, `parameter_sql`, `parent_id`) VALUES ('cashierIdSelectOne', 'cashierId', 'Cashier', 'select', 'number', '0', 'Y', 'N', 'select c.id, s.display_name from m_cashiers as c left join m_staff as s on c.staff_id = s.id where c.teller_id = ${tellerId}', (select id from (select id from stretchy_parameter where parameter_name='tellerIdSelectOne') as x));

INSERT INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `selectOne`, `selectAll`, `parameter_sql`, `parent_id`) VALUES ('currencyCodeSelectOne', 'currencyCode', 'Currency', 'select', 'string', '0', 'Y', 'N', 'select `code`, `name` from m_organisation_currency order by `code`', null);

INSERT INTO stretchy_report_parameter (report_id, parameter_id, report_parameter_name) VALUES ((select sr.id From stretchy_report sr where sr.report_name='Daily Teller Cash Report (Pentaho)'),(select sp.id from stretchy_parameter sp where sp.parameter_name='OfficeIdSelectOne'), 'officeId');

INSERT INTO stretchy_report_parameter (report_id, parameter_id, report_parameter_name) VALUES ((select sr.id From stretchy_report sr where sr.report_name='Daily Teller Cash Report (Pentaho)'),(select sp.id from stretchy_parameter sp where sp.parameter_name='tellerIdSelectOne'), 'tellerId');

INSERT INTO stretchy_report_parameter (report_id, parameter_id, report_parameter_name) VALUES ((select sr.id From stretchy_report sr where sr.report_name='Daily Teller Cash Report (Pentaho)'),(select sp.id from stretchy_parameter sp where sp.parameter_name='cashierIdSelectOne'), 'cashierId');

INSERT INTO stretchy_report_parameter (report_id, parameter_id, report_parameter_name) VALUES ((select sr.id From stretchy_report sr where sr.report_name='Daily Teller Cash Report (Pentaho)'),(select sp.id from stretchy_parameter sp where sp.parameter_name='currencyCodeSelectOne'), 'currencyCode');

INSERT INTO stretchy_report_parameter (report_id, parameter_id, report_parameter_name) VALUES ((select sr.id From stretchy_report sr where sr.report_name='Daily Teller Cash Report (Pentaho)'),(select sp.id from stretchy_parameter sp where sp.parameter_name='asOnDate'), 'asOnDate');

INSERT INTO m_permission (grouping,code,entity_name,action_name,can_maker_checker) VALUES ('report','READ_Daily Teller Cash Report (Pentaho)','Daily Teller Cash Report (Pentaho)','READ',0);