alter table m_loan_repayment_schedule
add column `fromdate` date DEFAULT NULL after `loan_id`;

alter table m_charge 
add column `is_penalty` tinyint(1) NOT NULL DEFAULT '0' after `amount`;

alter table m_loan_charge 
add column `is_penalty` tinyint(1) NOT NULL DEFAULT '0' after `charge_id`;