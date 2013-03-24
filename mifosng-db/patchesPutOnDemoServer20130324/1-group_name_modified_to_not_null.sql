ALTER TABLE m_group MODIFY name VARCHAR(100) NOT NULL;
ALTER TABLE m_group DROP INDEX name;
ALTER TABLE m_group DROP INDEX external_id;
ALTER TABLE m_group ADD UNIQUE INDEX name (name, level_Id);
ALTER TABLE m_group ADD UNIQUE INDEX external_id (external_id, level_Id);