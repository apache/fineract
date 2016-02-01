
CREATE TABLE IF NOT EXISTS `m_password_validation_policy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `regex` text NOT NULL,
  `description` text NOT NULL,
  `active` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;


INSERT INTO `m_password_validation_policy` (
`id` ,
`regex` ,
`description` ,
`active`
)
VALUES (
NULL ,  '^.{1,50}$',  'Password most be at least 1 character and not more that 50 characters long',  '1'
);

INSERT INTO `m_password_validation_policy` (
`id` ,
`regex` ,
`description` ,
`active`
)
VALUES (
NULL ,  '^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{6,50}$',  'Password must be at least 6 characters, no more than 50 characters long, must include at least one upper case letter, one lower case letter, one numeric digit and no space',  '0'
);

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUE ("authorisation","READ_PASSWORD_PREFERENCES","PASSWORD_PREFERENCES","READ",0);

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUE ("authorisation","UPDATE_PASSWORD_PREFERENCES","PASSWORD_PREFERENCES","UPDATE",0);

INSERT INTO m_permission (grouping, code, entity_name, action_name, can_maker_checker)
VALUE ("authorisation","UPDATE_PASSWORD_PREFERENCES_CHECKER","PASSWORD_PREFERENCES","UPDATE_CHECKER",0);





