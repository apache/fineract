
ALTER TABLE m_group  
	ADD UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC) ;

ALTER TABLE m_product_loan
	ADD COLUMN `external_id` VARCHAR(100) NULL  AFTER `loan_transaction_strategy_id`  , 
	ADD UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC) ; 

ALTER TABLE m_staff
	ADD COLUMN `external_id` VARCHAR(100) NULL  AFTER `display_name`  , 
	ADD UNIQUE INDEX `external_id_UNIQUE` (`external_id` ASC) ; 


/* status_id values for client and group 
0 - Invalid
100 - Pending
300 - Active
600 - Closed( or Exited)
*/

ALTER TABLE m_client 
	ADD COLUMN `status_id` INT(5) NOT NULL  DEFAULT 300 AFTER `is_deleted` ; 


ALTER TABLE m_group 
	ADD COLUMN `status_id` INT(5) NOT NULL  DEFAULT 300 AFTER `is_deleted` ; 


