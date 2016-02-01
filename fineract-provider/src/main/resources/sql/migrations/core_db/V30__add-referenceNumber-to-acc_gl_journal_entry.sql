alter table `acc_gl_journal_entry`
add column `ref_num` varchar(100) default NULL AFTER `reversed`;