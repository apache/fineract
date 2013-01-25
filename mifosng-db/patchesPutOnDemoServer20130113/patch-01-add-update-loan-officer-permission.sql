INSERT INTO `m_permission`
(`grouping`,
`code`,
`entity_name`,
`action_name`,
`can_maker_checker`)
VALUES 
('transaction_loan', 'UPDATELOANOFFICER_LOAN', 'LOAN', 'UPDATELOANOFFICER', 1),
('transaction_loan', 'UPDATELOANOFFICER_LOAN_CHECKER', 'LOAN', 'UPDATELOANOFFICER', 0);