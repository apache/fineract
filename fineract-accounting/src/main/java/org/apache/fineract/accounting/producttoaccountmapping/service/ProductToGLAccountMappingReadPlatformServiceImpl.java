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
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeOffReasonToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductToGLAccountMappingReadPlatformServiceImpl implements ProductToGLAccountMappingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    private final ProductToGLAccountMappingRepository productToGLAccountMappingRepository;

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForLoanProduct(final Long loanProductId, final Integer accountingType) {

        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        final List<ProductToGLAccountMapping> mappings = productToGLAccountMappingRepository.findAllRegularMappings(loanProductId,
                PortfolioProductType.LOAN.getValue());

        if (AccountingValidations.isCashBasedAccounting(accountingType)) {

            for (final ProductToGLAccountMapping mapping : mappings) {

                final CashAccountsForLoan glAccountForLoan = CashAccountsForLoan.fromInt(mapping.getFinancialAccountType());

                final GLAccountData gLAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                        .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());

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

            for (ProductToGLAccountMapping mapping : mappings) {
                final AccrualAccountsForLoan glAccountForLoan = AccrualAccountsForLoan.fromInt(mapping.getFinancialAccountType());

                final GLAccountData gLAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                        .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());

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

        final List<ProductToGLAccountMapping> mappings = productToGLAccountMappingRepository.findAllRegularMappings(savingsProductId,
                PortfolioProductType.SAVING.getValue());

        Map<String, Object> accountMappingDetails = null;
        if (AccountingValidations.isCashBasedAccounting(accountingType)) {
            accountMappingDetails = setCashSavingsProductToGLAccountMaps(mappings);

        } else if (AccountingValidations.isAccrualPeriodicBasedAccounting(accountingType)) {
            accountMappingDetails = setAccrualPeriodicSavingsProductToGLAccountMaps(mappings);

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
        final List<ProductToGLAccountMapping> mappings = productToGLAccountMappingRepository.findAllPaymentTypeMappings(loanProductId,
                portfolioProductType.getValue());

        List<PaymentTypeToGLAccountMapper> paymentTypeToGLAccountMappers = mappings.isEmpty() ? null : new ArrayList<>();
        for (final ProductToGLAccountMapping mapping : mappings) {
            final PaymentTypeData paymentTypeData = PaymentTypeData.instance(mapping.getPaymentType().getId(),
                    mapping.getPaymentType().getName());
            final GLAccountData gLAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                    .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());

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
        final List<ProductToGLAccountMapping> mappings = penalty
                ? productToGLAccountMappingRepository.findAllPenaltyMappings(loanProductId, portfolioProductType.getValue())
                : productToGLAccountMappingRepository.findAllFeeMappings(loanProductId, portfolioProductType.getValue());

        List<ChargeToGLAccountMapper> chargeToGLAccountMappers = mappings.isEmpty() ? null : new ArrayList<>();
        for (final ProductToGLAccountMapping mapping : mappings) {
            final GLAccountData gLAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                    .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());
            final ChargeData chargeData = ChargeData.builder().id(mapping.getCharge().getId()).name(mapping.getCharge().getName())
                    .penalty(mapping.getCharge().isPenalty()).build();
            final ChargeToGLAccountMapper chargeToGLAccountMapper = new ChargeToGLAccountMapper().setCharge(chargeData)
                    .setIncomeAccount(gLAccountData);
            chargeToGLAccountMappers.add(chargeToGLAccountMapper);
        }
        return chargeToGLAccountMappers;
    }

    private List<ChargeOffReasonToGLAccountMapper> fetchChargeOffReasonMappings(final PortfolioProductType portfolioProductType,
            final Long loanProductId) {
        final List<ProductToGLAccountMapping> mappings = productToGLAccountMappingRepository.findAllChargeOffReasonsMappings(loanProductId,
                portfolioProductType.getValue());
        List<ChargeOffReasonToGLAccountMapper> chargeOffReasonToGLAccountMappers = mappings.isEmpty() ? null : new ArrayList<>();
        for (final ProductToGLAccountMapping mapping : mappings) {
            final Long glAccountId = mapping.getGlAccount().getId();
            final String glAccountName = mapping.getGlAccount().getName();
            final String glCode = mapping.getGlAccount().getGlCode();
            final GLAccountData chargeOffExpenseAccount = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);
            final Long chargeOffReasonId = mapping.getChargeOffReason().getId();
            final String codeValue = mapping.getChargeOffReason().getLabel();
            final String codeDescription = mapping.getChargeOffReason().getDescription();
            final Integer orderPosition = mapping.getChargeOffReason().getPosition();
            final boolean isActive = mapping.getChargeOffReason().isActive();
            final boolean isMandatory = mapping.getChargeOffReason().isMandatory();
            final CodeValueData chargeOffReasonsCodeValue = CodeValueData.builder().id(chargeOffReasonId).name(codeValue)
                    .description(codeDescription).position(orderPosition).active(isActive).mandatory(isMandatory).build();

            final ChargeOffReasonToGLAccountMapper chargeOffReasonToGLAccountMapper = new ChargeOffReasonToGLAccountMapper()
                    .setChargeOffReasonCodeValue(chargeOffReasonsCodeValue).setExpenseAccount(chargeOffExpenseAccount);
            chargeOffReasonToGLAccountMappers.add(chargeOffReasonToGLAccountMapper);
        }
        return chargeOffReasonToGLAccountMappers;
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetailsForShareProduct(Long productId, Integer accountingType) {

        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        final List<ProductToGLAccountMapping> mappings = productToGLAccountMappingRepository.findAllRegularMappings(productId,
                PortfolioProductType.SHARES.getValue());

        if (AccountingRuleType.CASH_BASED.getValue().equals(accountingType)) {
            for (final ProductToGLAccountMapping mapping : mappings) {
                final CashAccountsForShares glAccountForShares = CashAccountsForShares.fromInt(mapping.getFinancialAccountType());

                final GLAccountData gLAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                        .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());

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

    @Override
    public List<ChargeOffReasonToGLAccountMapper> fetchChargeOffReasonMappingsForLoanProduct(Long loanProductId) {
        return fetchChargeOffReasonMappings(PortfolioProductType.LOAN, loanProductId);
    }

    private Map<String, Object> setAccrualPeriodicSavingsProductToGLAccountMaps(final List<ProductToGLAccountMapping> mappings) {
        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        for (final ProductToGLAccountMapping mapping : mappings) {

            AccrualAccountsForSavings glAccountForSavings = AccrualAccountsForSavings.fromInt(mapping.getFinancialAccountType());

            if (glAccountForSavings != null) {
                final GLAccountData glAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                        .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());

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
                log.error("Accounting mapping null {}", mapping.getFinancialAccountType());
            }
        }

        return accountMappingDetails;
    }

    private Map<String, Object> setCashSavingsProductToGLAccountMaps(final List<ProductToGLAccountMapping> mappings) {
        final Map<String, Object> accountMappingDetails = new LinkedHashMap<>(8);

        for (final ProductToGLAccountMapping mapping : mappings) {

            CashAccountsForSavings glAccountForSavings = CashAccountsForSavings.fromInt(mapping.getFinancialAccountType());

            if (glAccountForSavings != null) {
                final GLAccountData glAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                        .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());

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
                log.error("Accounting mapping null {}", mapping.getFinancialAccountType());
            }
        }

        return accountMappingDetails;
    }

}
