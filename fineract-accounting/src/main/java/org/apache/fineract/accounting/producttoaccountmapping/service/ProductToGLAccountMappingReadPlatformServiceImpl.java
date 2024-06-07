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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForShares;
import org.apache.fineract.accounting.common.AccountingConstants.LoanProductAccountingDataParams;
import org.apache.fineract.accounting.common.AccountingConstants.SavingProductAccountingDataParams;
import org.apache.fineract.accounting.common.AccountingConstants.SharesProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.common.AccountingValidations;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductToGLAccountMappingReadPlatformServiceImpl implements ProductToGLAccountMappingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

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

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, // NOSONAR
                new Object[] { PortfolioProductType.LOAN.getValue(), loanProductId });

        if (AccountingValidations.isCashBasedAccounting(accountingType)) {

            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {

                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final CashAccountsForLoan glAccountForLoan = CashAccountsForLoan.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData gLAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);

                if (glAccountForLoan.equals(CashAccountsForLoan.FUND_SOURCE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.FUND_SOURCE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_FEES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_PENALTIES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INTEREST_ON_LOANS)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INTEREST_ON_LOANS.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.LOAN_PORTFOLIO)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.LOAN_PORTFOLIO.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.TRANSFERS_SUSPENSE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.LOSSES_WRITTEN_OFF.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.GOODWILL_CREDIT)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.GOODWILL_CREDIT.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.OVERPAYMENT)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.OVERPAYMENT.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_RECOVERY)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_RECOVERY.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.CHARGE_OFF_EXPENSE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.CHARGE_OFF_EXPENSE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(),
                            gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(),
                            gLAccountData);
                }

            }
        } else if (AccountingValidations.isAccrualBasedAccounting(accountingType)
                || AccountingValidations.isUpfrontAccrualAccounting(accountingType)) {

            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {
                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final AccrualAccountsForLoan glAccountForLoan = AccrualAccountsForLoan.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData gLAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);

                if (glAccountForLoan.equals(AccrualAccountsForLoan.FUND_SOURCE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.FUND_SOURCE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_FEES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_PENALTIES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INTEREST_ON_LOANS)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INTEREST_ON_LOANS.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.LOAN_PORTFOLIO)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.LOAN_PORTFOLIO.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.OVERPAYMENT)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.OVERPAYMENT.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.TRANSFERS_SUSPENSE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.LOSSES_WRITTEN_OFF.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.GOODWILL_CREDIT)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.GOODWILL_CREDIT.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INTEREST_RECEIVABLE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INTEREST_RECEIVABLE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.FEES_RECEIVABLE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.FEES_RECEIVABLE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.PENALTIES_RECEIVABLE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.PENALTIES_RECEIVABLE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_RECOVERY)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_RECOVERY.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.CHARGE_OFF_EXPENSE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.CHARGE_OFF_EXPENSE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(),
                            gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), gLAccountData);
                } else if (glAccountForLoan.equals(AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY)) {
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(),
                            gLAccountData);
                }
            }

        }

        return accountMappingDetails;
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForSavingsProduct(final Long savingsProductId, final Integer accountingType) {
        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is null and mapping.charge_id is null ";

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, // NOSONAR
                new Object[] { PortfolioProductType.SAVING.getValue(), savingsProductId });

        Map<String, Object> accountMappingDetails = null;
        if (AccountingValidations.isCashBasedAccounting(accountingType)) {
            accountMappingDetails = setCashSavingsProductToGLAccountMaps(listOfProductToGLAccountMaps);

        } else if (AccountingValidations.isAccrualPeriodicBasedAccounting(accountingType)) {
            accountMappingDetails = setAccrualPeriodicSavingsProductToGLAccountMaps(listOfProductToGLAccountMaps);

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
     * @return
     */
    private List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappings(final PortfolioProductType portfolioProductType,
            final Long loanProductId) {
        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is not null";

        final List<Map<String, Object>> paymentTypeToFundSourceMappingsList = this.jdbcTemplate.query(sql, rm, // NOSONAR
                new Object[] { portfolioProductType.getValue(), loanProductId });

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

            final GLAccountData gLAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);

            final PaymentTypeToGLAccountMapper paymentTypeToGLAccountMapper = new PaymentTypeToGLAccountMapper()
                    .setPaymentType(paymentTypeData).setFundSourceAccount(gLAccountData);
            paymentTypeToGLAccountMappers.add(paymentTypeToGLAccountMapper);
        }
        return paymentTypeToGLAccountMappers;
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchFeeToGLAccountMappingsForLoanProduct(final Long loanProductId) {
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
            sql = sql + " true";
        } else {
            sql = sql + " false";
        }

        final List<Map<String, Object>> chargeToFundSourceMappingsList = this.jdbcTemplate.query(sql, rm, // NOSONAR
                new Object[] { portfolioProductType.getValue(), loanProductId });
        List<ChargeToGLAccountMapper> chargeToGLAccountMappers = null;
        for (final Map<String, Object> chargeToIncomeAccountMap : chargeToFundSourceMappingsList) {
            if (chargeToGLAccountMappers == null) {
                chargeToGLAccountMappers = new ArrayList<>();
            }
            final Long glAccountId = (Long) chargeToIncomeAccountMap.get("glAccountId");
            final String glAccountName = (String) chargeToIncomeAccountMap.get("glAccountName");
            final String glCode = (String) chargeToIncomeAccountMap.get("glCode");
            final GLAccountData gLAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);
            final Long chargeId = (Long) chargeToIncomeAccountMap.get("chargeId");
            final String chargeName = (String) chargeToIncomeAccountMap.get("chargeName");
            final Boolean penalty1 = (Boolean) chargeToIncomeAccountMap.get("penalty");
            final ChargeData chargeData = ChargeData.lookup(chargeId, chargeName, penalty1);
            final ChargeToGLAccountMapper chargeToGLAccountMapper = new ChargeToGLAccountMapper().setCharge(chargeData)
                    .setIncomeAccount(gLAccountData);
            chargeToGLAccountMappers.add(chargeToGLAccountMapper);
        }
        return chargeToGLAccountMappers;
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForShareProduct(Long productId, Integer accountingType) {

        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        final ProductToGLAccountMappingMapper rm = new ProductToGLAccountMappingMapper();
        final String sql = "select " + rm.schema() + " and product_id = ? and payment_type is null and mapping.charge_id is null ";

        final List<Map<String, Object>> listOfProductToGLAccountMaps = this.jdbcTemplate.query(sql, rm, // NOSONAR
                new Object[] { PortfolioProductType.SHARES.getValue(), productId });

        if (AccountingRuleType.CASH_BASED.getValue().equals(accountingType)) {
            for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {
                final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
                final CashAccountsForShares glAccountForShares = CashAccountsForShares.fromInt(financialAccountType);

                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData gLAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);

                if (glAccountForShares.equals(CashAccountsForShares.SHARES_REFERENCE)) {
                    accountMappingDetails.put(SharesProductAccountingParams.SHARES_REFERENCE.getValue(), gLAccountData);
                } else if (glAccountForShares.equals(CashAccountsForShares.SHARES_SUSPENSE)) {
                    accountMappingDetails.put(SharesProductAccountingParams.SHARES_SUSPENSE.getValue(), gLAccountData);
                } else if (glAccountForShares.equals(CashAccountsForShares.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(SharesProductAccountingParams.INCOME_FROM_FEES.getValue(), gLAccountData);
                } else if (glAccountForShares.equals(CashAccountsForShares.SHARES_EQUITY)) {
                    accountMappingDetails.put(SharesProductAccountingParams.SHARES_EQUITY.getValue(), gLAccountData);
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

    private Map<String, Object> setAccrualPeriodicSavingsProductToGLAccountMaps(
            final List<Map<String, Object>> listOfProductToGLAccountMaps) {
        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {

            final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
            AccrualAccountsForSavings glAccountForSavings = AccrualAccountsForSavings.fromInt(financialAccountType);

            if (glAccountForSavings != null) {
                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData glAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);

                // Assets
                if (glAccountForSavings.equals(AccrualAccountsForSavings.SAVINGS_REFERENCE)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.SAVINGS_REFERENCE.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.FEES_RECEIVABLE)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.FEES_RECEIVABLE.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.PENALTIES_RECEIVABLE)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.PENALTIES_RECEIVABLE.getValue(), glAccountData);
                    // Liabilities
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.SAVINGS_CONTROL)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.SAVINGS_CONTROL.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.TRANSFERS_SUSPENSE.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.INTEREST_PAYABLE)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INTEREST_PAYABLE.getValue(), glAccountData);
                    // Income
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INCOME_FROM_FEES.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INCOME_FROM_PENALTIES.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.INCOME_FROM_INTEREST)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INCOME_FROM_INTEREST.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.ESCHEAT_LIABILITY)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.ESCHEAT_LIABILITY.getValue(), glAccountData);
                    // Expense
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.INTEREST_ON_SAVINGS)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INTEREST_ON_SAVINGS.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(AccrualAccountsForSavings.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.LOSSES_WRITTEN_OFF.getValue(), glAccountData);
                }
            } else {
                log.error("Accounting mapping null {}", financialAccountType);
            }
        }

        return accountMappingDetails;
    }

    private Map<String, Object> setCashSavingsProductToGLAccountMaps(final List<Map<String, Object>> listOfProductToGLAccountMaps) {
        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        for (final Map<String, Object> productToGLAccountMap : listOfProductToGLAccountMaps) {

            final Integer financialAccountType = (Integer) productToGLAccountMap.get("financialAccountType");
            CashAccountsForSavings glAccountForSavings = CashAccountsForSavings.fromInt(financialAccountType);

            if (glAccountForSavings != null) {
                final Long glAccountId = (Long) productToGLAccountMap.get("glAccountId");
                final String glAccountName = (String) productToGLAccountMap.get("glAccountName");
                final String glCode = (String) productToGLAccountMap.get("glCode");
                final GLAccountData glAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);

                // Assets
                if (glAccountForSavings.equals(CashAccountsForSavings.SAVINGS_REFERENCE)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.SAVINGS_REFERENCE.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), glAccountData);
                    // Liabilities
                } else if (glAccountForSavings.equals(CashAccountsForSavings.SAVINGS_CONTROL)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.SAVINGS_CONTROL.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(CashAccountsForSavings.TRANSFERS_SUSPENSE)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.TRANSFERS_SUSPENSE.getValue(), glAccountData);
                    // Income
                } else if (glAccountForSavings.equals(CashAccountsForSavings.INCOME_FROM_FEES)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INCOME_FROM_FEES.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(CashAccountsForSavings.INCOME_FROM_PENALTIES)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INCOME_FROM_PENALTIES.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(CashAccountsForSavings.INCOME_FROM_INTEREST)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INCOME_FROM_INTEREST.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(CashAccountsForSavings.ESCHEAT_LIABILITY)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.ESCHEAT_LIABILITY.getValue(), glAccountData);
                    // Expense
                } else if (glAccountForSavings.equals(CashAccountsForSavings.INTEREST_ON_SAVINGS)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.INTEREST_ON_SAVINGS.getValue(), glAccountData);
                } else if (glAccountForSavings.equals(CashAccountsForSavings.LOSSES_WRITTEN_OFF)) {
                    accountMappingDetails.put(SavingProductAccountingDataParams.LOSSES_WRITTEN_OFF.getValue(), glAccountData);
                }
            } else {
                log.error("Accounting mapping null {}", financialAccountType);
            }
        }

        return accountMappingDetails;
    }

}
