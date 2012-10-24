ALTER TABLE `m_client_identifier`
	ADD UNIQUE INDEX `unique_identifier_key` (`document_type_id`, `document_key`);
	
ALTER TABLE `m_client_identifier`
	ADD UNIQUE INDEX `unique_client_identifier` (`client_id`, `document_type_id`);