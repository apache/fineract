DROP INDEX external_id ON portfolio_group;
ALTER TABLE portfolio_group ADD UNIQUE (external_id);
