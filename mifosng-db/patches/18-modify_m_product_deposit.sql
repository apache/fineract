ALTER TABLE m_product_deposit
  CHANGE `minimum_balance` `min_balance` decimal(19,6) NOT NULL,
  CHANGE `maximum_balance` `max_balance` decimal(19,6) NOT NULL,
  CHANGE `maturity_default_interest_rate` `default_interest_rate` decimal(19,6) NOT NULL,
  CHANGE `maturity_min_interest_rate` `min_interest_rate` decimal(19,6) NOT NULL,
  CHANGE `maturity_max_interest_rate` `max_interest_rate` decimal(19,6) NOT NULL,
  CHANGE `min_balance` `min_deposit` decimal(19,6) NOT NULL,
  CHANGE `max_balance` `max_deposit` decimal(19,6) NOT NULL,
  ADD COLUMN `default_deposit` decimal(19,6) NOT NULL AFTER `min_deposit`;