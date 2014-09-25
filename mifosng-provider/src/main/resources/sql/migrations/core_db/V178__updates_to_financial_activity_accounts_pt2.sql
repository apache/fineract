update m_permission set entity_name="FINANCIALACTIVITYACCOUNT" where entity_name="OFFICEGLACCOUNT";
update m_permission set code="READ_FINANCIALACTIVITYACCOUNT" where code="READ_OFFICEGLACCOUNT";
update m_permission set code="CREATE_FINANCIALACTIVITYACCOUNT" where code="CREATE_OFFICEGLACCOUNT";
update m_permission set code="DELETE_FINANCIALACTIVITYACCOUNT" where code="DELETE_OFFICEGLACCOUNT";
update m_permission set code="UPDATE_FINANCIALACTIVITYACCOUNT" where code="UPDATE_OFFICEGLACCOUNT";

/*Default Account for tracking account transfer*/
INSERT INTO `acc_gl_account` (`name`, `hierarchy`, `gl_code`,`account_usage`, `classification_enum`,`description`)
select 'Liability Transfer (Temp)', '.', '220004-Temp', 1, 2,'Temporary Liability account to track Account Transfers'
FROM m_product_loan WHERE accounting_type != 1
limit 1;

INSERT INTO `acc_gl_financial_activity_account` (`gl_account_id`,`financial_activity_type`)
select (select max(id) from acc_gl_account where classification_enum=2 and account_usage=1 LIMIT 1), 200
FROM m_product_loan WHERE accounting_type != 1
limit 1;