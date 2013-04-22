ALTER TABLE m_loan
ADD CONSTRAINT `fk_m_group_client_001`
FOREIGN KEY (`group_id` , `client_id` )
REFERENCES m_group_client (`group_id` , `client_id` )
ON DELETE NO ACTION
ON UPDATE NO ACTION
, ADD INDEX `fk_m_group_client_001_idx` (`group_id` ASC, `client_id` ASC) ;