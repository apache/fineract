package org.mifosplatform.accounting.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.mifosplatform.accounting.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.domain.GLAccount;
import org.mifosplatform.accounting.domain.GLAccountRepository;
import org.mifosplatform.accounting.domain.GLAccountType;
import org.mifosplatform.accounting.domain.PortfolioProductType;
import org.mifosplatform.accounting.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.accounting.exceptions.GLAccountNotFoundException;
import org.mifosplatform.accounting.exceptions.ProductToGLAccountMappingInvalidException;
import org.mifosplatform.accounting.exceptions.ProductToGLAccountMappingNotFoundException;
import org.mifosplatform.accounting.serialization.ProductToGLAccountMappingFromApiJsonDeserializer;
import org.mifosplatform.accounting.service.ProductToGLAccountMappingWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanproduct.domain.AccountingRuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class ProductToGLAccountMappingWritePlatformServiceImpl implements ProductToGLAccountMappingWritePlatformService {

    private final GLAccountRepository accountRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ProductToGLAccountMappingFromApiJsonDeserializer deserializer;

    @Autowired
    public ProductToGLAccountMappingWritePlatformServiceImpl(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ProductToGLAccountMappingFromApiJsonDeserializer deserializer) {
        this.accountRepository = glAccountRepository;
        this.accountMappingRepository = glAccountMappingRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.deserializer = deserializer;
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
            break;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateLoanProductToGLAccountMapping(final Long loanProductId, final JsonCommand command,
            final boolean accountingRuleChanged, int accountingRuleTypeId) {
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
            deserializer.validateForCreate(command.json());
            handleChangesToAccountingRuleType(loanProductId, command, changes, element, accountingRuleType);
        }/*** else examine and update individual changes ***/
        else {
            handleChangesToLoanProductToGLAccountMappings(loanProductId, changes, element, accountingRuleType);
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
            final ProductToGLAccountMapping accountMapping = this.accountMappingRepository
                    .findByProductIdAndProductTypeAndFinancialAccountType(productId, PortfolioProductType.LOAN.getValue(), accountTypeId);
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
     * of the expected Account Category ('DEBIT','CREDIT' etc)
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
        if (glAccount.getClassification().intValue() != expectedAccountType.getValue()) { throw new ProductToGLAccountMappingInvalidException(
                paramName, glAccount.getName(), accountId, GLAccountType.fromInt(glAccount.getClassification()).toString(),
                expectedAccountType.toString()); }
        return glAccount;
    }

}
