ALTER TABLE `m_product_loan`
	ADD COLUMN `allow_partial_period_interest_calcualtion` TINYINT(1) NOT NULL DEFAULT '0' AFTER `interest_calculated_in_period_enum`;
	
ALTER TABLE `m_loan`
	ADD COLUMN `allow_partial_period_interest_calcualtion` TINYINT(1) NOT NULL DEFAULT '0' AFTER `interest_calculated_in_period_enum`;
	
UPDATE m_product_loan mpl inner join (select mp.id as productId from m_product_loan mp where mp.interest_calculated_in_period_enum = 1 and  (mp.interest_recalculation_enabled = 1 or mp.allow_multiple_disbursals = 1 or mp.is_linked_to_floating_interest_rates =1 or mp.allow_variabe_installments =1)) x on x.productId = mpl.id SET mpl.allow_partial_period_interest_calcualtion = 1;	
	
UPDATE m_loan ml inner join (select loan.id as loanId from m_product_loan mp inner join m_loan loan on loan.product_id = mp.id where mp.allow_partial_period_interest_calcualtion = 1) x on x.loanId = ml.id SET ml.allow_partial_period_interest_calcualtion=1;
	
