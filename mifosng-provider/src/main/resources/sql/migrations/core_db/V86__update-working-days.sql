TRUNCATE `m_working_days`;
INSERT INTO `m_working_days` (`recurrence`, `repayment_rescheduling_enum`) VALUES ('FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU', 2);