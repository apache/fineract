ALTER TABLE `m_loan` 
DROP FOREIGN KEY `FK_m_loan_guarantor`;

ALTER TABLE `m_loan` 
DROP COLUMN `guarantor_id`, 
DROP INDEX `FK_m_loan_guarantor`;

ALTER TABLE `m_loan` 
CHANGE COLUMN `submittedon_date` `submittedon_date` DATE NULL DEFAULT NULL, 
CHANGE COLUMN `approvedon_date` `approvedon_date` DATE NULL DEFAULT NULL,
CHANGE COLUMN `closedon_date` `closedon_date` DATE NULL DEFAULT NULL,
CHANGE COLUMN `rejectedon_date` `rejectedon_date` DATE NULL DEFAULT NULL,
CHANGE COLUMN `rescheduledon_date` `rescheduledon_date` DATE NULL DEFAULT NULL,
CHANGE COLUMN `withdrawnon_date` `withdrawnon_date` DATE NULL DEFAULT NULL,
CHANGE COLUMN `writtenoffon_date` `writtenoffon_date` DATE NULL DEFAULT NULL;

ALTER TABLE `m_loan` 
ADD COLUMN `submittedon_userid` BIGINT(20) DEFAULT NULL AFTER `submittedon_date`,
ADD KEY `FK_submittedon_userid` (`submittedon_userid`),
ADD CONSTRAINT `FK_submittedon_userid` FOREIGN KEY (`submittedon_userid`) REFERENCES `m_appuser` (`id`);


ALTER TABLE `m_loan` 
ADD COLUMN `approvedon_userid` BIGINT(20) DEFAULT NULL AFTER `approvedon_date`,
ADD KEY `FK_approvedon_userid` (`approvedon_userid`),
ADD CONSTRAINT `FK_approvedon_userid` FOREIGN KEY (`approvedon_userid`) REFERENCES `m_appuser` (`id`);


ALTER TABLE `m_loan` 
ADD COLUMN `rejectedon_userid` BIGINT(20) DEFAULT NULL AFTER `rejectedon_date`,
ADD KEY `FK_rejectedon_userid` (`rejectedon_userid`),
ADD CONSTRAINT `FK_rejectedon_userid` FOREIGN KEY (`rejectedon_userid`) REFERENCES `m_appuser` (`id`);


ALTER TABLE `m_loan` 
ADD COLUMN `withdrawnon_userid` BIGINT(20) DEFAULT NULL AFTER `withdrawnon_date`,
ADD KEY `FK_withdrawnon_userid` (`withdrawnon_userid`),
ADD CONSTRAINT `FK_withdrawnon_userid` FOREIGN KEY (`withdrawnon_userid`) REFERENCES `m_appuser` (`id`);

ALTER TABLE `m_loan` 
ADD COLUMN `disbursedon_userid` BIGINT(20) DEFAULT NULL AFTER `disbursedon_date`,
ADD KEY `FK_disbursedon_userid` (`disbursedon_userid`),
ADD CONSTRAINT `FK_disbursedon_userid` FOREIGN KEY (`disbursedon_userid`) REFERENCES `m_appuser` (`id`);

ALTER TABLE `m_loan` 
ADD COLUMN `closedon_userid` BIGINT(20) DEFAULT NULL AFTER `closedon_date`,
ADD KEY `FK_closedon_userid` (`closedon_userid`),
ADD CONSTRAINT `FK_closedon_userid` FOREIGN KEY (`closedon_userid`) REFERENCES `m_appuser` (`id`);