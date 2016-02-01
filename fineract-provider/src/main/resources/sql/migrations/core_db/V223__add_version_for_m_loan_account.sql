ALTER TABLE `m_loan`
	ADD COLUMN `version` INT(15) NOT NULL DEFAULT '1' AFTER `create_standing_instruction_at_disbursement`;
