Alter TABLE `m_saving_account` 
  add column `interest_posting_every` int(11) DEFAULT NULL AFTER `frequency`,
  add column `interest_posting_frequency` int(11) DEFAULT NULL AFTER `interest_posting_every`;