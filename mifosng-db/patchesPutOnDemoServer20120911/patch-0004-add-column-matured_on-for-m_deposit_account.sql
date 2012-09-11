alter table m_deposit_account drop column actual_maturity_date;
alter table m_deposit_account drop column projected_maturity_date;
alter table `m_deposit_account` add column `matured_on` datetime NULL DEFAULT NULL after actual_commencement_date;