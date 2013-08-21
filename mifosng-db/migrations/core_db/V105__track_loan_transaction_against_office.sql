ALTER TABLE `m_loan_transaction`
    ADD COLUMN `office_id` BIGINT(20) NULL DEFAULT NULL AFTER `loan_id`;

/**update client loans**/
UPDATE m_loan_transaction lt set lt.office_id = (SELECT c.office_id AS officeId FROM m_loan l JOIN m_client c on l.client_id=c.id where l.id=lt.loan_id) where lt.loan_id in (Select l.id from m_loan l where l.client_id is not null);

/**update group loans**/
UPDATE m_loan_transaction lt set lt.office_id = (SELECT g.office_id AS officeId FROM m_loan l JOIN m_group g on l.group_id=g.id where l.id=lt.loan_id) where lt.loan_id in (Select l.id from m_loan l where l.group_id is not null);

/**Add foreign key constraints**/
ALTER TABLE `m_loan_transaction`
    CHANGE COLUMN `office_id` `office_id` BIGINT(20) NOT NULL AFTER `loan_id`;

ALTER TABLE `m_loan_transaction`
    ADD CONSTRAINT `FK_m_loan_transaction_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`);