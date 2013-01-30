package org.mifosplatform.accounting.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.service.ProductToGLAccountMappingReadPlatformService;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.portfolio.loanproduct.domain.AccountingRuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductToGLAccountMappingReadPlatformServiceImpl implements ProductToGLAccountMappingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ProductToGLAccountMappingReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class LoanProductToGLAccountMappingMapper implements RowMapper<Map<String, Object>> {

        public String schema() {
            return " id as id, gl_account_id as glAccountId,product_id as productId,product_type as productType,financial_account_type as financialAccountType"
                    + " from acc_product_mapping " + " where product_type=1 ";
        }

        @Override
        public Map<String, Object> mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long glAccountId = rs.getLong("glAccountId");
            final Long productId = rs.getLong("productId");
            final Integer productType = rs.getInt("productType");
            final Integer financialAccountType = rs.getInt("financialAccountType");

            final Map<String, Object> loanProductToGLAccountMap = new LinkedHashMap<String, Object>(5);
            loanProductToGLAccountMap.put("id", id);
            loanProductToGLAccountMap.put("glAccountId", glAccountId);
            loanProductToGLAccountMap.put("productId", productId);
            loanProductToGLAccountMap.put("productType", productType);
            loanProductToGLAccountMap.put("financialAccountType", financialAccountType);

            return loanProductToGLAccountMap;
        }
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForLoanProduct(final Long loanProductId, final Integer accountingType) {

        final Map<String, Object> accountMappingDetails = new LinkedHashMap<String, Object>(8);

        final LoanProductToGLAccountMappingMapper rm = new LoanProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ?";

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId });

        if (AccountingRuleType.CASH_BASED.getValue().equals(accountingType)) {

            for (Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {

                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                CASH_ACCOUNTS_FOR_LOAN glAccountForLoan = CASH_ACCOUNTS_FOR_LOAN.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");

                if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), glAccountId);
                }
            }
        } else if (AccountingRuleType.ACCRUAL_BASED.getValue().equals(accountingType)) {

            for (Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {
                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                ACCRUAL_ACCOUNTS_FOR_LOAN glAccountForLoan = ACCRUAL_ACCOUNTS_FOR_LOAN.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");

                if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), glAccountId);
                }
            }

        }

        return accountMappingDetails;
    }
}