ALTER TABLE m_code
 ADD COLUMN `is_system_defined` TINYINT(1) NOT NULL DEFAULT '0'  AFTER `code_name` ;

update m_code 
set is_system_defined = true
where code_name = 'Customer Identifier';
