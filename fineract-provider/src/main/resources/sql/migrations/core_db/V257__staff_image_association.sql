ALTER TABLE `m_staff` 
	ADD COLUMN `image_id` BIGINT(20) NULL,
	ADD CONSTRAINT `FK_m_staff_m_image` FOREIGN KEY (`image_id`) REFERENCES `m_image` (`id`);

INSERT INTO m_permission (
grouping ,
code ,
entity_name ,
action_name ,
can_maker_checker
) VALUES
('portfolio', 'READ_STAFFIMAGE', 'STAFFIMAGE', 'READ', '0'),
('portfolio', 'CREATE_STAFFIMAGE', 'STAFFIMAGE', 'CREATE', '1'),
('portfolio', 'CREATE_STAFFIMAGE_CHECKER', 'STAFFIMAGE', 'CREATE', '0'),
('portfolio', 'DELETE_STAFFIMAGE', 'STAFFIMAGE', 'DELETE', '1'),
('portfolio', 'DELETE_STAFFIMAGE_CHECKER', 'STAFFIMAGE', 'DELETE', '0');