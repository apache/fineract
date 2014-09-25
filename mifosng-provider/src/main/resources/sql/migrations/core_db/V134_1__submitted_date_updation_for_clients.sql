UPDATE m_client mc SET mc.submittedon_date=mc.activation_date where mc.submittedon_date is NULL;
UPDATE m_client mc SET mc.submittedon_date=now() where mc.submittedon_date is NULL;

UPDATE m_group mg SET mg.submittedon_date=mg.activation_date where mg.submittedon_date is NULL;
UPDATE m_group mg SET mg.submittedon_date=now() where mg.submittedon_date is NULL;
