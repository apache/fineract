INSERT INTO `m_permission` (
`id` ,
`grouping` ,
`code` ,
`entity_name` ,
`action_name` ,
`can_maker_checker`
)
VALUES (
NULL ,  'transaction_loan',  'RECOVERYPAYMENT_LOAN',  'LOAN',  'RECOVERYPAYMENT',  '0'
);


Alter table m_loan
add column total_recovered_derived Decimal(19,6);


INSERT INTO `acc_gl_account` (`name`, `hierarchy`, `gl_code`,`account_usage`, `classification_enum`,`description`)
select 'Loan Recovery (Temp)', '.', '220002-Temp', 1, 4,'Temporary account to track income from Loan recovery'
FROM m_product_loan WHERE accounting_type != 1
limit 1;

INSERT INTO `acc_product_mapping` (`gl_account_id`,`product_id`,`product_type`,`financial_account_type`)
select (select max(id) from acc_gl_account where classification_enum=4 and account_usage=1 LIMIT 1), mapping.product_id, mapping.product_type, 12
from acc_product_mapping mapping
where mapping.financial_account_type = 4 and mapping.product_type=1
group by mapping.product_id;
