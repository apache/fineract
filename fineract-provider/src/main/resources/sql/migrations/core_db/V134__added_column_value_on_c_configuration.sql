ALTER TABLE `c_configuration`
ADD COLUMN `value` INT NULL AFTER `name`;

INSERT INTO `c_configuration` (
`id` ,
`name` ,
`value` ,
`enabled`
)
VALUES (
NULL ,  'penalty-wait-period',  '2',  '1'
);
