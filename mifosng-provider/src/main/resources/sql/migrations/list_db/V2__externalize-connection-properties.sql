ALTER TABLE `tenants`
  ADD COLUMN `pool_initial_size` int(5) DEFAULT 5 AFTER `auto_update`,
  ADD COLUMN `pool_validation_interval` int(11) DEFAULT 30000,
  ADD COLUMN `pool_remove_abandoned` tinyint(1) DEFAULT 1,
  ADD COLUMN `pool_remove_abandoned_timeout` int(5) DEFAULT 60,
  ADD COLUMN `pool_log_abandoned` tinyint(1) DEFAULT 1,
  ADD COLUMN `pool_abandon_when_percentage_full` int(5) DEFAULT 50,
  ADD COLUMN `pool_test_on_borrow` tinyint(1) DEFAULT 1,
  ADD COLUMN `pool_max_active` int(5) DEFAULT 40,
  ADD COLUMN `pool_min_idle` int(5) DEFAULT 20,
  ADD COLUMN `pool_max_idle` int(5) DEFAULT 10,
  ADD COLUMN `pool_suspect_timeout` int(5) DEFAULT 60,
  ADD COLUMN `pool_time_between_eviction_runs_millis` int(11) DEFAULT 34000,
  ADD COLUMN `pool_min_evictable_idle_time_millis` int(11) DEFAULT 60000;