ALTER TABLE `tenants`
  ADD COLUMN `deadlock_max_retries` int(5) DEFAULT 0,
  ADD COLUMN `deadlock_max_retry_interval` int(5) DEFAULT 1;