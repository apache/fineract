ALTER TABLE `m_client_identifier`
  ADD COLUMN `status` INT(5) NOT NULL DEFAULT 300 AFTER `document_key`,
  DROP INDEX `unique_client_identifier`,
  ADD COLUMN `active` INT(5) NULL DEFAULT NULL AFTER `status`,
  ADD UNIQUE INDEX `unique_active_client_identifier` (`client_id`, `document_type_id`, `active` );
  