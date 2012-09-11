ALTER TABLE `m_client`
	ADD COLUMN `display_name` VARCHAR(100) NOT NULL AFTER `lastname`;
	
update m_client set display_name = concat_ws(' ',firstname,lastname);