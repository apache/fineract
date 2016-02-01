update stretchy_report_parameter scr set scr.report_parameter_name='selectLoan' where scr.report_id=(select sr.id from    
stretchy_report  sr where report_name='Client Loan Account Schedule')
and scr.parameter_id=(select sp.id from  stretchy_parameter sp where parameter_label = 'Enter Account No');