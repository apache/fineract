ALTER TABLE `m_loan` 
ADD COLUMN `account_no` VARCHAR(20) NOT NULL AFTER `id`;

UPDATE `m_loan`
SET
`account_no` = `id`
WHERE `id` > 0;

ALTER TABLE `m_loan` ADD UNIQUE INDEX `loan_account_no_UNIQUE` (`account_no` ASC);


ALTER TABLE `m_loan` DROP INDEX `org_id`;
ALTER TABLE `m_loan` ADD UNIQUE INDEX `loan_externalid_UNIQUE` (`external_id` ASC);