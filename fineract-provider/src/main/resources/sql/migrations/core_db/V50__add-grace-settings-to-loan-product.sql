ALTER TABLE `m_product_loan`
ADD COLUMN `grace_on_principal_periods` SMALLINT(5) NULL DEFAULT NULL AFTER `max_number_of_repayments`,
ADD COLUMN `grace_on_interest_periods` SMALLINT(5) NULL DEFAULT NULL AFTER `grace_on_principal_periods`,
ADD COLUMN `grace_interest_free_periods` SMALLINT(5) NULL DEFAULT NULL AFTER `grace_on_interest_periods`;

ALTER TABLE `m_loan`
ADD COLUMN `grace_on_principal_periods` SMALLINT(5) NULL DEFAULT NULL AFTER `number_of_repayments`,
ADD COLUMN `grace_on_interest_periods` SMALLINT(5) NULL DEFAULT NULL AFTER `grace_on_principal_periods`,
ADD COLUMN `grace_interest_free_periods` SMALLINT(5) NULL DEFAULT NULL AFTER `grace_on_interest_periods`;