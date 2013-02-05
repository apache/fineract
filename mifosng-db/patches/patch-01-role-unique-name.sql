UPDATE m_role SET name = CONCAT(id,'-',name);

ALTER TABLE m_role
ADD CONSTRAINT unq_name UNIQUE (name);