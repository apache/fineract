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
import org.mifosplatform.accounting.domain.PortfolioProductType;
import org.mifosplatform.accounting.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.accounting.exceptions.GLAccountNotFoundException;
import org.mifosplatform.accounting.exceptions.ProductToGLAccountMappingNotFoundException;
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

    @Autowired
    public ProductToGLAccountMappingWritePlatformServiceImpl(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper) {
        this.accountRepository = glAccountRepository;
        this.accountMappingRepository = glAccountMappingRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    @Transactional
    public void createLoanProductToGLAccountMapping(final Long loanProductId, final JsonCommand command) {
        final JsonElement element = fromApiJsonHelper.parse(command.json());
        final Integer accountingRuleTypeId = fromApiJsonHelper.extractIntegerNamed("accountingType", element, Locale.getDefault());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        final Long loanPortfolioAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(),
                element);
        final Long fundAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), element);
        final Long incomeFromInterestId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(),
                element);
        final Long incomeFromFeeId = fromApiJsonHelper
                .extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
        final Long incomeFromPenaltyId = fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
        final Long writeOffAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                element);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                saveLoanToAccountMapping(fundAccountId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
                saveLoanToAccountMapping(loanPortfolioAccountId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());
                saveLoanToAccountMapping(incomeFromInterestId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());

                // income
                saveLoanToAccountMapping(incomeFromFeeId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
                saveLoanToAccountMapping(incomeFromPenaltyId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());

                // expenses
                saveLoanToAccountMapping(writeOffAccountId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());
            break;
            case ACCRUAL_BASED:
                final Long receivableInterestAccountId = fromApiJsonHelper.extractLongNamed(
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
                final Long receivableFeeAccountId = fromApiJsonHelper.extractLongNamed(
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), element);
                final Long receivablePenaltyAccountId = fromApiJsonHelper.extractLongNamed(
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);

                // assets (including receivables)
                saveLoanToAccountMapping(fundAccountId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
                saveLoanToAccountMapping(loanPortfolioAccountId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());
                saveLoanToAccountMapping(incomeFromInterestId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());

                saveLoanToAccountMapping(receivableInterestAccountId, loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.getValue());
                saveLoanToAccountMapping(receivableFeeAccountId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.getValue());
                saveLoanToAccountMapping(receivablePenaltyAccountId, loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.getValue());

                // income
                saveLoanToAccountMapping(incomeFromFeeId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
                saveLoanToAccountMapping(incomeFromPenaltyId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());

                // expenses
                saveLoanToAccountMapping(writeOffAccountId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());
            break;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateLoanProductToGLAccountMapping(final Long loanProductId, final JsonCommand command,
            boolean accountingRuleChanged) {
        final Map<String, Object> changes = new HashMap<String, Object>();

        final JsonElement element = fromApiJsonHelper.parse(command.json());
        final Integer accountingRuleTypeId = fromApiJsonHelper.extractIntegerNamed("accountingType", element, Locale.getDefault());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        final Long fundAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), element);
        final Long loanPortfolioAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(),
                element);
        final Long incomeFromInterestId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(),
                element);
        final Long incomeFromFeeId = fromApiJsonHelper
                .extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
        final Long incomeFromPenaltyId = fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
        final Long writeOffAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                element);

        final Long receivableInterestAccountId = fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
        final Long receivableFeeAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(),
                element);
        final Long receivablePenaltyAccountId = fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);

        if (accountingRuleChanged) {
            deleteLoanProductToGLAccountMapping(loanProductId);
            createLoanProductToGLAccountMapping(loanProductId, command);

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
        } else {
            switch (accountingRuleType) {
                case NONE:
                break;
                case CASH_BASED:
                    // asset
                    updateLoanToAccountMapping(fundAccountId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(),
                            CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.toString(), LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), changes);
                    updateLoanToAccountMapping(loanPortfolioAccountId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(),
                            CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.toString(), LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(),
                            changes);
                    updateLoanToAccountMapping(incomeFromInterestId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue(),
                            CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), changes);

                    // income
                    updateLoanToAccountMapping(incomeFromFeeId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue(),
                            CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.toString(), LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                            changes);
                    updateLoanToAccountMapping(incomeFromPenaltyId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue(),
                            CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), changes);

                    // expenses
                    updateLoanToAccountMapping(writeOffAccountId, loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue(),
                            CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), changes);
                break;
                case ACCRUAL_BASED:
                    // assets (including receivables)
                    updateLoanToAccountMapping(fundAccountId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.toString(), LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(),
                            changes);
                    updateLoanToAccountMapping(loanPortfolioAccountId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.toString(), LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(),
                            changes);
                    updateLoanToAccountMapping(incomeFromInterestId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), changes);

                    updateLoanToAccountMapping(receivableInterestAccountId, loanProductId,
                            ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), changes);

                    updateLoanToAccountMapping(receivableFeeAccountId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), changes);

                    updateLoanToAccountMapping(receivablePenaltyAccountId, loanProductId,
                            ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), changes);

                    // income
                    updateLoanToAccountMapping(incomeFromFeeId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), changes);
                    updateLoanToAccountMapping(incomeFromPenaltyId, loanProductId,
                            ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), changes);

                    // expenses
                    updateLoanToAccountMapping(writeOffAccountId, loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue(),
                            ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.toString(),
                            LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), changes);
                break;
            }
        }
        return changes;
    }

    private void saveLoanToAccountMapping(final Long glAccountId, final Long productId, final int accountTypeId) {
        GLAccount glAccount = accountRepository.findOne(glAccountId);
        if (glAccount == null) { throw new GLAccountNotFoundException(glAccountId); }
        ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                PortfolioProductType.LOAN.getValue(), accountTypeId);
        accountMappingRepository.save(accountMapping);
    }

    private void updateLoanToAccountMapping(final Long glAccountId, final Long productId, final int accountTypeId,
            final String accountTypeName, final String accountParamValue, final Map<String, Object> changes) {
        // get the existing product
        ProductToGLAccountMapping accountMapping = accountMappingRepository.findByProductIdAndProductTypeAndFinancialAccountType(productId,
                PortfolioProductType.LOAN.getValue(), accountTypeId);
        if (accountMapping == null) { throw new ProductToGLAccountMappingNotFoundException(PortfolioProductType.LOAN, productId,
                accountTypeName); }
        if (accountMapping.getGlAccount().getId() != glAccountId) {
            GLAccount glAccount = accountRepository.findOne(glAccountId);
            if (glAccount == null) { throw new GLAccountNotFoundException(glAccountId); }
            changes.put(accountParamValue, glAccountId);
            accountMapping.setGlAccount(glAccount);
            accountMappingRepository.save(accountMapping);
        }
    }

    @Override
    public void deleteLoanProductToGLAccountMapping(final Long loanProductId) {
        List<ProductToGLAccountMapping> productToGLAccountMappings = accountMappingRepository.findByProductIdAndProductType(loanProductId,
                PortfolioProductType.LOAN.getValue());
        if (productToGLAccountMappings != null && productToGLAccountMappings.size() > 0) {
            accountMappingRepository.deleteInBatch(productToGLAccountMappings);
        }
    }
}
