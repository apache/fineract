truncate table `r_enum_value`;
INSERT INTO `r_enum_value` 
VALUES ('amortization_method_enum',0,'Equal installments','Equal installments'),('amortization_method_enum',1,'Equal principle payments','Equal principle payments'),
('interest_calculated_in_period_enum',0,'Daily','Daily'),('interest_calculated_in_period_enum',1,'Same as repayment period','Same as repayment period'),
('interest_method_enum',0,'Declining Balance','Declining Balance'),('interest_method_enum',1,'Flat','Flat'),
('interest_period_frequency_enum',2,'Per month','Per month'),('interest_period_frequency_enum',3,'Per year','Per year'),
('loan_status_id',100,'Submitted and awaiting approval','Submitted and awaiting approval'),('loan_status_id',200,'Approved','Approved'),
('loan_status_id',300,'Active','Active'),('loan_status_id',400,'Withdrawn by client','Withdrawn by client'),('loan_status_id',500,'Rejected','Rejected'),
('loan_status_id',600,'Closed','Closed'),('loan_status_id',700,'Overpaid','Overpaid'),
('loan_transaction_strategy_id',1,'mifos-standard-strategy','Mifos style'),('loan_transaction_strategy_id',2,'heavensfamily-strategy','Heavensfamily'),
('loan_transaction_strategy_id',3,'creocore-strategy','Creocore'),('loan_transaction_strategy_id',4,'rbi-india-strategy','RBI (India)'),
('processing_result_enum',0,'invalid','Invalid'),('processing_result_enum',1,'processed','Processed'),
('processing_result_enum',2,'awaiting.approval','Awaiting Approval'),('processing_result_enum',3,'rejected','Rejected'),
('repayment_period_frequency_enum',0,'Days','Days'),('repayment_period_frequency_enum',1,'Weeks','Weeks'),
('repayment_period_frequency_enum',2,'Months','Months'),('term_period_frequency_enum',0,'Days','Days'),
('term_period_frequency_enum',1,'Weeks','Weeks'),('term_period_frequency_enum',2,'Months','Months'),('term_period_frequency_enum',3,'Years','Years');
