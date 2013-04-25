INSERT INTO c_configuration (name,enabled) VALUES ('amazon-S3',0);


CREATE TABLE IF NOT EXISTS m_external_services (
	name VARCHAR(150) NOT NULL,
	value VARCHAR(250),
	UNIQUE(name) 
);

INSERT INTO m_external_services (name) VALUES ('s3_bucket_name');
INSERT INTO m_external_services (name) VALUES ('s3_access_key');
INSERT INTO m_external_services (name) VALUES ('s3_secret_key');

ALTER TABLE m_document ADD COLUMN storage_type varchar(50);

CREATE TABLE IF NOT EXISTS `m_image`(
	`id` BIGINT NOT NULL AUTO_INCREMENT,
	`client_id` BIGINT NOT NULL,
	`key` varchar(500),
	`storage_type` varchar(50),
	FOREIGN KEY (`client_id`) REFERENCES m_client(`id`),
	PRIMARY KEY (`id`),
	UNIQUE(client_id)
);

UPDATE m_document set storage_type='file_system';

insert into m_image (`client_id`,`key`,`storage_type`) select id,`image_key`,'file_system' from m_client where `image_key` IS NOT NULL;
