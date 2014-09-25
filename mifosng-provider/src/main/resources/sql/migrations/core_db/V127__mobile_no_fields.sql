ALTER TABLE `m_client`
ADD COLUMN `mobile_no` VARCHAR(50) NULL DEFAULT NULL AFTER `display_name`,
ADD UNIQUE INDEX `mobile_no_UNIQUE` (`mobile_no` ASC);


ALTER TABLE `m_staff`
ADD COLUMN `mobile_no` VARCHAR(50) NULL DEFAULT NULL AFTER `display_name`,
ADD UNIQUE INDEX `mobile_no_UNIQUE` (`mobile_no` ASC);