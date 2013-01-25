ALTER TABLE `m_client` 
ADD COLUMN `account_no` VARCHAR(20) NOT NULL AFTER `id`;

UPDATE `m_client`
SET
`account_no` = `id`
WHERE `id` > 0;

ALTER TABLE `m_client` ADD UNIQUE INDEX `account_no_UNIQUE` (`account_no` ASC);



