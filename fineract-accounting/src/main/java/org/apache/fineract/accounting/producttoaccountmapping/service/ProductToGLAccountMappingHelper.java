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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.LoanProductAccountingParams;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingInvalidException;
import org.apache.fineract.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingNotFoundException;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductToGLAccountMappingHelper {

    protected static final List<GLAccountType> ASSET_LIABILITY_TYPES = List.of(GLAccountType.ASSET, GLAccountType.LIABILITY);
    private static final Integer GL_ACCOUNT_EXPENSE_TYPE = 5;

    protected final GLAccountRepository accountRepository;
    protected final ProductToGLAccountMappingRepository accountMappingRepository;
    protected final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;
    protected final GLAccountRepositoryWrapper accountRepositoryWrapper;
    private final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper;
    private final CodeValueRepository codeValueRepository;

    public void saveProductToAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId, final GLAccountType expectedAccountType, final PortfolioProductType portfolioProductType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        if (accountId != null) { // optional entries may be null
            final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);

            final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping().setGlAccount(glAccount).setProductId(productId)
                    .setProductType(portfolioProductType.getValue()).setFinancialAccountType(placeHolderTypeId);
            this.accountMappingRepository.saveAndFlush(accountMapping);
        }
    }

    public void mergeProductToAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes,
            final GLAccountType expectedAccountType, final PortfolioProductType portfolioProductType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);

        // get the existing product
        if (accountId != null) {
            final ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(productId,
                    portfolioProductType.getValue(), accountTypeId);
            if (accountMapping == null) {
                ArrayList<String> optionalProductToGLAccountMappingEntries = new ArrayList<>();
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.GOODWILL_CREDIT.getValue());
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue());
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue());
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue());
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue());
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue());
                optionalProductToGLAccountMappingEntries.add("incomeFromGoodwillCreditInterestAccountId");
                optionalProductToGLAccountMappingEntries.add("incomeFromGoodwillCreditFeesAccountId");
                optionalProductToGLAccountMappingEntries.add("incomeFromGoodwillCreditPenaltyAccountId");
                optionalProductToGLAccountMappingEntries.add("interestReceivableAccountId");
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.DEFERRED_INCOME_LIABILITY.getValue());
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.INCOME_FROM_CAPITALIZATION.getValue());
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.BUY_DOWN_EXPENSE.getValue());
                optionalProductToGLAccountMappingEntries.add(LoanProductAccountingParams.INCOME_FROM_BUY_DOWN.getValue());

                if (optionalProductToGLAccountMappingEntries.contains(paramName)) {
                    saveProductToAccountMapping(element, paramName, productId, accountTypeId, expectedAccountType, portfolioProductType);
                } else {
                    throw new ProductToGLAccountMappingNotFoundException(portfolioProductType, productId, accountTypeName);
                }
            } else {
                if (accountMapping.getGlAccount() != null && !Objects.equals(accountMapping.getGlAccount().getId(), accountId)) {
                    final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
                    changes.put(paramName, accountId);
                    accountMapping.setGlAccount(glAccount);
                    this.accountMappingRepository.saveAndFlush(accountMapping);
                }
            }
        }
    }

    public void createOrmergeProductToAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final Map<String, Object> changes, final GLAccountType expectedAccountType,
            final PortfolioProductType portfolioProductType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);

        // get the existing product
        if (accountId != null) {
            final ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(productId,
                    portfolioProductType.getValue(), accountTypeId);
            if (accountMapping == null) {
                final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
                changes.put(paramName, accountId);
                ProductToGLAccountMapping newAccountMapping = new ProductToGLAccountMapping().setGlAccount(glAccount)
                        .setProductId(productId).setProductType(portfolioProductType.getValue()).setFinancialAccountType(accountTypeId);
                this.accountMappingRepository.saveAndFlush(newAccountMapping);
            } else if (accountMapping.getGlAccount() != null && !Objects.equals(accountMapping.getGlAccount().getId(), accountId)) {
                final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
                changes.put(paramName, accountId);
                accountMapping.setGlAccount(glAccount);
                this.accountMappingRepository.saveAndFlush(accountMapping);
            }
        }
    }

    /**
     * Saves the payment type to Fund source mappings for a particular product/product type (also populates the changes
     * array if passed in)
     *
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    public void savePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType) {
        final JsonArray paymentChannelMappingArray = this.fromApiJsonHelper
                .extractJsonArrayNamed(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
        if (paymentChannelMappingArray != null) {
            if (changes != null) {
                changes.put(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
                        command.jsonFragment(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue()));
            }
            for (int i = 0; i < paymentChannelMappingArray.size(); i++) {
                final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                final Long paymentTypeId = jsonObject.get(LoanProductAccountingParams.PAYMENT_TYPE.getValue()).getAsLong();
                final Long paymentSpecificFundAccountId = jsonObject.get(LoanProductAccountingParams.FUND_SOURCE.getValue()).getAsLong();
                savePaymentChannelToFundSourceMapping(productId, paymentTypeId, paymentSpecificFundAccountId, portfolioProductType);
            }
        }
    }

    /**
     * Saves the Charge to Income / Liability account mappings for a particular product/product type (also populates the
     * changes array if passed in)
     *
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    public void saveChargesToGLAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType, final boolean isPenalty) {
        String arrayName;
        if (isPenalty) {
            arrayName = LoanProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            arrayName = LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(arrayName, element);
        if (chargeToIncomeAccountMappingArray != null) {
            if (changes != null) {
                changes.put(LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue(),
                        command.jsonFragment(LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue()));
            }
            for (int i = 0; i < chargeToIncomeAccountMappingArray.size(); i++) {
                final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                final Long chargeId = jsonObject.get(LoanProductAccountingParams.CHARGE_ID.getValue()).getAsLong();
                final Long incomeAccountId = jsonObject.get(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue()).getAsLong();
                saveChargeToFundSourceMapping(productId, chargeId, incomeAccountId, portfolioProductType, isPenalty);
            }
        }
    }

    public void saveReasonToGLAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType,
            final LoanProductAccountingParams arrayNameParam, final LoanProductAccountingParams reasonCodeValueIdParam,
            final CashAccountsForLoan cashAccountsForLoan) {

        final String arrayName = arrayNameParam.getValue();
        final JsonArray reasonToExpenseAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(arrayName, element);

        if (reasonToExpenseAccountMappingArray != null) {
            if (changes != null) {
                changes.put(arrayName, command.jsonFragment(arrayName));
            }

            for (int i = 0; i < reasonToExpenseAccountMappingArray.size(); i++) {
                final JsonObject jsonObject = reasonToExpenseAccountMappingArray.get(i).getAsJsonObject();
                final Long reasonId = jsonObject.get(reasonCodeValueIdParam.getValue()).getAsLong();
                final Long expenseAccountId = jsonObject.get(LoanProductAccountingParams.EXPENSE_GL_ACCOUNT_ID.getValue()).getAsLong();

                saveReasonToExpenseMapping(productId, reasonId, expenseAccountId, portfolioProductType, cashAccountsForLoan);
            }
        }
    }

    public void saveClassificationToGLAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType,
            final LoanProductAccountingParams classificationParameter) {

        final String arrayName = classificationParameter.getValue();
        final JsonArray classificationToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(arrayName, element);

        if (classificationToIncomeAccountMappingArray != null) {
            if (changes != null) {
                changes.put(arrayName, command.jsonFragment(arrayName));
            }

            for (int i = 0; i < classificationToIncomeAccountMappingArray.size(); i++) {
                final JsonObject jsonObject = classificationToIncomeAccountMappingArray.get(i).getAsJsonObject();
                final Long classificationId = jsonObject.get(LoanProductAccountingParams.CLASSIFICATION_CODE_VALUE_ID.getValue())
                        .getAsLong();
                final Long incomeAccountId = jsonObject.get(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue()).getAsLong();

                saveClassificationToIncomeMapping(productId, classificationId, incomeAccountId, portfolioProductType,
                        classificationParameter);
            }
        }
    }

    /**
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    public void updateChargeToIncomeAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType, final boolean isPenalty) {
        // find all existing payment Channel to Fund source Mappings
        List<ProductToGLAccountMapping> existingChargeToIncomeAccountMappings;
        String arrayFragmentName;

        if (isPenalty) {
            existingChargeToIncomeAccountMappings = this.accountMappingRepository.findAllPenaltyToIncomeAccountMappings(productId,
                    portfolioProductType.getValue());
            arrayFragmentName = LoanProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            existingChargeToIncomeAccountMappings = this.accountMappingRepository.findAllFeeToIncomeAccountMappings(productId,
                    portfolioProductType.getValue());
            arrayFragmentName = LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(arrayFragmentName, element);
        /**
         * Variable stores a map representation of charges (key) and their associated income Id's (value) extracted from
         * the passed in Jsoncommand
         **/
        final Map<Long, Long> inputChargeToIncomeAccountMap = new HashMap<>();
        /***
         * Variable stores all charges which have already been mapped to Income Accounts in the system
         **/
        final Set<Long> existingCharges = new HashSet<>();
        if (chargeToIncomeAccountMappingArray != null) {
            if (changes != null) {
                changes.put(arrayFragmentName, command.jsonFragment(arrayFragmentName));
            }

            for (int i = 0; i < chargeToIncomeAccountMappingArray.size(); i++) {
                final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                final Long chargeId = jsonObject.get(LoanProductAccountingParams.CHARGE_ID.getValue()).getAsLong();
                final Long incomeAccountId = jsonObject.get(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue()).getAsLong();
                inputChargeToIncomeAccountMap.put(chargeId, incomeAccountId);
            }

            // If input map is empty, delete all existing mappings
            if (inputChargeToIncomeAccountMap.size() == 0) {
                this.accountMappingRepository.deleteAll(existingChargeToIncomeAccountMappings);
            } /**
               * Else, <br/>
               * update existing mappings OR <br/>
               * delete old mappings (which are already present, but not passed in as a part of Jsoncommand)<br/>
               * Create new mappings for charges that are passed in as a part of the Jsoncommand but not already present
               *
               **/
            else {
                for (final ProductToGLAccountMapping chargeToIncomeAccountMapping : existingChargeToIncomeAccountMappings) {
                    final Long currentCharge = chargeToIncomeAccountMapping.getCharge().getId();
                    existingCharges.add(currentCharge);
                    // update existing mappings (if required)
                    if (inputChargeToIncomeAccountMap.containsKey(currentCharge)) {
                        final Long newGLAccountId = inputChargeToIncomeAccountMap.get(currentCharge);
                        if (!newGLAccountId.equals(chargeToIncomeAccountMapping.getGlAccount().getId())) {
                            final GLAccount glAccount;
                            if (isPenalty) {
                                glAccount = getAccountByIdAndType(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue(),
                                        GLAccountType.INCOME, newGLAccountId);
                            } else {
                                List<GLAccountType> allowedAccountTypes = getAllowedAccountTypesForFeeMapping();
                                glAccount = getAccountByIdAndType(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue(),
                                        allowedAccountTypes, newGLAccountId);
                            }
                            chargeToIncomeAccountMapping.setGlAccount(glAccount);
                            this.accountMappingRepository.saveAndFlush(chargeToIncomeAccountMapping);
                        }
                    } // deleted payment type
                    else {
                        this.accountMappingRepository.delete(chargeToIncomeAccountMapping);
                    }
                }

                // only the newly added
                for (Map.Entry<Long, Long> entry : inputChargeToIncomeAccountMap.entrySet().stream()
                        .filter(e -> !existingCharges.contains(e.getKey())).toList()) {
                    saveChargeToFundSourceMapping(productId, entry.getKey(), entry.getValue(), portfolioProductType, isPenalty);
                }
            }
        }
    }

    /**
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    public void updatePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType) {
        // find all existing payment Channel to Fund source Mappings
        final List<ProductToGLAccountMapping> existingPaymentChannelToFundSourceMappings = this.accountMappingRepository
                .findAllPaymentTypeToFundSourceMappings(productId, portfolioProductType.getValue());
        final JsonArray paymentChannelMappingArray = this.fromApiJsonHelper
                .extractJsonArrayNamed(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
        /**
         * Variable stores a map representation of Payment channels (key) and their fund sources (value) extracted from
         * the passed in Jsoncommand
         **/
        final Map<Long, Long> inputPaymentChannelFundSourceMap = new HashMap<>();
        /***
         * Variable stores all payment types which have already been mapped to Fund Sources in the system
         **/
        final Set<Long> existingPaymentTypes = new HashSet<>();
        if (paymentChannelMappingArray != null) {
            if (changes != null) {
                changes.put(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
                        command.jsonFragment(LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue()));
            }

            for (int i = 0; i < paymentChannelMappingArray.size(); i++) {
                final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                final Long paymentTypeId = jsonObject.get(LoanProductAccountingParams.PAYMENT_TYPE.getValue()).getAsLong();
                final Long paymentSpecificFundAccountId = jsonObject.get(LoanProductAccountingParams.FUND_SOURCE.getValue()).getAsLong();
                inputPaymentChannelFundSourceMap.put(paymentTypeId, paymentSpecificFundAccountId);
            }

            // If input map is empty, delete all existing mappings
            if (inputPaymentChannelFundSourceMap.isEmpty()) {
                this.accountMappingRepository.deleteAll(existingPaymentChannelToFundSourceMappings);
            } /**
               * Else, <br/>
               * update existing mappings OR <br/>
               * delete old mappings (which re already present, but not passed in as a part of Jsoncommand)<br/>
               * Create new mappings for payment types that are passed in as a part of the Jsoncommand but not already
               * present
               *
               **/
            else {
                for (final ProductToGLAccountMapping existingPaymentChannelToFundSourceMapping : existingPaymentChannelToFundSourceMappings) {
                    final Long currentPaymentChannelId = existingPaymentChannelToFundSourceMapping.getPaymentType().getId();
                    existingPaymentTypes.add(currentPaymentChannelId);
                    // update existing mappings (if required)
                    if (inputPaymentChannelFundSourceMap.containsKey(currentPaymentChannelId)) {
                        final Long newGLAccountId = inputPaymentChannelFundSourceMap.get(currentPaymentChannelId);
                        if (!newGLAccountId.equals(existingPaymentChannelToFundSourceMapping.getGlAccount().getId())) {
                            final GLAccount glAccount = getAccountById(LoanProductAccountingParams.FUND_SOURCE.getValue(), newGLAccountId);
                            existingPaymentChannelToFundSourceMapping.setGlAccount(glAccount);
                            this.accountMappingRepository.saveAndFlush(existingPaymentChannelToFundSourceMapping);
                        }
                    } // deleted payment type
                    else {
                        this.accountMappingRepository.delete(existingPaymentChannelToFundSourceMapping);
                    }
                }

                // only the newly added
                for (Map.Entry<Long, Long> entry : inputPaymentChannelFundSourceMap.entrySet().stream()
                        .filter(e -> !existingPaymentTypes.contains(e.getKey())).toList()) {
                    savePaymentChannelToFundSourceMapping(productId, entry.getKey(), entry.getValue(), portfolioProductType);
                }
            }
        }
    }

    private Long getReasonIdByCashAccountForLoan(final ProductToGLAccountMapping productToGLAccountMapping,
            final CashAccountsForLoan cashAccountsForLoan) {
        return switch (cashAccountsForLoan) {
            case LOSSES_WRITTEN_OFF -> productToGLAccountMapping != null && productToGLAccountMapping.getWriteOffReason() != null
                    ? productToGLAccountMapping.getWriteOffReason().getId()
                    : null;
            case CHARGE_OFF_EXPENSE -> productToGLAccountMapping != null && productToGLAccountMapping.getChargeOffReason() != null
                    ? productToGLAccountMapping.getChargeOffReason().getId()
                    : null;
            default -> throw new IllegalStateException("Unexpected value: " + cashAccountsForLoan);
        };
    }

    public void updateReasonToGLAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType,
            final List<ProductToGLAccountMapping> existingReasonToGLAccountMappings,
            final LoanProductAccountingParams reasonToExpenseAccountMappingsParam, final LoanProductAccountingParams reasonCodeValueIdParam,
            final CashAccountsForLoan cashAccountsForLoan) {

        final JsonArray reasonToGLAccountMappingArray = this.fromApiJsonHelper
                .extractJsonArrayNamed(reasonToExpenseAccountMappingsParam.getValue(), element);

        final Map<Long, Long> inputReasonToGLAccountMap = new HashMap<>();

        final Set<Long> existingReasons = new HashSet<>();
        if (reasonToGLAccountMappingArray != null) {
            if (changes != null) {
                changes.put(reasonToExpenseAccountMappingsParam.getValue(),
                        command.jsonFragment(reasonToExpenseAccountMappingsParam.getValue()));
            }

            for (int i = 0; i < reasonToGLAccountMappingArray.size(); i++) {
                final JsonObject jsonObject = reasonToGLAccountMappingArray.get(i).getAsJsonObject();
                final Long expenseGlAccountId = jsonObject.get(LoanProductAccountingParams.EXPENSE_GL_ACCOUNT_ID.getValue()).getAsLong();
                final Long reasonCodeValueId = jsonObject.get(reasonCodeValueIdParam.getValue()).getAsLong();
                inputReasonToGLAccountMap.put(reasonCodeValueId, expenseGlAccountId);
            }

            // If input map is empty, delete all existing mappings
            if (inputReasonToGLAccountMap.isEmpty()) {
                this.accountMappingRepository.deleteAll(existingReasonToGLAccountMappings);

            } else {
                for (final ProductToGLAccountMapping existingReasonToGLAccountMapping : existingReasonToGLAccountMappings) {
                    final Long currentReasonId = getReasonIdByCashAccountForLoan(existingReasonToGLAccountMapping, cashAccountsForLoan);
                    if (currentReasonId != null) {
                        existingReasons.add(currentReasonId);
                        // update existing mappings (if required)
                        if (inputReasonToGLAccountMap.containsKey(currentReasonId)) {
                            final Long newGLAccountId = inputReasonToGLAccountMap.get(currentReasonId);
                            if (!newGLAccountId.equals(existingReasonToGLAccountMapping.getGlAccount().getId())) {
                                final Optional<GLAccount> glAccount = accountRepository.findById(newGLAccountId);
                                if (glAccount.isPresent()) {
                                    existingReasonToGLAccountMapping.setGlAccount(glAccount.get());
                                    this.accountMappingRepository.saveAndFlush(existingReasonToGLAccountMapping);
                                }
                            }
                        } // deleted payment type
                        else {
                            this.accountMappingRepository.delete(existingReasonToGLAccountMapping);
                        }
                    }
                }

                // only the newly added
                for (Map.Entry<Long, Long> entry : inputReasonToGLAccountMap.entrySet().stream()
                        .filter(e -> !existingReasons.contains(e.getKey())).toList()) {
                    saveReasonToExpenseMapping(productId, entry.getKey(), entry.getValue(), portfolioProductType, cashAccountsForLoan);
                }
            }
        }
    }

    public void updateClassificationToGLAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType,
            final LoanProductAccountingParams classificationParameter) {

        final List<ProductToGLAccountMapping> existingClassificationToGLAccountMappings = classificationParameter
                .equals(LoanProductAccountingParams.CAPITALIZED_INCOME_CLASSIFICATION_TO_INCOME_ACCOUNT_MAPPINGS)
                        ? this.accountMappingRepository.findAllCapitalizedIncomeClassificationsMappings(productId,
                                portfolioProductType.getValue())
                        : this.accountMappingRepository.findAllBuyDownFeeClassificationsMappings(productId,
                                portfolioProductType.getValue());

        final JsonArray classificationToGLAccountMappingArray = this.fromApiJsonHelper
                .extractJsonArrayNamed(classificationParameter.getValue(), element);

        final Map<Long, Long> inputClassificationToGLAccountMap = new HashMap<>();

        final Set<Long> existingClassifications = new HashSet<>();
        if (classificationToGLAccountMappingArray != null) {
            if (changes != null) {
                changes.put(classificationParameter.getValue(), command.jsonFragment(classificationParameter.getValue()));
            }

            for (int i = 0; i < classificationToGLAccountMappingArray.size(); i++) {
                final JsonObject jsonObject = classificationToGLAccountMappingArray.get(i).getAsJsonObject();
                final Long incomeGlAccountId = jsonObject.get(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue()).getAsLong();
                final Long classificationCodeValueId = jsonObject.get(LoanProductAccountingParams.CLASSIFICATION_CODE_VALUE_ID.getValue())
                        .getAsLong();
                inputClassificationToGLAccountMap.put(classificationCodeValueId, incomeGlAccountId);
            }

            // If input map is empty, delete all existing mappings
            if (inputClassificationToGLAccountMap.isEmpty()) {
                this.accountMappingRepository.deleteAll(existingClassificationToGLAccountMappings);
            } else {
                for (final ProductToGLAccountMapping existingClassificationToGLAccountMapping : existingClassificationToGLAccountMappings) {
                    final Long currentClassificationId = classificationParameter
                            .equals(LoanProductAccountingParams.CAPITALIZED_INCOME_CLASSIFICATION_TO_INCOME_ACCOUNT_MAPPINGS)
                                    ? existingClassificationToGLAccountMapping.getCapitalizedIncomeClassification().getId()
                                    : existingClassificationToGLAccountMapping.getBuydownFeeClassification().getId();

                    if (currentClassificationId != null) {
                        existingClassifications.add(currentClassificationId);
                        // update existing mappings (if required)
                        if (inputClassificationToGLAccountMap.containsKey(currentClassificationId)) {
                            final Long newGLAccountId = inputClassificationToGLAccountMap.get(currentClassificationId);
                            if (!newGLAccountId.equals(existingClassificationToGLAccountMapping.getGlAccount().getId())) {
                                final Optional<GLAccount> glAccount = accountRepository.findById(newGLAccountId);
                                if (glAccount.isPresent()) {
                                    existingClassificationToGLAccountMapping.setGlAccount(glAccount.get());
                                    this.accountMappingRepository.saveAndFlush(existingClassificationToGLAccountMapping);
                                }
                            }
                        } // deleted previous record
                        else {
                            this.accountMappingRepository.delete(existingClassificationToGLAccountMapping);
                        }
                    }
                }

                // only the newly added
                for (Map.Entry<Long, Long> entry : inputClassificationToGLAccountMap.entrySet().stream()
                        .filter(e -> !existingClassifications.contains(e.getKey())).toList()) {
                    saveClassificationToIncomeMapping(productId, entry.getKey(), entry.getValue(), portfolioProductType,
                            classificationParameter);
                }
            }
        }
    }

    /**
     * @param productId
     *
     */
    private void savePaymentChannelToFundSourceMapping(final Long productId, final Long paymentTypeId,
            final Long paymentTypeSpecificFundAccountId, final PortfolioProductType portfolioProductType) {
        final PaymentType paymentType = this.paymentTypeRepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        final GLAccount glAccount = getAccountById(LoanProductAccountingParams.FUND_SOURCE.getValue(), paymentTypeSpecificFundAccountId);
        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping().setGlAccount(glAccount).setProductId(productId)
                .setProductType(portfolioProductType.getValue()).setFinancialAccountType(CashAccountsForLoan.FUND_SOURCE.getValue())
                .setPaymentType(paymentType);
        this.accountMappingRepository.saveAndFlush(accountMapping);
    }

    /**
     * @param productId
     *
     */
    private void saveChargeToFundSourceMapping(final Long productId, final Long chargeId, final Long incomeAccountId,
            final PortfolioProductType portfolioProductType, final boolean isPenalty) {
        final Charge charge = this.chargeRepositoryWrapper.findOneWithNotFoundDetection(chargeId);

        // TODO Vishwas: Need to validate if given charge is fee or Penalty
        // based on input condition

        GLAccount glAccount;
        /**
         * Both CASH and Accrual placeholders have the same value for income from Interest and penalties
         **/
        CashAccountsForLoan placeHolderAccountType;
        if (isPenalty) {
            glAccount = getAccountByIdAndType(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue(), GLAccountType.INCOME,
                    incomeAccountId);
            placeHolderAccountType = CashAccountsForLoan.INCOME_FROM_PENALTIES;
        } else {
            List<GLAccountType> allowedAccountTypes = getAllowedAccountTypesForFeeMapping();
            glAccount = getAccountByIdAndType(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue(), allowedAccountTypes,
                    incomeAccountId);
            placeHolderAccountType = CashAccountsForLoan.INCOME_FROM_FEES;
        }
        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping().setGlAccount(glAccount).setProductId(productId)
                .setProductType(portfolioProductType.getValue()).setFinancialAccountType(placeHolderAccountType.getValue())
                .setCharge(charge);
        this.accountMappingRepository.saveAndFlush(accountMapping);
    }

    private Predicate<? super ProductToGLAccountMapping> matching(final CashAccountsForLoan typeDef, final Long reasonId) {
        return switch (typeDef) {
            case CHARGE_OFF_EXPENSE -> (mapping) -> (mapping.getChargeOffReason() != null && mapping.getChargeOffReason().getId() != null
                    && mapping.getChargeOffReason().getId().equals(reasonId));
            case LOSSES_WRITTEN_OFF -> (mapping) -> (mapping.getWriteOffReason() != null && mapping.getWriteOffReason().getId() != null
                    && mapping.getWriteOffReason().getId().equals(reasonId));
            default -> throw new IllegalStateException("Unexpected value: " + typeDef);
        };
    }

    private void saveReasonToExpenseMapping(final Long productId, final Long reasonId, final Long expenseAccountId,
            final PortfolioProductType portfolioProductType, final CashAccountsForLoan cashAccountsForLoan) {

        final Optional<GLAccount> glAccount = accountRepository.findById(expenseAccountId);
        final Optional<CodeValue> codeValueOptional = codeValueRepository.findById(reasonId);

        final boolean reasonMappingExists = this.accountMappingRepository
                .findAllProductToGLAccountMappingsByProductIdAndProductTypeAndFinancialAccountType(productId,
                        portfolioProductType.getValue(), cashAccountsForLoan.getValue())
                .stream().anyMatch(matching(cashAccountsForLoan, reasonId));

        if (!reasonMappingExists && glAccount.isPresent() && codeValueOptional.isPresent()) {
            final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping().setGlAccount(glAccount.get())
                    .setProductId(productId).setProductType(portfolioProductType.getValue())
                    .setFinancialAccountType(cashAccountsForLoan.getValue());

            switch (cashAccountsForLoan) {
                case CHARGE_OFF_EXPENSE -> accountMapping.setChargeOffReason(codeValueOptional.get());
                case LOSSES_WRITTEN_OFF -> accountMapping.setWriteOffReason(codeValueOptional.get());
                default -> throw new IllegalStateException("Unexpected value: " + cashAccountsForLoan);
            }

            this.accountMappingRepository.saveAndFlush(accountMapping);
        }
    }

    private void saveClassificationToIncomeMapping(final Long productId, final Long classificationId, final Long incomeAccountId,
            final PortfolioProductType portfolioProductType, final LoanProductAccountingParams classificationParameter) {

        final Optional<GLAccount> glAccount = accountRepository.findById(incomeAccountId);

        boolean classificationMappingExists = false;
        if (classificationParameter.equals(LoanProductAccountingParams.CAPITALIZED_INCOME_CLASSIFICATION_TO_INCOME_ACCOUNT_MAPPINGS)) {
            classificationMappingExists = this.accountMappingRepository
                    .findAllCapitalizedIncomeClassificationsMappings(productId, portfolioProductType.getValue()).stream()
                    .anyMatch(mapping -> mapping.getCapitalizedIncomeClassification().getId().equals(classificationId));
        } else {
            classificationMappingExists = this.accountMappingRepository
                    .findAllBuyDownFeeClassificationsMappings(productId, portfolioProductType.getValue()).stream()
                    .anyMatch(mapping -> mapping.getBuydownFeeClassification().getId().equals(classificationId));
        }

        final Optional<CodeValue> codeValueOptional = codeValueRepository.findById(classificationId);

        if (glAccount.isPresent() && !classificationMappingExists && codeValueOptional.isPresent()) {
            final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping().setGlAccount(glAccount.get())
                    .setProductId(productId).setProductType(portfolioProductType.getValue())
                    .setFinancialAccountType(CashAccountsForLoan.CLASSIFICATION_INCOME.getValue());

            if (classificationParameter.equals(LoanProductAccountingParams.CAPITALIZED_INCOME_CLASSIFICATION_TO_INCOME_ACCOUNT_MAPPINGS)) {
                accountMapping.setCapitalizedIncomeClassification(codeValueOptional.get());
            } else {
                accountMapping.setBuydownFeeClassification(codeValueOptional.get());
            }

            this.accountMappingRepository.saveAndFlush(accountMapping);
        }
    }

    private List<GLAccountType> getAllowedAccountTypesForFeeMapping() {
        List<GLAccountType> allowedAccountTypes = new ArrayList<>();
        allowedAccountTypes.add(GLAccountType.INCOME);
        allowedAccountTypes.add(GLAccountType.LIABILITY);
        return allowedAccountTypes;
    }

    /**
     * Fetches account with a particular Id and throws and Exception it is not of the expected Account Category
     * ('ASSET','liability' etc)
     *
     * @param paramName
     * @param expectedAccountType
     * @param accountId
     * @return
     */
    public GLAccount getAccountByIdAndType(final String paramName, final GLAccountType expectedAccountType, final Long accountId) {
        final GLAccount glAccount = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountId);

        // validate account is of the expected Type
        if (glAccount.getType().intValue() != expectedAccountType.getValue()) {
            throw new ProductToGLAccountMappingInvalidException(paramName, glAccount.getName(), accountId,
                    GLAccountType.fromInt(glAccount.getType()).toString(), expectedAccountType.toString());
        }
        return glAccount;
    }

    public GLAccount getAccountById(final String paramName, final Long accountId) {
        final GLAccount glAccount = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountId);
        return glAccount;
    }

    public GLAccount getAccountByIdAndType(final String paramName, final List<GLAccountType> expectedAccountTypes, final Long accountId) {
        final GLAccount glAccount = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountId);
        // validate account is of the expected Type
        List<Integer> glAccountTypeValues = new ArrayList<>();
        for (GLAccountType glAccountType : expectedAccountTypes) {
            glAccountTypeValues.add(glAccountType.getValue());
        }
        if (!glAccountTypeValues.contains(glAccount.getType())) {
            throw new ProductToGLAccountMappingInvalidException(paramName, glAccount.getName(), accountId,
                    GLAccountType.fromInt(glAccount.getType()).toString(), glAccountTypeValues.toString());
        }
        return glAccount;
    }

    public void deleteProductToGLAccountMapping(final Long loanProductId, final PortfolioProductType portfolioProductType,
            final int accountTypeId) {
        final ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(loanProductId,
                portfolioProductType.getValue(), accountTypeId);
        if (accountMapping != null && accountMapping.getGlAccount() != null) {
            this.accountMappingRepository.delete(accountMapping);
        }
    }

    public void deleteProductToGLAccountMapping(final Long loanProductId, final PortfolioProductType portfolioProductType) {
        final List<ProductToGLAccountMapping> productToGLAccountMappings = this.accountMappingRepository
                .findByProductIdAndProductType(loanProductId, portfolioProductType.getValue());
        if (productToGLAccountMappings != null && !productToGLAccountMappings.isEmpty()) {
            this.accountMappingRepository.deleteAll(productToGLAccountMappings);
        }
    }

    public void validateWriteOffMappingsInDatabase(final List<ApiParameterError> validationErrors, final List<JsonObject> mappings) {
        for (JsonObject jsonObject : mappings) {
            final Long writeOffReasonCodeValueId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.WRITE_OFF_REASON_CODE_VALUE_ID.getValue(), jsonObject);

            // Validation: writeOffReasonCodeValueId must exist in the database
            CodeValue codeValue = this.codeValueRepository.findByCodeNameAndId("WriteOffReasons", writeOffReasonCodeValueId);
            if (codeValue == null) {
                validationErrors.add(ApiParameterError.parameterError("validation.msg.writeoffreason.invalid",
                        "Write-off reason with ID " + writeOffReasonCodeValueId + " does not exist",
                        LoanProductAccountingParams.WRITE_OFF_REASON_TO_EXPENSE_ACCOUNT_MAPPINGS.getValue()));
            }
        }
    }

    public void validateGLAccountInDatabase(final List<ApiParameterError> validationErrors, final List<JsonObject> mappings) {
        for (JsonObject jsonObject : mappings) {
            final Long expenseGlAccountId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.EXPENSE_GL_ACCOUNT_ID.getValue(), jsonObject);

            // Validation: expenseGLAccountId must exist as a valid Expense GL account
            final Optional<GLAccount> glAccount = accountRepository.findById(expenseGlAccountId);

            if (glAccount.isEmpty() || !glAccount.get().getType().equals(GL_ACCOUNT_EXPENSE_TYPE)) {
                validationErrors.add(ApiParameterError.parameterError("validation.msg.glaccount.not.found",
                        "GL Account with ID " + expenseGlAccountId + " does not exist or is not an Expense GL account",
                        LoanProductAccountingParams.EXPENSE_GL_ACCOUNT_ID.getValue()));

            }
        }
    }

    public void validateChargeOffMappingsInDatabase(final List<ApiParameterError> validationErrors, final List<JsonObject> mappings) {

        for (JsonObject jsonObject : mappings) {
            final Long chargeOffReasonCodeValueId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.CHARGE_OFF_REASON_CODE_VALUE_ID.getValue(), jsonObject);

            // Validation: chargeOffReasonCodeValueId must exist in the database
            CodeValue codeValue = this.codeValueRepository.findByCodeNameAndId("ChargeOffReasons", chargeOffReasonCodeValueId);
            if (codeValue == null) {
                validationErrors.add(ApiParameterError.parameterError("validation.msg.chargeoffreason.invalid",
                        "Charge-off reason with ID " + chargeOffReasonCodeValueId + " does not exist",
                        LoanProductAccountingParams.CHARGE_OFF_REASON_TO_EXPENSE_ACCOUNT_MAPPINGS.getValue()));
            }
        }

        // Throw all collected validation errors, if any
        if (!validationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(validationErrors);
        }
    }

    public void validateClassificationMappingsInDatabase(final List<JsonObject> mappings, final String dataCodeName) {
        final List<ApiParameterError> validationErrors = new ArrayList<>();

        for (JsonObject jsonObject : mappings) {
            final Long incomeGlAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue(),
                    jsonObject);
            final Long classificationCodeValueId = this.fromApiJsonHelper
                    .extractLongNamed(LoanProductAccountingParams.CLASSIFICATION_CODE_VALUE_ID.getValue(), jsonObject);

            // Validation: classificationCodeValueId must exist in the database
            final CodeValue codeValue = this.codeValueRepository.findByCodeNameAndId(dataCodeName, classificationCodeValueId);
            if (codeValue == null) {
                validationErrors.add(ApiParameterError.parameterError("validation.msg.classification.invalid",
                        "Classification with ID " + classificationCodeValueId + " does not exist", dataCodeName));
            }

            // Validation: expenseGLAccountId must exist as a valid Expense GL account
            final Optional<GLAccount> glAccount = accountRepository.findById(incomeGlAccountId);

            if (glAccount.isEmpty() || !GLAccountType.fromInt(glAccount.get().getType()).isIncomeType()) {
                validationErrors.add(ApiParameterError.parameterError("validation.msg.glaccount.not.found",
                        "GL Account with ID " + incomeGlAccountId + " does not exist or is not an Income GL account",
                        LoanProductAccountingParams.INCOME_ACCOUNT_ID.getValue()));

            }
        }

        // Throw all collected validation errors, if any
        if (!validationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(validationErrors);
        }
    }

}
