alter table m_group add column office_id bigint(20) NOT NULL AFTER id;
update m_group set office_id = 1 WHERE office_id = 0;
alter table m_group add constraint foreign key (`office_id`) references `m_office` (`id`);
