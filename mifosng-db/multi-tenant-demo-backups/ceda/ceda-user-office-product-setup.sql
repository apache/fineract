UPDATE `mifostenant-ceda`.`m_appuser` SET `username` = 'admin' WHERE id=1;

INSERT INTO `mifostenant-ceda`.`m_appuser`
(`is_deleted`,
`office_id`,
`username`,
`firstname`,
`lastname`,
`password`,
`email`,
`firsttime_login_remaining`,
`nonexpired`,
`nonlocked`,
`nonexpired_credentials`,
`enabled`)
VALUES
(0, 1, 'keithwoodlock', 'Keith', 'Woodlock', 
'4f607e9b6cffbe7d3db92d4bfa3391c7aa751727b4ea29d08fddf9dd72e6e7e3', 'keithwoodlock@gmail.com', 0, 1, 1, 1, 1);

UPDATE `mifostenant-ceda`.`m_office` SET `name` = 'CEDA Microfinance Ltd.' WHERE id=1;

INSERT INTO `mifostenant-ceda`.`m_office`
(
`parent_id`,
`hierarchy`,
`external_id`,
`name`,
`opening_date`)
VALUES 
(1, '.2.', 2, 'Uganda (Kampala)', '2009-01-01');


INSERT INTO `mifostenant-ceda`.`m_staff`
(
`is_loan_officer`,
`office_id`,
`firstname`,
`lastname`,
`display_name`)
VALUES
(1, 1, 'CEDA HO', 'LoanOfficer', 'LoanOfficer, CEDA HO'),
(1, 2, 'Kampala', 'LoanOfficer', 'LoanOfficer, Kampala');
