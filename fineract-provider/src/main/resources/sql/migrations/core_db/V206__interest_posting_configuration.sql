ALTER TABLE c_configuration ADD `description` varchar(300) DEFAULT NULL;

INSERT INTO `c_configuration` (`id`, `name`, `enabled`, `description`) 
VALUES (NULL, 'savings-interest-posting-current-period-end', '0', "Recommended to be changed only once during start of production. When set as false(default), interest will be posted on the first date of next period. If set as true, interest will be posted on last date of current period. There is no difference in the interest amount posted.");

INSERT INTO `c_configuration` (`id`, `name`, `value`, `enabled`, `description`)
VALUES (NULL, 'financial-year-beginning-month', '1', '1', "Recommended to be changed only once during start of production. Allowed values 1 - 12 (January - December). Interest posting periods are evaluated based on this configuration.");
