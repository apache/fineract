/*CREATE TABLE `m_loan_transaction_temp` (
	`id` BIGINT(20) NOT NULL,
	`loan_id` BIGINT(20) NOT NULL,
	`amount` DECIMAL(19,6) NOT NULL DEFAULT '0',
	`transaction_date` DATE NOT NULL
);

INSERT INTO m_loan_transaction_temp(`id`,`loan_id`,`transaction_date`,`amount`) select lt.id, lt.loan_id,lt.transaction_date,if(lt.transaction_type_enum = 1 , IFNULL(lt.amount,0),IFNULL(-lt.principal_portion_derived,0)) from m_loan_transaction lt where lt.is_reversed=0;


UPDATE m_loan_transaction lt SET lt.outstanding_loan_balance_derived = (select sum(ltt.amount) from m_loan_transaction_temp ltt where ((ltt.transaction_date = lt.transaction_date and ltt.id  <= lt.id) or ltt.transaction_date < lt.transaction_date) and ltt.loan_id = lt.loan_id) where lt.transaction_type_enum != 10 and lt.is_reversed = 0;

DROP TABLE `m_loan_transaction_temp`;*/

drop table if exists m_loan_transaction_temp;
 
/**Temp table with primary key and indices**/
CREATE TABLE `m_loan_transaction_temp` (
`id` BIGINT(20) NOT NULL,
`loan_id` BIGINT(20) NOT NULL,
`amount` DECIMAL(19,6) NOT NULL DEFAULT '0.000000',
`transaction_date` DATE NOT NULL,
PRIMARY KEY (`id`),
INDEX `loan_id` (`loan_id`),
INDEX `transaction_date` (`transaction_date`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
 
/**Copy data to temp table, skip accrual transaction, greatly reduces table size**/
INSERT INTO m_loan_transaction_temp(`id`,`loan_id`,`transaction_date`,`amount`)
select lt.id, lt.loan_id,lt.transaction_date,if(lt.transaction_type_enum = 1 , IFNULL(lt.amount,0),IFNULL(-lt.principal_portion_derived,0)) from m_loan_transaction lt where lt.is_reversed=0 and lt.transaction_type_enum <> 10;
 
 
/**Drop stored procedure**/
drop procedure if exists update_running_balance;
 
 
/**Update Balance for 500 records at a time using store procedure**/
DELIMITER //
CREATE PROCEDURE `update_running_balance` ()
LANGUAGE SQL
DETERMINISTIC
SQL SECURITY DEFINER
COMMENT 'Update Running Balances for Loans'
BEGIN
 
DECLARE max_transaction INT DEFAULT 0;
DECLARE i INT DEFAULT 0;
DECLARE n INT DEFAULT 0;
DECLARE page_size INT DEFAULT 500;
 
select max(id) into max_transaction from m_loan_transaction;
 
 
SET i = 0;
 
theLoop:LOOP
SET n = i + page_size;
 
IF i < max_transaction THEN
 
START TRANSACTION;
 
UPDATE m_loan_transaction lt SET lt.outstanding_loan_balance_derived = (select sum(ltt.amount) from m_loan_transaction_temp ltt where ((ltt.transaction_date = lt.transaction_date and ltt.id <= lt.id) or ltt.transaction_date < lt.transaction_date) and ltt.loan_id = lt.loan_id) where lt.transaction_type_enum != 10 and lt.is_reversed = 0 and lt.id BETWEEN i AND n;
SET i = i + page_size;
COMMIT;
 
ITERATE theLoop;
 
END IF;
LEAVE theLoop;
END LOOP theLoop;
 
END//
 
/**Call stored procedure**/
call update_running_balance();
 
/**Drop stored procedure**/
drop procedure update_running_balance; 
drop table m_loan_transaction_temp;