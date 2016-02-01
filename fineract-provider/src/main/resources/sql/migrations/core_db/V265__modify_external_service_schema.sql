


alter table c_external_service drop index name;

Rename table c_external_service to c_external_service_properties;

CREATE TABLE `c_external_service` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(100) NULL DEFAULT NULL,	
	PRIMARY KEY (`id`),
	UNIQUE KEY `name_UNIQUE` (`name`)
) 
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

insert into `c_external_service` ( `name`) values( 'S3');

Alter table c_external_service_properties 
	ADD COLUMN `external_service_id` BIGINT(20) NULL DEFAULT NULL;

update c_external_service_properties set external_service_id = (select id from c_external_service where name = 'S3');

ALTER TABLE `c_external_service_properties`
    CHANGE COLUMN `external_service_id` `external_service_id` BIGINT(20) NOT NULL;

ALTER TABLE `c_external_service_properties`
    ADD CONSTRAINT `FK_c_external_service_properties_c_external_service` FOREIGN KEY (`external_service_id`) REFERENCES `c_external_service` (`id`);


insert into `c_external_service` ( `name`) values( 'SMTP_Email_Account');

insert into c_external_service_properties (`name`, `value`, `external_service_id`) values('username', 'support@cloudmicrofinance.com', (select id from c_external_service where name = 'SMTP_Email_Account'));

insert into c_external_service_properties (`name`, `value`, `external_service_id`) values('password', 'support80', (select id from c_external_service where name = 'SMTP_Email_Account'));

insert into c_external_service_properties (`name`, `value`, `external_service_id`) values('host', 'smtp.gmail.com', (select id from c_external_service where name = 'SMTP_Email_Account'));

insert into c_external_service_properties (`name`, `value`, `external_service_id`) values('port', '25', (select id from c_external_service where name = 'SMTP_Email_Account'));

insert into c_external_service_properties (`name`, `value`, `external_service_id`) values('useTLS', 'true', (select id from c_external_service where name = 'SMTP_Email_Account'));

insert into m_permission(grouping, code, entity_name, action_name, can_maker_checker) values('externalservices', 'UPDATE_EXTERNALSERVICES', 'EXTERNALSERVICES', 'UPDATE', 0);


	
commit;

