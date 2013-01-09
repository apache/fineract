package org.mifosplatform.accounting.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.AccountingConstants;
import org.mifosplatform.accounting.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.AccountingConstants.PORTFOLIO_PRODUCT_TYPE;
import org.mifosplatform.accounting.domain.GLAccount;
import org.mifosplatform.accounting.domain.GLAccountRepository;
import org.mifosplatform.accounting.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.accounting.exceptions.GLAccountNotFoundException;
import org.mifosplatform.accounting.exceptions.ProductToGLAccountMappingNotFoundException;
import org.mifosplatform.accounting.service.ProductToGLAccountMappingWritePlatformService;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.mifosplatform.portfolio.loanproduct.domain.AccountingRuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductToGLAccountMappingWritePlatformServiceImpl implements ProductToGLAccountMappingWritePlatformService {

    private final GLAccountRepository accountRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;

    @Autowired
    public ProductToGLAccountMappingWritePlatformServiceImpl(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository) {
        this.accountRepository = glAccountRepository;
        this.accountMappingRepository = glAccountMappingRepository;
    }

    @Override
    @Transactional
    public void createLoanProductToGLAccountMapping(Long loanProductId, LoanProductCommand command) {
        AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.getAccountingType());

        if (accountingRuleType.equals(AccountingRuleType.NONE)) {
            return;
        } else if (accountingRuleType.equals(AccountingRuleType.CASH_BASED)) {
            // asset
            saveLoanToAccountMapping(command.getFundAccountId(), loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
            saveLoanToAccountMapping(command.getLoanPortfolioAccountId(), loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());
            saveLoanToAccountMapping(command.getInterestOnLoanId(), loanProductId, CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());

            // income
            saveLoanToAccountMapping(command.getIncomeFromFeeId(), loanProductId, CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
            saveLoanToAccountMapping(command.getIncomeFromPenaltyId(), loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());

            // expenses
            saveLoanToAccountMapping(command.getWriteOffAccountId(), loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());
        } else if (accountingRuleType.equals(AccountingRuleType.ACCRUAL_BASED)) {
            // assets (including receivables)
            saveLoanToAccountMapping(command.getFundAccountId(), loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
            saveLoanToAccountMapping(command.getLoanPortfolioAccountId(), loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());
            saveLoanToAccountMapping(command.getInterestOnLoanId(), loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());

            saveLoanToAccountMapping(command.getReceivableInterestAccountId(), loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.getValue());
            saveLoanToAccountMapping(command.getReceivableFeeAccountId(), loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.getValue());
            saveLoanToAccountMapping(command.getReceivablePenaltyAccountId(), loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.getValue());

            // income
            saveLoanToAccountMapping(command.getIncomeFromFeeId(), loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
            saveLoanToAccountMapping(command.getIncomeFromPenaltyId(), loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());

            // expenses
            saveLoanToAccountMapping(command.getWriteOffAccountId(), loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());
        }

    }

    @Override
    @Transactional
    public Map<String, Object> updateLoanProductToGLAccountMapping(Long loanProductId, LoanProductCommand command,
            boolean accountingRuleChanged) {
        Map<String, Object> changes = new HashMap<String, Object>();
        AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.getAccountingType());
        if (accountingRuleChanged) {
            deleteLoanProductToGLAccountMapping(loanProductId);
            createLoanProductToGLAccountMapping(loanProductId, command);
            if (accountingRuleType.equals(AccountingRuleType.CASH_BASED)) {
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), command.getFundAccountId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), command.getLoanPortfolioAccountId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), command.getInterestOnLoanId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), command.getIncomeFromFeeId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), command.getIncomeFromPenaltyId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), command.getWriteOffAccountId());
            } else if (accountingRuleType.equals(AccountingRuleType.ACCRUAL_BASED)) {
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), command.getFundAccountId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), command.getLoanPortfolioAccountId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), command.getInterestOnLoanId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), command.getIncomeFromFeeId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), command.getIncomeFromPenaltyId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), command.getWriteOffAccountId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), command.getReceivableInterestAccountId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), command.getReceivableFeeAccountId());
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), command.getReceivablePenaltyAccountId());
            }

        } else {

            if (accountingRuleType.equals(AccountingRuleType.NONE)) {
                // do nothing
            } else if (accountingRuleType.equals(AccountingRuleType.CASH_BASED)) {
                // asset
                updateLoanToAccountMapping(command.getFundAccountId(), loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(),
                        CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.toString(), LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), changes);
                updateLoanToAccountMapping(command.getLoanPortfolioAccountId(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), changes);
                updateLoanToAccountMapping(command.getInterestOnLoanId(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue(), CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), changes);

                // income
                updateLoanToAccountMapping(command.getIncomeFromFeeId(), loanProductId, CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue(),
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.toString(), LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                        changes);
                updateLoanToAccountMapping(command.getIncomeFromPenaltyId(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue(), CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), changes);

                // expenses
                updateLoanToAccountMapping(command.getWriteOffAccountId(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue(), CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), changes);
            } else if (accountingRuleType.equals(AccountingRuleType.ACCRUAL_BASED)) {
                // assets (including receivables)
                updateLoanToAccountMapping(command.getFundAccountId(), loanProductId, ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(),
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.toString(), LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), changes);
                updateLoanToAccountMapping(command.getLoanPortfolioAccountId(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), changes);
                updateLoanToAccountMapping(command.getInterestOnLoanId(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), changes);

                updateLoanToAccountMapping(command.getReceivableInterestAccountId(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), changes);
                updateLoanToAccountMapping(command.getReceivableFeeAccountId(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), changes);
                updateLoanToAccountMapping(command.getReceivablePenaltyAccountId(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.getValue(),
                        ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), changes);

                // income
                updateLoanToAccountMapping(command.getIncomeFromFeeId(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), changes);
                updateLoanToAccountMapping(command.getIncomeFromPenaltyId(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue(),
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), changes);

                // expenses
                updateLoanToAccountMapping(command.getWriteOffAccountId(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue(), ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.toString(),
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), changes);
            }
        }
        return changes;
    }

    private void saveLoanToAccountMapping(Long glAccountId, Long productId, int accountTypeId) {
        GLAccount glAccount = accountRepository.findOne(glAccountId);
        if (glAccount == null) { throw new GLAccountNotFoundException(glAccountId); }
        ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                AccountingConstants.PORTFOLIO_PRODUCT_TYPE.LOAN.getValue(), accountTypeId);
        accountMappingRepository.save(accountMapping);
    }

    private void updateLoanToAccountMapping(Long glAccountId, Long productId, int accountTypeId, String accountTypeName,
            String accountParamValue, Map<String, Object> changes) {
        // get the existing product
        ProductToGLAccountMapping accountMapping = accountMappingRepository.findByProductIdAndProductTypeAndFinancialAccountType(productId,
                PORTFOLIO_PRODUCT_TYPE.LOAN.getValue(), accountTypeId);
        if (accountMapping == null) { throw new ProductToGLAccountMappingNotFoundException(PORTFOLIO_PRODUCT_TYPE.LOAN, productId,
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
    public void deleteLoanProductToGLAccountMapping(Long loanProductId) {
        List<ProductToGLAccountMapping> productToGLAccountMappings = accountMappingRepository.findByProductIdAndProductType(loanProductId,
                PORTFOLIO_PRODUCT_TYPE.LOAN.getValue());
        if (productToGLAccountMappings != null && productToGLAccountMappings.size() > 0) {
            accountMappingRepository.deleteInBatch(productToGLAccountMappings);
        }
    }
}
