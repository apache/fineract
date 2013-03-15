

ALTER TABLE stretchy_parameter
 ADD COLUMN `parent_parameter_id` INT(11) NULL  AFTER `parameter_sql` , 
 ADD CONSTRAINT `fk_stretchy_parameter_0001`
 FOREIGN KEY (`parent_parameter_id` )
 REFERENCES stretchy_parameter (`parameter_id` )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
, ADD INDEX `fk_stretchy_parameter_0001_idx` (`parent_parameter_id` ASC) ;
