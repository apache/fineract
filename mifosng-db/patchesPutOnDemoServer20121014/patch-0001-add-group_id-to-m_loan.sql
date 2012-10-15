alter table m_loan add column group_id bigint(20) after client_id;
alter table m_loan modify client_id bigint(20);
alter table m_loan add constraint foreign key (`group_id`) references `m_group` (`id`);
