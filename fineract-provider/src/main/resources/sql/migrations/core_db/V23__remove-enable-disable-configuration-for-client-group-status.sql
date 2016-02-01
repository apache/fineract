DELETE FROM `c_configuration` WHERE `name`='allow-pending-client-status';
DELETE FROM `c_configuration` WHERE `name`='allow-pending-group-status';

ALTER TABLE `m_client` DROP COLUMN `is_deleted`;