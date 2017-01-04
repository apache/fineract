/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.producttoaccountmapping.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SHARES;
import org.apache.fineract.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS;
import org.apache.fineract.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS;
import org.apache.fineract.accounting.common.AccountingConstants.SHARES_PRODUCT_ACCOUNTING_PARAMS;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
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
            return " mapping.id as id, mapping.gl_account_id as glAccountId,glaccount.name as name,glaccount.gl_code as code,"
                    + " mapping.product_id as productId, mapping.product_type as productType,mapping.financial_account_type as financialAccountType, "
                    + " mapping.payment_type as paymentTypeId,pt.value as paymentTypeValue, mapping.charge_id as chargeId, charge.is_penalty as penalty, "
                    + " charge.name as chargeName "
                    + " from acc_product_mapping mapping left join m_charge charge on mapping.charge_id=charge.id "
                    + " left join acc_gl_account as  glaccount on mapping.gl_account_id = glaccount.id"
                    + " left join m_payment_type pt on mapping.payment_type=pt.id" + " where mapping.product_type= ? ";
        }

        @Override
        public Map<String, Object> mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long glAccountId = rs.getLong("glAccountId");
            final Long productId = rs.getLong("productId");
            final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentTypeId");
            final Long chargeId = rs.getLong("chargeId");
            final Integer productType = rs.getInt("productType");
            final String paymentTypeValue = rs.getString("paymentTypeValue");
            final Integer financialAccountType = rs.getInt("financialAccountType");
            final String glAccountName = rs.getString("name");
            final String glCode = rs.getString("code");
            final String chargeName = rs.getString("chargeName");
            final Boolean penalty = rs.getBoolean("penalty");

            final Map<String, Object> loanProductToGLAccountMap = new LinkedHashMap<>(5);
            loanProductToGLAccountMap.put("id", id);
            loanProductToGLAccountMap.put("glAccountId", glAccountId);
            loanProductToGLAccountMap.put("productId", productId);
            loanProductToGLAccountMap.put("productType", productType);
            loanProductToGLAccountMap.put("financialAccountType", financialAccountType);
            loanProductToGLAccountMap.put("paymentTypeId", paymentTypeId);
            loanProductToGLAccountMap.put("paymentTypeValue", paymentTypeValue);
            loanProductToGLAccountMap.put("chargeId", chargeId);
            loanProductToGLAccountMap.put("chargeName", chargeName);
            loanProductToGLAccountMap.put("penalty", penalty);
            loanProductToGLAccountMap.put("glAccountName", glAccountName);
            loanProductToGLAccountMap.put("glCode", glCode);
            return loanProductToGLAccountMap;
        }
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForLoanProduct(final Long loanProductId, final Integer accountingType) {

        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is null and mapping.charge_id is null";

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, new Object[] {
                PortfolioProductType.LOAN.getValue(), loanProductId });

        if (AccountingRuleType.CASH_BASED.getValue().equals(accountingType)) {

            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {

                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final CASH_ACCOUNTS_FOR_LOAN glAccountForLoan = CASH_ACCOUNTS_FOR_LOAN.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData gLAccountData = new GLAccountData(glAccountId, glAccountName, glCode);

                if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.FUND_SOURCE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_FEES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_PENALTIES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INTEREST_ON_LOANS.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.LOAN_PORTFOLIO.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.TRANSFERS_SUSPENSE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.LOSSES_WRITTEN_OFF.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.OVERPAYMENT)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.OVERPAYMENT.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_RECOVERY)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_RECOVERY.getValue(), gLAccountData);
                }
            }
        } else if (AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(accountingType)
                || AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(accountingType)) {

            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {
                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final ACCRUAL_ACCOUNTS_FOR_LOAN glAccountForLoan = ACCRUAL_ACCOUNTS_FOR_LOAN.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData gLAccountData = new GLAccountData(glAccountId, glAccountName, glCode);

                if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.FUND_SOURCE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_FEES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_PENALTIES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INTEREST_ON_LOANS.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.LOAN_PORTFOLIO.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.OVERPAYMENT)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.OVERPAYMENT.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.TRANSFERS_SUSPENSE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.LOSSES_WRITTEN_OFF.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INTEREST_RECEIVABLE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.FEES_RECEIVABLE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE)) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.PENALTIES_RECEIVABLE.getValue(), gLAccountData);
                } else if ((glAccountForLoan.equals(ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_RECOVERY))) {
                    accountMappingDetails.put(LOAN_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_RECOVERY.getValue(), gLAccountData);
                }
            }

        }

        return accountMappingDetails;
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForSavingsProduct(final Long savingsProductId, final Integer accountingType) {
        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is null and mapping.charge_id is null ";

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, new Object[] {
                PortfolioProductType.SAVING.getValue(), savingsProductId });

        if (AccountingRuleType.CASH_BASED.getValue().equals(accountingType)) {

            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {

                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final CASH_ACCOUNTS_FOR_SAVINGS glAccountForSavings = CASH_ACCOUNTS_FOR_SAVINGS.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData gLAccountData = new GLAccountData(glAccountId, glAccountName, glCode);

                if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.SAVINGS_REFERENCE.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.SAVINGS_CONTROL.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_FEES.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_PENALTIES.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.TRANSFERS_SUSPENSE.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.INTEREST_ON_SAVINGS.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.LOSSES_WRITTEN_OFF.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_INTEREST)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.INCOME_FROM_INTEREST.getValue(), gLAccountData);
                } else if (glAccountForSavings.equals(CASH_ACCOUNTS_FOR_SAVINGS.ESCHEAT_LIABILITY)) {
                    accountMappingDetails.put(SAVINGS_PRODUCT_ACCOUNTING_DATA_PARAMS.ESCHEAT_LIABILITY.getValue(), gLAccountData);
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
                paymentTypeToGLAccountMappers = new ArrayList<>();
            }
            final Long paymentTypeId = (Long) productToGLAccountMap.get("paymentTypeId");
            final String paymentTypeValue = (String) productToGLAccountMap.get("paymentTypeValue");
            final PaymentTypeData paymentTypeData = PaymentTypeData.instance(paymentTypeId, paymentTypeValue);
            final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
            final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
            final String glCode = (String) productToGLAccountMap.get("glCode");
            final GLAccountData gLAccountData = new GLAccountData(glAccountId, glAccountName, glCode);

            final PaymentTypeToGLAccountMapper paymentTypeToGLAccountMapper = new PaymentTypeToGLAccountMapper(paymentTypeData,
                    gLAccountData);
            paymentTypeToGLAccountMappers.add(paymentTypeToGLAccountMapper);
        }
        return paymentTypeToGLAccountMappers;
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchFeeToIncomeOrLiabilityAccountMappingsForLoanProduct(final Long loanProductId) {
        return fetchChargeToIncomeAccountMappings(PortfolioProductType.LOAN, loanProductId, false);
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchPenaltyToIncomeAccountMappingsForLoanProduct(final Long loanProductId) {
        return fetchChargeToIncomeAccountMappings(PortfolioProductType.LOAN, loanProductId, true);
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchFeeToIncomeAccountMappingsForSavingsProduct(Long savingsProductId) {
        return fetchChargeToIncomeAccountMappings(PortfolioProductType.SAVING, savingsProductId, false);
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchPenaltyToIncomeAccountMappingsForSavingsProduct(Long savingsProductId) {
        return fetchChargeToIncomeAccountMappings(PortfolioProductType.SAVING, savingsProductId, true);
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
                chargeToGLAccountMappers = new ArrayList<>();
            }
            final Long glAccountId = (Long) chargeToIncomeAccountMap.get("glAccountId");
            final String glAccountName = (String) chargeToIncomeAccountMap.get("glAccountName");
            final String glCode = (String) chargeToIncomeAccountMap.get("glCode");
            final GLAccountData gLAccountData = new GLAccountData(glAccountId, glAccountName, glCode);
            final Long chargeId = (Long) chargeToIncomeAccountMap.get("chargeId");
            final String chargeName = (String) chargeToIncomeAccountMap.get("chargeName");
            final Boolean penalty1 = (Boolean) chargeToIncomeAccountMap.get("penalty");
            final ChargeData chargeData = ChargeData.lookup(chargeId, chargeName, penalty1);
            final ChargeToGLAccountMapper chargeToGLAccountMapper = new ChargeToGLAccountMapper(chargeData, gLAccountData);
            chargeToGLAccountMappers.add(chargeToGLAccountMapper);
        }
        return chargeToGLAccountMappers;
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForShareProduct(Long productId, Integer accountingType) {

        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is null and mapping.charge_id is null ";

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, new Object[] {
                PortfolioProductType.SHARES.getValue(), productId });

        if (AccountingRuleType.CASH_BASED.getValue().equals(accountingType)) {
            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {
                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final CASH_ACCOUNTS_FOR_SHARES glAccountForShares = CASH_ACCOUNTS_FOR_SHARES.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData gLAccountData = new GLAccountData(glAccountId, glAccountName, glCode);

                if (glAccountForShares.equals(CASH_ACCOUNTS_FOR_SHARES.SHARES_REFERENCE)) {
                    accountMappingDetails.put(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_REFERENCE.getValue(), gLAccountData);
                } else if (glAccountForShares.equals(CASH_ACCOUNTS_FOR_SHARES.SHARES_SUSPENSE)) {
                    accountMappingDetails.put(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_SUSPENSE.getValue(), gLAccountData);
                } else if (glAccountForShares.equals(CASH_ACCOUNTS_FOR_SHARES.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(SHARES_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), gLAccountData);
                } else if (glAccountForShares.equals(CASH_ACCOUNTS_FOR_SHARES.SHARES_EQUITY)) {
                    accountMappingDetails.put(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_EQUITY.getValue(), gLAccountData);
                }
            }
        }
        return accountMappingDetails;

    }

    @Override
    public List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappingsForShareProduct(Long productId) {
        return fetchPaymentTypeToFundSourceMappings(PortfolioProductType.SHARES, productId);
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchFeeToIncomeAccountMappingsForShareProduct(Long productId) {
        return fetchChargeToIncomeAccountMappings(PortfolioProductType.SHARES, productId, false);
    }

}