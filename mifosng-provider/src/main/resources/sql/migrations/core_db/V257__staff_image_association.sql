ALTER TABLE `m_staff` 
	ADD COLUMN `image_id` BIGINT(20) NULL,
	ADD CONSTRAINT `FK_m_staff_m_image` FOREIGN KEY (`image_id`) REFERENCES `m_image` (`id`);