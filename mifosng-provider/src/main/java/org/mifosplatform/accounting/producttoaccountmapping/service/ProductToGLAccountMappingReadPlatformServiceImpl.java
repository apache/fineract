/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.mifosplatform.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductToGLAccountMappingReadPlatformServiceImpl implements ProductToGLAccountMappingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ProductToGLAccountMappingReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class ProductToGLAccountMappingMapper implements RowMapper<Map<String, Object>> {

        public String schema() {
            return " mapping.id as id, mapping.gl_account_id as glAccountId, mapping.product_id as productId, "
                    + " mapping.product_type as productType,mapping.financial_account_type as financialAccountType, "
                    + " mapping.payment_type as paymentTypeId, mapping.charge_id as chargeId, charge.is_penalty as penalty "
                    + " from acc_product_mapping mapping left join m_charge charge on mapping.charge_id=charge.id "
                    + " where mapping.product_type= ? ";
        }

        @Override
        public Map<String, Object> mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long glAccountId = rs.getLong("glAccountId");
            final Long productId = rs.getLong("productId");
            final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentTypeId");
            final Long chargeId = rs.getLong("chargeId");
            final Integer productType = rs.getInt("productType");
            final Integer financialAccountType = rs.getInt("financialAccountType");

            final Map<String, Object> loanProductToGLAccountMap = new LinkedHashMap<String, Object>(5);
            loanProductToGLAccountMap.put("id", id);
            loanProductToGLAccountMap.put("glAccountId", glAccountId);
            loanProductToGLAccountMap.put("productId", productId);
            loanProductToGLAccountMap.put("productType", productType);
            loanProductToGLAccountMap.put("financialAccountType", financialAccountType);
            loanProductToGLAccountMap.put("paymentTypeId", paymentTypeId);
            loanProductToGLAccountMap.put("chargeId", chargeId);

            return loanProductToGLAccountMap;
        }
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForLoanProduct(final Long loanProductId, final Integer accountingType) {

        final Map<String, Object> accountMappingDetails = new LinkedHashMap<String, Object>(8);

        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is null and mapping.charge_id is null";

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, new Object[] {
                PortfolioProductType.LOAN.getValue(), loanProductId });

        if (AccountingRuleType.CASH_BASED.getValue().equals(accountingType)) {

            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {

                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final CASH_ACCOUNTS_FOR_LOAN glAccountForLoan = CASH_ACCOUNTS_FOR_LOAN.fromInt(financialAccountType);

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
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), glAccountId);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), glAccountId);
                }
            }
        } else if (AccountingRuleType.ACCRUAL_BASED.getValue().equals(accountingType)) {

            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {
                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final ACCRUAL_ACCOUNTS_FOR_LOAN glAccountForLoan = ACCRUAL_ACCOUNTS_FOR_LOAN.fromInt(financialAccountType);

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
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), glAccountId);
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

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForSavingsProduct(final Long savingsProductId, final Integer accountingType) {
        final Map<String, Object> accountMappingDetails = new LinkedHashMap<String, Object>(8);

        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is null and mapping.charge_id is null ";

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, new Object[] {
                PortfolioProductType.SAVING.getValue(), savingsProductId });

        if (AccountingRuleType.CASH_BASED.getValue().equals(accountingType)) {

            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {

                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final CASH_ACCOUNTS_FOR_SAVINGS glAccountForSavings = CASH_ACCOUNTS_FOR_SAVINGS.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");

                if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), glAccountId);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), glAccountId);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), glAccountId);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), glAccountId);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), glAccountId);
                }
            }
        }
        return accountMappingDetails;
    }

    @Override
    public List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappingsForLoanProduct(final Long loanProductId) {
        return fetchPaymentTypeToFundSourceMappings(PortfolioProductType.LOAN, loanProductId);
    }

    @Override
    public List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappingsForSavingsProduct(final Long savingsProductId) {
        return fetchPaymentTypeToFundSourceMappings(PortfolioProductType.SAVING, savingsProductId);
    }

    /**
     * @param loanProductId
     * @param paymentTypeToGLAccountMappers
     * @return
     */
    private List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappings(final PortfolioProductType portfolioProductType,
            final Long loanProductId) {
        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is not null";

        final List<Map<String, Object>> paymentTypeToFundSourceMappingsList = this.jdbcTemplate.query(sql, rm, new Object[] {
                portfolioProductType.getValue(), loanProductId });

        List<PaymentTypeToGLAccountMapper> paymentTypeToGLAccountMappers = null;
        for (final Map<String, Object> productToGLAccountMap : paymentTypeToFundSourceMappingsList) {
            if (paymentTypeToGLAccountMappers == null) {
                paymentTypeToGLAccountMappers = new ArrayList<PaymentTypeToGLAccountMapper>();
            }
            final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
            final Long paymentTypeId = (Long) productToGLAccountMap.get("paymentTypeId");
            final PaymentTypeToGLAccountMapper paymentTypeToGLAccountMapper = new PaymentTypeToGLAccountMapper(paymentTypeId, glAccountId);
            paymentTypeToGLAccountMappers.add(paymentTypeToGLAccountMapper);
        }
        return paymentTypeToGLAccountMappers;
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchFeeToIncomeAccountMappingsForLoanProduct(final Long loanProductId) {
        return fetchChargeToIncomeAccountMappings(PortfolioProductType.LOAN, loanProductId, false);
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchPenaltyToIncomeAccountMappingsForLoanProduct(final Long loanProductId) {
        return fetchChargeToIncomeAccountMappings(PortfolioProductType.LOAN, loanProductId, true);
    }

    private List<ChargeToGLAccountMapper> fetchChargeToIncomeAccountMappings(final PortfolioProductType portfolioProductType,
            final Long loanProductId, final boolean penalty) {
        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        String sql = "select " + rm.schema() + " and product_id = ? and mapping.charge_id is not null and charge.is_penalty=";
        if (penalty) {
            sql = sql + " 1";
        } else {
            sql = sql + " 0";
        }

        final List<Map<String, Object>> chargeToFundSourceMappingsList = this.jdbcTemplate.query(sql, rm, new Object[] {
                portfolioProductType.getValue(), loanProductId });
        List<ChargeToGLAccountMapper> chargeToGLAccountMappers = null;
        for (final Map<String, Object> chargeToIncomeAccountMap : chargeToFundSourceMappingsList) {
            if (chargeToGLAccountMappers == null) {
                chargeToGLAccountMappers = new ArrayList<ChargeToGLAccountMapper>();
            }
            final Long glAccountId = (Long) chargeToIncomeAccountMap.get("glAccountId");
            final Long chargeId = (Long) chargeToIncomeAccountMap.get("chargeId");
            final ChargeToGLAccountMapper chargeToGLAccountMapper = new ChargeToGLAccountMapper(chargeId, glAccountId);
            chargeToGLAccountMappers.add(chargeToGLAccountMapper);
        }
        return chargeToGLAccountMappers;
    }
}