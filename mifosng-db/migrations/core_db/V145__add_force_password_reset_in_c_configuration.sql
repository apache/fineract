INSERT INTO `c_configuration` (`id`, `name`, `value`, `enabled`) VALUES (NULL, 'force-password-reset-days', '0', '0');

ALTER TABLE  `m_appuser` ADD  `last_time_password_updated` DATE NOT NULL ,
ADD INDEX (  `last_time_password_updated` ) ;

UPDATE  `m_appuser` SET  `last_time_password_updated` =  NOW() WHERE  `m_appuser`.`last_time_password_updated` ='0000-00-00';

CREATE TABLE IF NOT EXISTS `m_appuser_previous_password` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `removal_date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

ALTER TABLE m_appuser_previous_password
ADD FOREIGN KEY (user_id) REFERENCES m_appuser(id);