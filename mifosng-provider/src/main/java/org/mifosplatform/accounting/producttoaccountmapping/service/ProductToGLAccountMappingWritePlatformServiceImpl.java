/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepository;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.exception.GLAccountNotFoundException;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingInvalidException;
import org.mifosplatform.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingNotFoundException;
import org.mifosplatform.accounting.producttoaccountmapping.serialization.ProductToGLAccountMappingFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanproduct.domain.AccountingRuleType;
import org.mifosplatform.portfolio.paymentdetail.PaymentDetailConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class ProductToGLAccountMappingWritePlatformServiceImpl implements ProductToGLAccountMappingWritePlatformService {

    private final GLAccountRepository accountRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ProductToGLAccountMappingFromApiJsonDeserializer deserializer;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Autowired
    public ProductToGLAccountMappingWritePlatformServiceImpl(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ProductToGLAccountMappingFromApiJsonDeserializer deserializer, final CodeValueRepositoryWrapper codeValueRepositoryWrapper) {
        this.accountRepository = glAccountRepository;
        this.accountMappingRepository = glAccountMappingRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.deserializer = deserializer;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
    }

    @Override
    @Transactional
    public void createLoanProductToGLAccountMapping(final Long loanProductId, final JsonCommand command) {
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Integer accountingRuleTypeId = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
                saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());

                // income
                saveLoanToIncomeAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());
                saveLoanToIncomeAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
                saveLoanToIncomeAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());

                // expenses
                saveLoanToExpenseAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());

                // advanced accounting mappings
                savePaymentChannelToFundSourceMappings(command, element, loanProductId, null);
            break;
            case ACCRUAL_BASED:

                // assets (including receivables)
                saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
                saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());
                saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.getValue());
                saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.getValue());
                saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.getValue());

                // income
                saveLoanToIncomeAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());
                saveLoanToIncomeAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
                saveLoanToIncomeAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());

                // expenses
                saveLoanToExpenseAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());

                // advanced accounting mappings
                savePaymentChannelToFundSourceMappings(command, element, loanProductId, null);
            break;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateLoanProductToGLAccountMapping(final Long loanProductId, final JsonCommand command,
            final boolean accountingRuleChanged, final int accountingRuleTypeId) {
        /***
         * Variable tracks all accounting mapping properties that have been
         * updated
         ***/
        final Map<String, Object> changes = new HashMap<String, Object>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping
         * for the product and recreate a new set of mappings
         ***/
        if (accountingRuleChanged) {
            this.deserializer.validateForCreate(command.json());
            handleChangesToAccountingRuleType(loanProductId, command, changes, element, accountingRuleType);
            savePaymentChannelToFundSourceMappings(command, element, loanProductId, changes);
        }/*** else examine and update individual changes ***/
        else {
            handleChangesToLoanProductToGLAccountMappings(loanProductId, changes, element, accountingRuleType);
            updatePaymentChannelToFundSourceMappings(command, element, loanProductId, changes);
        }
        return changes;
    }

    @Override
    public void deleteLoanProductToGLAccountMapping(final Long loanProductId) {
        final List<ProductToGLAccountMapping> productToGLAccountMappings = this.accountMappingRepository.findByProductIdAndProductType(
                loanProductId, PortfolioProductType.LOAN.getValue());
        if (productToGLAccountMappings != null && productToGLAccountMappings.size() > 0) {
            this.accountMappingRepository.deleteInBatch(productToGLAccountMappings);
        }
    }

    /**
     * Saves payment type to Fund source mappings (also populates the changes
     * array if passed in)
     * 
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    private void savePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            Map<String, Object> changes) {
        JsonArray paymentChannelMappingArray = fromApiJsonHelper.extractJsonArrayNamed(
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
                savePaymentChannelToFundSourceMapping(productId, paymentTypeId, paymentSpecificFundAccountId);
            }
        }
    }

    /**
     * Finds
     * 
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    private void updatePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            Map<String, Object> changes) {
        // find all existing payment Channel to Fund source Mappings
        List<ProductToGLAccountMapping> existingPaymentChannelToFundSourceMappings = accountMappingRepository
                .findAllPaymentTypeToFundSourceMappings(productId, PortfolioProductType.LOAN.getValue());
        JsonArray paymentChannelMappingArray = fromApiJsonHelper.extractJsonArrayNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
        /**
         * Variable stores a map representation of Payment channels (key) and
         * their fund sources (value) extracted from the passed in Jsoncommand
         **/
        Map<Long, Long> inputPaymentChannelFundSourceMap = new HashMap<Long, Long>();
        /***
         * Variable stores all payment types which hae already been mapped to
         * Fund Sources in the system
         **/
        Set<Long> existingPaymentTypes = new HashSet<Long>();
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
                accountMappingRepository.deleteInBatch(existingPaymentChannelToFundSourceMappings);
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
                for (ProductToGLAccountMapping existingPaymentChannelToFundSourceMapping : existingPaymentChannelToFundSourceMappings) {
                    Long currentPaymentChannelId = existingPaymentChannelToFundSourceMapping.getId();
                    existingPaymentTypes.add(currentPaymentChannelId);
                    // update existing mappings (if required)
                    if (inputPaymentChannelFundSourceMap.containsKey(existingPaymentChannelToFundSourceMapping.getId())) {
                        Long newGLAccountId = inputPaymentChannelFundSourceMap.get(existingPaymentChannelToFundSourceMapping.getId());
                        if (newGLAccountId != existingPaymentChannelToFundSourceMapping.getGlAccount().getId()) {
                            final GLAccount glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(),
                                    GLAccountType.ASSET, newGLAccountId);
                            existingPaymentChannelToFundSourceMapping.setGlAccount(glAccount);
                            accountMappingRepository.save(existingPaymentChannelToFundSourceMapping);
                        }
                    }// deleted payment type
                    else {
                        accountMappingRepository.delete(existingPaymentChannelToFundSourceMapping);
                    }
                }
                // create new mappings
                Set<Long> incomingPaymentTypes = inputPaymentChannelFundSourceMap.keySet();
                boolean newPaymentTypesAdded = incomingPaymentTypes.removeAll(existingPaymentTypes);
                if (newPaymentTypesAdded) {
                    for (Long newPaymentType : incomingPaymentTypes) {
                        Long newGLAccountId = inputPaymentChannelFundSourceMap.get(newPaymentType);
                        savePaymentChannelToFundSourceMapping(productId, newPaymentType, newGLAccountId);
                    }
                }
            }
        }
    }

    /**
     * @param productId
     * @param jsonObject
     */
    private void savePaymentChannelToFundSourceMapping(final Long productId, final Long paymentTypeId,
            final Long paymentTypeSpecificFundAccountId) {
        CodeValue paymentType = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                PaymentDetailConstants.paymentTypeCodeName, paymentTypeId);
        final GLAccount glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), GLAccountType.ASSET,
                paymentTypeSpecificFundAccountId);
        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                PortfolioProductType.LOAN.getValue(), CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(), paymentType);
        this.accountMappingRepository.save(accountMapping);
    }

    /**
     * Examines and updates each account mapping for given loan product with
     * changes passed in from the Json element
     * 
     * @param loanProductId
     * @param changes
     * @param element
     * @param accountingRuleType
     */
    private void handleChangesToLoanProductToGLAccountMappings(final Long loanProductId, final Map<String, Object> changes,
            final JsonElement element, final AccountingRuleType accountingRuleType) {
        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                mergeLoanToAssetAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(), CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.toString(), changes);
                mergeLoanToAssetAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.toString(), changes);

                // income
                mergeLoanToIncomeAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue(), CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue(), CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(),
                        loanProductId, CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue(),
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.toString(), changes);

                // expenses
                mergeLoanToExpenseAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                        loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue(),
                        CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.toString(), changes);
            break;
            case ACCRUAL_BASED:
                // assets (including receivables)
                mergeLoanToAssetAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.toString(), changes);
                mergeLoanToAssetAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.toString(), changes);
                mergeLoanToAssetAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(),
                        loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.getValue(),
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.toString(), changes);
                mergeLoanToAssetAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.toString(), changes);
                mergeLoanToAssetAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(),
                        loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.getValue(),
                        ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.toString(), changes);

                // income
                mergeLoanToIncomeAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.toString(),
                        changes);
                mergeLoanToIncomeAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.toString(),
                        changes);
                mergeLoanToIncomeAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(),
                        loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue(),
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.toString(), changes);

                // expenses
                mergeLoanToExpenseAccountMappingChanges(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                        loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue(),
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.toString(), changes);
            break;
        }
    }

    /**
     * delete all existing mapping for a product and recreates a new set of
     * mappings (to be used exclusively when the selected accounting rule has
     * changed)
     * 
     * @param loanProductId
     * @param command
     * @param changes
     * @param element
     * @param accountingRuleType
     */
    private void handleChangesToAccountingRuleType(final Long loanProductId, final JsonCommand command, final Map<String, Object> changes,
            final JsonElement element, final AccountingRuleType accountingRuleType) {
        deleteLoanProductToGLAccountMapping(loanProductId);
        createLoanProductToGLAccountMapping(loanProductId, command);
        final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), element);
        final Long loanPortfolioAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), element);
        final Long incomeFromInterestId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), element);
        final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                element);
        final Long incomeFromPenaltyId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
        final Long writeOffAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), element);

        final Long receivableInterestAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
        final Long receivableFeeAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), element);
        final Long receivablePenaltyAccountId = this.fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), fundAccountId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanPortfolioAccountId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), incomeFromInterestId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), incomeFromFeeId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), incomeFromPenaltyId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), writeOffAccountId);
            break;
            case ACCRUAL_BASED:
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), fundAccountId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanPortfolioAccountId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), incomeFromInterestId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), incomeFromFeeId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), incomeFromPenaltyId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), writeOffAccountId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), receivableInterestAccountId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), receivableFeeAccountId);
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), receivablePenaltyAccountId);
            break;
        }
    }

    /**
     * Utility wrapper for
     * {@link #saveLoanToAccountMapping(JsonElement, String, Long, int, GLAccountType)}
     * 
     * @param element
     * @param paramName
     * @param productId
     * @param placeHolderTypeId
     */
    private void saveLoanToAssetAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveLoanToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.ASSET);
    }

    /**
     * Utility wrapper for
     * {@link #saveLoanToAccountMapping(JsonElement, String, Long, int, GLAccountType)}
     * 
     * @param element
     * @param paramName
     * @param productId
     * @param placeHolderTypeId
     */
    private void saveLoanToIncomeAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveLoanToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.INCOME);
    }

    /**
     * Utility wrapper for
     * {@link #saveLoanToAccountMapping(JsonElement, String, Long, int, GLAccountType)}
     * 
     * @param element
     * @param paramName
     * @param productId
     * @param placeHolderTypeId
     */
    private void saveLoanToExpenseAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveLoanToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.EXPENSE);
    }

    /**
     * @param element
     * @param paramName
     * @param productId
     * @param placeHolderTypeId
     * @param expectedAccountType
     */
    private void saveLoanToAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId, final GLAccountType expectedAccountType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);

        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                PortfolioProductType.LOAN.getValue(), placeHolderTypeId);
        this.accountMappingRepository.save(accountMapping);
    }

    /**
     * Utility wrapper for
     * {@link #mergeLoanToAccountMappingChanges(JsonElement, String, Long, int, String, Map, GLAccountType)}
     * 
     * @param element
     * @param paramName
     * @param productId
     * @param accountTypeId
     * @param accountTypeName
     * @param changes
     */
    private void mergeLoanToAssetAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeLoanToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.ASSET);
    }

    /**
     * Utility wrapper for
     * {@link #mergeLoanToAccountMappingChanges(JsonElement, String, Long, int, String, Map, GLAccountType)}
     * 
     * @param element
     * @param paramName
     * @param productId
     * @param accountTypeId
     * @param accountTypeName
     * @param changes
     */
    private void mergeLoanToIncomeAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeLoanToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.INCOME);
    }

    /**
     * Utility wrapper for
     * {@link #mergeLoanToAccountMappingChanges(JsonElement, String, Long, int, String, Map, GLAccountType)}
     * 
     * @param element
     * @param paramName
     * @param productId
     * @param accountTypeId
     * @param accountTypeName
     * @param changes
     */
    private void mergeLoanToExpenseAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeLoanToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.EXPENSE);
    }

    /**
     * Method merges changes passed in through JSON element
     * 
     * @param element
     * @param paramName
     * @param productId
     * @param accountTypeId
     * @param accountTypeName
     * @param changes
     * @param expectedAccountType
     */
    private void mergeLoanToAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes,
            final GLAccountType expectedAccountType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);

        // get the existing product
        if (accountId != null) {
            final ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(productId,
                    PortfolioProductType.LOAN.getValue(), accountTypeId);
            if (accountMapping == null) { throw new ProductToGLAccountMappingNotFoundException(PortfolioProductType.LOAN, productId,
                    accountTypeName); }
            if (accountMapping.getGlAccount().getId() != accountId) {
                final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
                changes.put(paramName, accountId);
                accountMapping.setGlAccount(glAccount);
                this.accountMappingRepository.save(accountMapping);
            }
        }
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
    private GLAccount getAccountByIdAndType(final String paramName, final GLAccountType expectedAccountType, final Long accountId) {
        final GLAccount glAccount = this.accountRepository.findOne(accountId);
        if (glAccount == null) { throw new GLAccountNotFoundException(accountId); }

        // validate account is of the expected Type
        if (glAccount.getType().intValue() != expectedAccountType.getValue()) { throw new ProductToGLAccountMappingInvalidException(
                paramName, glAccount.getName(), accountId, GLAccountType.fromInt(glAccount.getType()).toString(),
                expectedAccountType.toString()); }
        return glAccount;
    }

}
