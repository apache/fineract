CREATE TABLE `m_savings_officer_assignment_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL,
  `savings_officer_id` bigint(20) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_m_savings_officer_assignment_history_0001` (`account_id`),
  KEY `fk_m_savings_officer_assignment_history_0002` (`savings_officer_id`),
  CONSTRAINT `fk_m_savings_officer_assignment_history_0001` FOREIGN KEY (`account_id`) REFERENCES `m_savings_account` (`id`),
  CONSTRAINT `fk_m_savings_officer_assignment_history_0002` FOREIGN KEY (`savings_officer_id`) REFERENCES `m_staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into m_permission (grouping,code,entity_name,action_name) values ('portfolio','REMOVESAVINGSOFFICER_SAVINGSACCOUNT','SAVINGSACCOUNT','REMOVESAVINGSOFFICER');
insert into m_permission (grouping,code,entity_name,action_name) values ('portfolio','UPDATESAVINGSOFFICER_SAVINGSACCOUNT','SAVINGSACCOUNT','UPDATESAVINGSOFFICER');

