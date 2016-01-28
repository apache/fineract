ALTER TABLE `m_note`
ADD COLUMN `savings_account_id` BIGINT(20) NULL DEFAULT NULL  AFTER `loan_transaction_id`,
ADD COLUMN `savings_account_transaction_id` BIGINT(20) NULL DEFAULT NULL  AFTER `savings_account_id`;

ALTER TABLE `m_note`   ADD CONSTRAINT `FK_savings_account_id`  FOREIGN KEY (`savings_account_id` )  REFERENCES `m_savings_account` (`id` )  ON DELETE NO ACTION  ON UPDATE NO ACTION, ADD INDEX `FK_savings_account_id` (`savings_account_id` ASC) ;


ALTER TABLE `m_savings_account_transaction`
CHANGE COLUMN `is_reversed` `is_reversed` TINYINT(1) NOT NULL AFTER `transaction_type_enum`,
CHANGE COLUMN `balance_end_date_derived` `balance_end_date_derived` DATE NULL DEFAULT NULL  AFTER `amount`,
CHANGE COLUMN `balance_number_of_days_derived` `balance_number_of_days_derived` INT(11) NULL DEFAULT NULL AFTER `balance_end_date_derived`;

ALTER TABLE `m_savings_account`
ADD COLUMN `field_officer_id` bigint(20) DEFAULT NULL AFTER `product_id`,
ADD COLUMN `submittedon_date` DATE NOT NULL AFTER `status_enum`,
ADD COLUMN `submittedon_userid` bigint(20) DEFAULT NULL AFTER `submittedon_date`,
ADD COLUMN `approvedon_date` DATE DEFAULT NULL AFTER `submittedon_userid`,
ADD COLUMN `approvedon_userid` bigint(20) DEFAULT NULL AFTER `approvedon_date`,
ADD COLUMN `rejectedon_date` DATE DEFAULT NULL AFTER `approvedon_userid`,
ADD COLUMN `rejectedon_userid` bigint(20) DEFAULT NULL AFTER `rejectedon_date`,
ADD COLUMN `withdrawnon_date` DATE DEFAULT NULL AFTER `rejectedon_userid`,
ADD COLUMN `withdrawnon_userid` bigint(20) DEFAULT NULL AFTER `withdrawnon_date`;

ALTER TABLE `m_savings_account`
CHANGE COLUMN `activation_date` `activatedon_date` DATE NULL DEFAULT NULL AFTER `withdrawnon_userid`;

ALTER TABLE `m_savings_account`
ADD COLUMN `activatedon_userid` bigint(20) DEFAULT NULL AFTER `activatedon_date`,
ADD COLUMN `closedon_date` DATE DEFAULT NULL AFTER `activatedon_userid`,
ADD COLUMN `closedon_userid` bigint(20) DEFAULT NULL AFTER `closedon_date`;

UPDATE `m_savings_account`
SET
`submittedon_date`=`activatedon_date`,
`approvedon_date`=`activatedon_date`;


INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`)
VALUES
('transaction_savings', 'APPROVE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'APPROVE', 1),
('transaction_savings', 'REJECT_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'REJECT', 1),
('transaction_savings', 'WITHDRAW_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'WITHDRAW', 1),
('transaction_savings', 'APPROVALUNDO_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'APPROVALUNDO', 1),
('transaction_savings', 'CLOSE_SAVINGSACCOUNT', 'SAVINGSACCOUNT', 'CLOSE', 1);

INSERT INTO `m_permission`
(`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`)
VALUES
('transaction_savings', 'APPROVE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'APPROVE', 0),
('transaction_savings', 'REJECT_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'REJECT', 0),
('transaction_savings', 'WITHDRAW_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'WITHDRAW', 0),
('transaction_savings', 'APPROVALUNDO_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'APPROVALUNDO', 0),
('transaction_savings', 'CLOSE_SAVINGSACCOUNT_CHECKER', 'SAVINGSACCOUNT', 'CLOSE', 0);


-- Remove permissions with 'in the past' permissions

DELETE FROM `m_permission` WHERE `id`='210';

DELETE FROM `m_permission` WHERE `id`='212';
DELETE FROM `m_permission` WHERE `id`='214';
DELETE FROM `m_permission` WHERE `id`='217';
DELETE FROM `m_permission` WHERE `id`='220';
DELETE FROM `m_permission` WHERE `id`='233';
DELETE FROM `m_permission` WHERE `id`='235';
DELETE FROM `m_permission` WHERE `id`='237';
DELETE FROM `m_permission` WHERE `id`='240';
DELETE FROM `m_permission` WHERE `id`='243';