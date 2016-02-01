/***Store destination office Id while client is pending transfers and effective joining date in a particular branch**/
ALTER TABLE `m_client`
	ADD COLUMN `office_joining_date` DATE NULL AFTER `activation_date`,
	ADD COLUMN `transfer_to_office_id` BIGINT(20) NULL AFTER `office_id`,
	ADD CONSTRAINT `FK_m_client_m_office` FOREIGN KEY (`transfer_to_office_id`) REFERENCES `m_office` (`id`);


/**For current Clients, set the office joining date to activation date**/
update m_client set office_joining_date=activation_date;
