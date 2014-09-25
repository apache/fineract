/***Delete previously set defaults**/
delete from acc_product_mapping where financial_account_type=10 and product_type=2;

/***Set the proper defaults**/
INSERT INTO `acc_product_mapping` (`gl_account_id`,`product_id`,`product_type`,`payment_type`,`charge_id`,`financial_account_type`)
select mapping.gl_account_id,mapping.product_id,mapping.product_type,mapping.payment_type,mapping.charge_id, 10
from acc_product_mapping mapping
where mapping.financial_account_type = 2 and mapping.product_type=2;