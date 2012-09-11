alter table m_deposit_account drop column `matured_on`;
alter table `m_deposit_account` add column `matures_on_date` datetime NULL DEFAULT NULL after actual_commencement_date;