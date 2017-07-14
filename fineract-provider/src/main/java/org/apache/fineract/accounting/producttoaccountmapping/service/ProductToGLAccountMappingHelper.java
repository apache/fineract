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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.apache.fineract.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingInvalidException;
import org.apache.fineract.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingNotFoundException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class ProductToGLAccountMappingHelper {

    protected final GLAccountRepository accountRepository;
    protected final GLAccountRepositoryWrapper accountRepositoryWrapper;
    protected final ProductToGLAccountMappingRepository accountMappingRepository;
    protected final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;
    private final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper;

    @Autowired
    public ProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final GLAccountRepositoryWrapper accountRepositoryWrapper,
            PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper) {
        this.accountRepository = glAccountRepository;
        this.accountMappingRepository = glAccountMappingRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepositoryWrapper = chargeRepositoryWrapper;
        this.accountRepositoryWrapper = accountRepositoryWrapper;
        this.paymentTypeRepositoryWrapper = paymentTypeRepositoryWrapper;

    }

    public void saveProductToAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId, final GLAccountType expectedAccountType, final PortfolioProductType portfolioProductType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);

        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                portfolioProductType.getValue(), placeHolderTypeId);
        this.accountMappingRepository.save(accountMapping);
    }

    public void mergeProductToAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes,
            final GLAccountType expectedAccountType, final PortfolioProductType portfolioProductType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);

        // get the existing product
        if (accountId != null) {
            final ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(productId,
                    portfolioProductType.getValue(), accountTypeId);
            if (accountMapping == null) { throw new ProductToGLAccountMappingNotFoundException(portfolioProductType, productId,
                    accountTypeName); }
            if (accountMapping.getGlAccount().getId() != accountId) {
                final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
                changes.put(paramName, accountId);
                accountMapping.setGlAccount(glAccount);
                this.accountMappingRepository.save(accountMapping);
            }
        }
    }

    public void createOrmergeProductToAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final Map<String, Object> changes,
            final GLAccountType expectedAccountType, final PortfolioProductType portfolioProductType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);

        // get the existing product
        if (accountId != null) {
            final ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(productId,
                    portfolioProductType.getValue(), accountTypeId);
            if(accountMapping == null) {
                final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
                changes.put(paramName, accountId);
                ProductToGLAccountMapping newAccountMapping  = new ProductToGLAccountMapping(glAccount, productId,
                portfolioProductType.getValue(), accountTypeId) ;
                this.accountMappingRepository.save(newAccountMapping);
            }else if (accountMapping.getGlAccount().getId() != accountId) {
                final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
                changes.put(paramName, accountId);
                accountMapping.setGlAccount(glAccount);
                this.accountMappingRepository.save(accountMapping);
            }
        }
    }
    /**
     * Saves the payment type to Fund source mappings for a particular
     * product/product type (also populates the changes array if passed in)
     * 
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    public void savePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType) {
        final JsonArray paymentChannelMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
        if (paymentChannelMappingArray != null) {
            if (changes != null) {
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
                        command.jsonFragment(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue()));
            }
            for (int i = 0; i < paymentChannelMappingArray.size(); i++) {
                final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                final Long paymentTypeId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_TYPE.getValue()).getAsLong();
                final Long paymentSpecificFundAccountId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).getAsLong();
                savePaymentChannelToFundSourceMapping(productId, paymentTypeId, paymentSpecificFundAccountId, portfolioProductType);
            }
        }
    }

    /**
     * Saves the Charge to Income / Liability account mappings for a particular
     * product/product type (also populates the changes array if passed in)
     * 
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    public void saveChargesToIncomeOrLiabilityAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes, final PortfolioProductType portfolioProductType, final boolean isPenalty) {
        String arrayName;
        if (isPenalty) {
            arrayName = LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            arrayName = LOAN_PRODUCT_ACCOUNTING_PARAMS.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(arrayName, element);
        if (chargeToIncomeAccountMappingArray != null) {
            if (changes != null) {
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEE_INCOME_ACCOUNT_MAPPING.getValue(),
                        command.jsonFragment(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEE_INCOME_ACCOUNT_MAPPING.getValue()));
            }
            for (int i = 0; i < chargeToIncomeAccountMappingArray.size(); i++) {
                final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                final Long chargeId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.CHARGE_ID.getValue()).getAsLong();
                final Long incomeAccountId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue()).getAsLong();
                saveChargeToFundSourceMapping(productId, chargeId, incomeAccountId, portfolioProductType, isPenalty);
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
            arrayFragmentName = LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            existingChargeToIncomeAccountMappings = this.accountMappingRepository.findAllFeeToIncomeAccountMappings(productId,
                    portfolioProductType.getValue());
            arrayFragmentName = LOAN_PRODUCT_ACCOUNTING_PARAMS.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(arrayFragmentName, element);
        /**
         * Variable stores a map representation of charges (key) and their
         * associated income Id's (value) extracted from the passed in
         * Jsoncommand
         **/
        final Map<Long, Long> inputChargeToIncomeAccountMap = new HashMap<>();
        /***
         * Variable stores all charges which have already been mapped to Income
         * Accounts in the system
         **/
        final Set<Long> existingCharges = new HashSet<>();
        if (chargeToIncomeAccountMappingArray != null) {
            if (changes != null) {
                changes.put(arrayFragmentName, command.jsonFragment(arrayFragmentName));
            }

            for (int i = 0; i < chargeToIncomeAccountMappingArray.size(); i++) {
                final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                final Long chargeId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.CHARGE_ID.getValue()).getAsLong();
                final Long incomeAccountId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue()).getAsLong();
                inputChargeToIncomeAccountMap.put(chargeId, incomeAccountId);
            }

            // If input map is empty, delete all existing mappings
            if (inputChargeToIncomeAccountMap.size() == 0) {
                this.accountMappingRepository.deleteInBatch(existingChargeToIncomeAccountMappings);
            }/**
             * Else, <br/>
             * update existing mappings OR <br/>
             * delete old mappings (which are already present, but not passed in
             * as a part of Jsoncommand)<br/>
             * Create new mappings for charges that are passed in as a part of
             * the Jsoncommand but not already present
             * 
             **/
            else {
                for (final ProductToGLAccountMapping chargeToIncomeAccountMapping : existingChargeToIncomeAccountMappings) {
                    final Long currentCharge = chargeToIncomeAccountMapping.getCharge().getId();
                    existingCharges.add(currentCharge);
                    // update existing mappings (if required)
                    if (inputChargeToIncomeAccountMap.containsKey(currentCharge)) {
                        final Long newGLAccountId = inputChargeToIncomeAccountMap.get(currentCharge);
                        if (newGLAccountId != chargeToIncomeAccountMapping.getGlAccount().getId()) {
                            final GLAccount glAccount;
                            if (isPenalty) {
                                glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue(),
                                        GLAccountType.INCOME, newGLAccountId);
                            } else {
                                List<GLAccountType> allowedAccountTypes = getAllowedAccountTypesForFeeMapping();
                                glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue(),
                                        allowedAccountTypes, newGLAccountId);
                            }
                            chargeToIncomeAccountMapping.setGlAccount(glAccount);
                            this.accountMappingRepository.save(chargeToIncomeAccountMapping);
                        }
                    }// deleted payment type
                    else {
                        this.accountMappingRepository.delete(chargeToIncomeAccountMapping);
                    }
                }
                // create new mappings
                final Set<Long> incomingCharges = inputChargeToIncomeAccountMap.keySet();
                incomingCharges.removeAll(existingCharges);
                // incomingPaymentTypes now only contains the newly added
                // payment Type mappings
                for (final Long newCharge : incomingCharges) {
                    final Long newGLAccountId = inputChargeToIncomeAccountMap.get(newCharge);
                    saveChargeToFundSourceMapping(productId, newCharge, newGLAccountId, portfolioProductType, isPenalty);
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
        final JsonArray paymentChannelMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
        /**
         * Variable stores a map representation of Payment channels (key) and
         * their fund sources (value) extracted from the passed in Jsoncommand
         **/
        final Map<Long, Long> inputPaymentChannelFundSourceMap = new HashMap<>();
        /***
         * Variable stores all payment types which have already been mapped to
         * Fund Sources in the system
         **/
        final Set<Long> existingPaymentTypes = new HashSet<>();
        if (paymentChannelMappingArray != null) {
            if (changes != null) {
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
                        command.jsonFragment(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue()));
            }

            for (int i = 0; i < paymentChannelMappingArray.size(); i++) {
                final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                final Long paymentTypeId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_TYPE.getValue()).getAsLong();
                final Long paymentSpecificFundAccountId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).getAsLong();
                inputPaymentChannelFundSourceMap.put(paymentTypeId, paymentSpecificFundAccountId);
            }

            // If input map is empty, delete all existing mappings
            if (inputPaymentChannelFundSourceMap.size() == 0) {
                this.accountMappingRepository.deleteInBatch(existingPaymentChannelToFundSourceMappings);
            }/**
             * Else, <br/>
             * update existing mappings OR <br/>
             * delete old mappings (which re already present, but not passed in
             * as a part of Jsoncommand)<br/>
             * Create new mappings for payment types that are passed in as a
             * part of the Jsoncommand but not already present
             * 
             **/
            else {
                for (final ProductToGLAccountMapping existingPaymentChannelToFundSourceMapping : existingPaymentChannelToFundSourceMappings) {
                    final Long currentPaymentChannelId = existingPaymentChannelToFundSourceMapping.getPaymentType().getId();
                    existingPaymentTypes.add(currentPaymentChannelId);
                    // update existing mappings (if required)
                    if (inputPaymentChannelFundSourceMap.containsKey(currentPaymentChannelId)) {
                        final Long newGLAccountId = inputPaymentChannelFundSourceMap.get(currentPaymentChannelId);
                        if (newGLAccountId != existingPaymentChannelToFundSourceMapping.getGlAccount().getId()) {
                            final GLAccount glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(),
                                    GLAccountType.ASSET, newGLAccountId);
                            existingPaymentChannelToFundSourceMapping.setGlAccount(glAccount);
                            this.accountMappingRepository.save(existingPaymentChannelToFundSourceMapping);
                        }
                    }// deleted payment type
                    else {
                        this.accountMappingRepository.delete(existingPaymentChannelToFundSourceMapping);
                    }
                }
                // create new mappings
                final Set<Long> incomingPaymentTypes = inputPaymentChannelFundSourceMap.keySet();
                incomingPaymentTypes.removeAll(existingPaymentTypes);
                // incomingPaymentTypes now only contains the newly added
                // payment Type mappings
                for (final Long newPaymentType : incomingPaymentTypes) {
                    final Long newGLAccountId = inputPaymentChannelFundSourceMap.get(newPaymentType);
                    savePaymentChannelToFundSourceMapping(productId, newPaymentType, newGLAccountId, portfolioProductType);
                }
            }
        }
    }

    /**
     * @param productId
     * @param jsonObject
     */
    private void savePaymentChannelToFundSourceMapping(final Long productId, final Long paymentTypeId,
            final Long paymentTypeSpecificFundAccountId, final PortfolioProductType portfolioProductType) {
        final PaymentType paymentType = this.paymentTypeRepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        final GLAccount glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), GLAccountType.ASSET,
                paymentTypeSpecificFundAccountId);
        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                portfolioProductType.getValue(), CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(), paymentType);
        this.accountMappingRepository.save(accountMapping);
    }

    /**
     * @param productId
     * @param jsonObject
     */
    private void saveChargeToFundSourceMapping(final Long productId, final Long chargeId, final Long incomeAccountId,
            final PortfolioProductType portfolioProductType, final boolean isPenalty) {
        final Charge charge = this.chargeRepositoryWrapper.findOneWithNotFoundDetection(chargeId);

        // TODO Vishwas: Need to validate if given charge is fee or Penalty
        // based on input condition

        GLAccount glAccount;
        /**
         * Both CASH and Accrual placeholders have the same value for income
         * from Interest and penalties
         **/
        CASH_ACCOUNTS_FOR_LOAN placeHolderAccountType;
        if (isPenalty) {
            glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue(), GLAccountType.INCOME,
                    incomeAccountId);
            placeHolderAccountType = CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES;
        } else {
            List<GLAccountType> allowedAccountTypes = getAllowedAccountTypesForFeeMapping();
            glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_ACCOUNT_ID.getValue(), allowedAccountTypes,
                    incomeAccountId);
            placeHolderAccountType = CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES;
        }
        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                portfolioProductType.getValue(), placeHolderAccountType.getValue(), charge);
        this.accountMappingRepository.save(accountMapping);
    }

    private List<GLAccountType> getAllowedAccountTypesForFeeMapping() {
        List<GLAccountType> allowedAccountTypes = new ArrayList<>();
        allowedAccountTypes.add(GLAccountType.INCOME);
        allowedAccountTypes.add(GLAccountType.LIABILITY);
        return allowedAccountTypes;
    }

    /**
     * Fetches account with a particular Id and throws and Exception it is not
     * of the expected Account Category ('ASSET','liability' etc)
     * 
     * @param paramName
     * @param expectedAccountType
     * @param accountId
     * @return
     */
    public GLAccount getAccountByIdAndType(final String paramName, final GLAccountType expectedAccountType, final Long accountId) {
        final GLAccount glAccount = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountId);

        // validate account is of the expected Type
        if (glAccount.getType().intValue() != expectedAccountType.getValue()) { throw new ProductToGLAccountMappingInvalidException(
                paramName, glAccount.getName(), accountId, GLAccountType.fromInt(glAccount.getType()).toString(),
                expectedAccountType.toString()); }
        return glAccount;
    }

    public GLAccount getAccountByIdAndType(final String paramName, final List<GLAccountType> expectedAccountTypes, final Long accountId) {
        final GLAccount glAccount = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountId);
        // validate account is of the expected Type
        List<Integer> glAccountTypeValues = new ArrayList<>();
        for (GLAccountType glAccountType : expectedAccountTypes) {
            glAccountTypeValues.add(glAccountType.getValue());
        }
        if (!glAccountTypeValues.contains(glAccount.getType())) { throw new ProductToGLAccountMappingInvalidException(paramName,
                glAccount.getName(), accountId, GLAccountType.fromInt(glAccount.getType()).toString(), glAccountTypeValues.toString()); }
        return glAccount;
    }

    public void deleteProductToGLAccountMapping(final Long loanProductId, final PortfolioProductType portfolioProductType) {
        final List<ProductToGLAccountMapping> productToGLAccountMappings = this.accountMappingRepository.findByProductIdAndProductType(
                loanProductId, portfolioProductType.getValue());
        if (productToGLAccountMappings != null && productToGLAccountMappings.size() > 0) {
            this.accountMappingRepository.deleteInBatch(productToGLAccountMappings);
        }
    }
}
