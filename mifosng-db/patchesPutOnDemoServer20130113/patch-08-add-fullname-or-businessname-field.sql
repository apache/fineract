ALTER TABLE `m_client` 
ADD COLUMN `middlename` VARCHAR(50) DEFAULT NULL AFTER `firstname`;

ALTER TABLE `m_client` 
ADD COLUMN `fullname` VARCHAR(100) DEFAULT NULL AFTER `lastname`;

UPDATE `m_client`
SET
`fullname` = `lastname`
WHERE `firstname` is NULL;

UPDATE `m_client`
SET
`lastname` = null
WHERE `fullname` is not NULL;