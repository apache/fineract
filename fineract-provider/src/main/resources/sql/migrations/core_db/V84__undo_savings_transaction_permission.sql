INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`)
VALUES
('transaction_savings', 'UNDOTRANSACTION_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'UNDOTRANSACTION', 1),
('transaction_savings', 'UNDOTRANSACTION_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'UNDOTRANSACTION', 0);