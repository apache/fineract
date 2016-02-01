ALTER TABLE `m_appuser`
ADD COLUMN `is_self_service_user` BIT(1) NOT NULL DEFAULT 0;

CREATE TABLE `m_selfservice_user_client_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`appuser_id` BIGINT(20) NOT NULL,
	`client_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `appuser_id_client_id` (`appuser_id`, `client_id`),
	CONSTRAINT `m_selfservice_appuser_id` FOREIGN KEY (`appuser_id`) REFERENCES `m_appuser` (`id`),
	CONSTRAINT `m_selfservice_client_id` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1;