ALTER TABLE `m_password_validation_policy` ADD COLUMN `key` VARCHAR(255) NOT NULL;

UPDATE `m_password_validation_policy` pvp SET pvp.`key`='simple' where pvp.id='1' ;
UPDATE `m_password_validation_policy` pvp SET pvp.`key`='secure' where pvp.id='2' ;
