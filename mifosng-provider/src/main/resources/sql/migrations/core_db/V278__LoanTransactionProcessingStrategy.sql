UPDATE `ref_loan_transaction_processing_strategy` SET `name`='Penalties, Fees, Interest, Principal order'
WHERE code='mifos-standard-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `name`='Overdue/Due Fee/Int,Principal' WHERE code='rbi-india-strategy' ; 
UPDATE `ref_loan_transaction_processing_strategy` SET `name`='HeavensFamily Unique, then list order' WHERE code='heavensfamily-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `name`='Creocore Unique, then list order' WHERE code='creocore-strategy' ;

ALTER TABLE `ref_loan_transaction_processing_strategy`ADD `sort_order` INT(4) ;

UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=1 WHERE code='mifos-standard-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=2 WHERE code='rbi-india-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=3 WHERE code='principal-interest-penalties-fees-order-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=4 WHERE code='interest-principal-penalties-fees-order-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=5 WHERE code='early-repayment-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=6 WHERE code='heavensfamily-strategy' ;
UPDATE `ref_loan_transaction_processing_strategy` SET `sort_order`=7 WHERE code='creocore-strategy' ;



