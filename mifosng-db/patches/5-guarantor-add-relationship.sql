-- Adding new system defined code for tracking guarantor relation ship type
insert into m_code (code_name,is_system_defined) values ('GuarantorRelationship',1);

-- Adding a few Default Guarantor Relationships

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Spouse",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Parent",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Sibling",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Business Associate",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

insert into m_code_value (code_id,code_value,order_position) 
	select id,"Other",0 
	from m_code 
	where m_code.code_name="GuarantorRelationship";

-- Add a column to link Guarantor to Code value
ALTER TABLE `m_guarantor`
	ADD COLUMN `client_reln_cv_id` INT(11) NULL DEFAULT NULL AFTER `loan_id`,
	ADD CONSTRAINT `FK_m_guarantor_m_code_value` FOREIGN KEY (`client_reln_cv_id`) REFERENCES `m_code_value` (`id`);