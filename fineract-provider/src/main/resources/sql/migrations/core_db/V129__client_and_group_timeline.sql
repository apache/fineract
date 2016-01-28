ALTER TABLE  `m_client` ADD  `submittedon_date` DATE NULL DEFAULT NULL ,
ADD  `submittedon_userid` BIGINT( 20 ) NULL DEFAULT NULL ,
ADD  `activatedon_userid` BIGINT( 20 ) NULL DEFAULT NULL ,
ADD  `closedon_userid` BIGINT( 20 ) NULL DEFAULT NULL ;

ALTER TABLE  `m_group` ADD  `activatedon_userid` BIGINT( 20 ) NULL ,
ADD  `submittedon_date` DATE NULL ,
ADD  `submittedon_userid` BIGINT( 20 ) NULL ,
ADD  `closedon_userid` BIGINT( 20 ) NULL ;