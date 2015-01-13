ALTER TABLE `m_hook` ADD COLUMN `ugd_template_id` bigint(20) NULL;
ALTER TABLE `m_hook` ADD CONSTRAINT `fk_ugd_template_id` FOREIGN KEY (`ugd_template_id`) REFERENCES `m_template` (`id`);