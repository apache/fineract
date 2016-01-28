ALTER TABLE `c_configuration`
	ADD COLUMN `is_trap_door` boolean NOT NULL DEFAULT '0' AFTER `enabled`;

insert into c_configuration(name, value, enabled, is_trap_door, description) values('rounding-mode', '6', '1', '1', '0 - UP, 1 - DOWN, 2- CEILING, 3- FLOOR, 4- HALF_UP, 5- HALF_DOWN, 6 - HALF_EVEN');