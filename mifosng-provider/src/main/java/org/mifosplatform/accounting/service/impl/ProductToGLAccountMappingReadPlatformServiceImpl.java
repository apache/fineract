package org.mifosplatform.accounting.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.accounting.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.api.data.ProductToGLAccountMappingData;
import org.mifosplatform.accounting.service.ProductToGLAccountMappingReadPlatformService;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
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

    private static final class LoanProductToGLAccountMappingMapper implements RowMapper<ProductToGLAccountMappingData> {

        public String schema() {
            return " id as id, gl_account_id as glAccountId,product_id as productId,product_type as productType,financial_account_type as financialAccountType"
                    + " from acc_product_mapping " + " where product_type=1 ";
        }

        @Override
        public ProductToGLAccountMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            Long glAccountId = rs.getLong("glAccountId");
            Long productId = rs.getLong("productId");
            Integer productType = rs.getInt("productType");
            Integer financialAccountType = rs.getInt("financialAccountType");

            return new ProductToGLAccountMappingData(id, glAccountId, productId, productType, financialAccountType);
        }
    }

    @Override
    public LoanProductData fetchAccountMappingDetailsForLoanProduct(LoanProductData loanProductData) {
        LoanProductToGLAccountMappingMapper rm = new LoanProductToGLAccountMappingMapper();
        String sql = "select " + rm.schema() + " and product_id = ?";
        List<ProductToGLAccountMappingData> productToGLAccountMappingDatas = this.jdbcTemplate.query(sql, rm,
                new Object[] { loanProductData.getId() });

        if (loanProductData.getAccountingType() == AccountingRuleType.CASH_BASED.getValue()) {
            for (ProductToGLAccountMappingData productToGLAccountMappingData : productToGLAccountMappingDatas) {
                CASH_ACCOUNTS_FOR_LOAN glAccountForLoan = CASH_ACCOUNTS_FOR_LOAN.fromInt(productToGLAccountMappingData
                        .getFinancialAccountType());
                if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE)) {
                    loanProductData.setFundSourceAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES)) {
                    loanProductData.setIncomeFromFeeAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES)) {
                    loanProductData.setIncomeFromPenaltyAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS)) {
                    loanProductData.setInterestOnLoanAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO)) {
                    loanProductData.setLoanPortfolioAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF)) {
                    loanProductData.setWriteOffAccountId(productToGLAccountMappingData.getGlAccountId());
                }
            }
        } else if (loanProductData.getAccountingType() == AccountingRuleType.ACCRUAL_BASED.getValue()) {
            for (ProductToGLAccountMappingData productToGLAccountMappingData : productToGLAccountMappingDatas) {
                ACCRUAL_ACCOUNTS_FOR_LOAN glAccountForLoan = ACCRUAL_ACCOUNTS_FOR_LOAN.fromInt(productToGLAccountMappingData
                        .getFinancialAccountType());
                if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE)) {
                    loanProductData.setFundSourceAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES)) {
                    loanProductData.setIncomeFromFeeAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES)) {
                    loanProductData.setIncomeFromPenaltyAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS)) {
                    loanProductData.setInterestOnLoanAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO)) {
                    loanProductData.setLoanPortfolioAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF)) {
                    loanProductData.setWriteOffAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE)) {
                    loanProductData.setReceivableInterestAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE)) {
                    loanProductData.setReceivableFeeAccountId(productToGLAccountMappingData.getGlAccountId());
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE)) {
                    loanProductData.setReceivablePenaltyAccountId(productToGLAccountMappingData.getGlAccountId());
                }
            }

        }

        return loanProductData;
    }

}
