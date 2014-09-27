/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.closure.domain.GLClosureRepository;
import org.mifosplatform.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.mifosplatform.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.mifosplatform.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.mifosplatform.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.journalentry.data.ChargePaymentDTO;
import org.mifosplatform.accounting.journalentry.data.LoanDTO;
import org.mifosplatform.accounting.journalentry.data.LoanTransactionDTO;
import org.mifosplatform.accounting.journalentry.data.SavingsDTO;
import org.mifosplatform.accounting.journalentry.data.SavingsTransactionDTO;
import org.mifosplatform.accounting.journalentry.domain.JournalEntry;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryRepository;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.accounting.journalentry.exception.JournalEntryInvalidException;
import org.mifosplatform.accounting.journalentry.exception.JournalEntryInvalidException.GL_JOURNAL_ENTRY_INVALID_REASON;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingNotFoundException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.service.AccountTransfersReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountingProcessorHelper {

    public static final String LOAN_TRANSACTION_IDENTIFIER = "L";
    public static final String SAVINGS_TRANSACTION_IDENTIFIER = "S";
    private final JournalEntryRepository glJournalEntryRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository;
    private final GLClosureRepository closureRepository;
    private final OfficeRepository officeRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;

    @Autowired
    public AccountingProcessorHelper(final JournalEntryRepository glJournalEntryRepository,
            final ProductToGLAccountMappingRepository accountMappingRepository, final GLClosureRepository closureRepository,
            final OfficeRepository officeRepository, final LoanTransactionRepository loanTransactionRepository,
            final SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository,
            final AccountTransfersReadPlatformService accountTransfersReadPlatformService) {
        this.glJournalEntryRepository = glJournalEntryRepository;
        this.accountMappingRepository = accountMappingRepository;
        this.closureRepository = closureRepository;
        this.officeRepository = officeRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.savingsAccountTransactionRepository = savingsAccountTransactionRepository;
        this.financialActivityAccountRepository = financialActivityAccountRepository;
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
    }

    public LoanDTO populateLoanDtoFromMap(final Map<String, Object> accountingBridgeData, final boolean cashBasedAccountingEnabled,
            final boolean upfrontAccrualBasedAccountingEnabled, final boolean periodicAccrualBasedAccountingEnabled) {
        final Long loanId = (Long) accountingBridgeData.get("loanId");
        final Long loanProductId = (Long) accountingBridgeData.get("loanProductId");
        final Long officeId = (Long) accountingBridgeData.get("officeId");
        final CurrencyData currencyData = (CurrencyData) accountingBridgeData.get("currency");
        final List<LoanTransactionDTO> newLoanTransactions = new ArrayList<>();
        boolean isAccountTransfer = (Boolean) accountingBridgeData.get("isAccountTransfer");

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> newTransactionsMap = (List<Map<String, Object>>) accountingBridgeData.get("newLoanTransactions");

        for (final Map<String, Object> map : newTransactionsMap) {
            final Long transactionOfficeId = (Long) map.get("officeId");
            final String transactionId = ((Long) map.get("id")).toString();
            final Date transactionDate = ((LocalDate) map.get("date")).toDate();
            final LoanTransactionEnumData transactionType = (LoanTransactionEnumData) map.get("type");
            final BigDecimal amount = (BigDecimal) map.get("amount");
            final BigDecimal principal = (BigDecimal) map.get("principalPortion");
            final BigDecimal interest = (BigDecimal) map.get("interestPortion");
            final BigDecimal fees = (BigDecimal) map.get("feeChargesPortion");
            final BigDecimal penalties = (BigDecimal) map.get("penaltyChargesPortion");
            final BigDecimal overPayments = (BigDecimal) map.get("overPaymentPortion");
            final boolean reversed = (Boolean) map.get("reversed");
            final Long paymentTypeId = (Long) map.get("paymentTypeId");

            final List<ChargePaymentDTO> feePaymentDetails = new ArrayList<>();
            final List<ChargePaymentDTO> penaltyPaymentDetails = new ArrayList<>();
            // extract charge payment details (if exists)
            if (map.containsKey("loanChargesPaid")) {
                @SuppressWarnings("unchecked")
                final List<Map<String, Object>> loanChargesPaidData = (List<Map<String, Object>>) map.get("loanChargesPaid");
                for (final Map<String, Object> loanChargePaid : loanChargesPaidData) {
                    final Long chargeId = (Long) loanChargePaid.get("chargeId");
                    final Long loanChargeId = (Long) loanChargePaid.get("loanChargeId");
                    final boolean isPenalty = (Boolean) loanChargePaid.get("isPenalty");
                    final BigDecimal chargeAmountPaid = (BigDecimal) loanChargePaid.get("amount");
                    final ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargeId, loanChargeId, chargeAmountPaid);
                    if (isPenalty) {
                        penaltyPaymentDetails.add(chargePaymentDTO);
                    } else {
                        feePaymentDetails.add(chargePaymentDTO);
                    }
                }
            }

            if (!isAccountTransfer) {
                isAccountTransfer = this.accountTransfersReadPlatformService.isAccountTransfer(Long.parseLong(transactionId),
                        PortfolioAccountType.LOAN);
            }
            final LoanTransactionDTO transaction = new LoanTransactionDTO(transactionOfficeId, paymentTypeId, transactionId,
                    transactionDate, transactionType, amount, principal, interest, fees, penalties, overPayments, reversed,
                    feePaymentDetails, penaltyPaymentDetails, isAccountTransfer);

            newLoanTransactions.add(transaction);

        }

        return new LoanDTO(loanId, loanProductId, officeId, currencyData.code(), cashBasedAccountingEnabled,
                upfrontAccrualBasedAccountingEnabled, periodicAccrualBasedAccountingEnabled, newLoanTransactions);
    }

    public SavingsDTO populateSavingsDtoFromMap(final Map<String, Object> accountingBridgeData, final boolean cashBasedAccountingEnabled,
            final boolean accrualBasedAccountingEnabled) {
        final Long loanId = (Long) accountingBridgeData.get("savingsId");
        final Long loanProductId = (Long) accountingBridgeData.get("savingsProductId");
        final Long officeId = (Long) accountingBridgeData.get("officeId");
        final CurrencyData currencyData = (CurrencyData) accountingBridgeData.get("currency");
        final List<SavingsTransactionDTO> newSavingsTransactions = new ArrayList<>();
        boolean isAccountTransfer = (Boolean) accountingBridgeData.get("isAccountTransfer");

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> newTransactionsMap = (List<Map<String, Object>>) accountingBridgeData.get("newSavingsTransactions");

        for (final Map<String, Object> map : newTransactionsMap) {
            final Long transactionOfficeId = (Long) map.get("officeId");
            final String transactionId = ((Long) map.get("id")).toString();
            final Date transactionDate = ((LocalDate) map.get("date")).toDate();
            final SavingsAccountTransactionEnumData transactionType = (SavingsAccountTransactionEnumData) map.get("type");
            final BigDecimal amount = (BigDecimal) map.get("amount");
            final boolean reversed = (Boolean) map.get("reversed");
            final Long paymentTypeId = (Long) map.get("paymentTypeId");
            final BigDecimal overdraftAmount = (BigDecimal) map.get("overdraftAmount");

            final List<ChargePaymentDTO> feePayments = new ArrayList<>();
            final List<ChargePaymentDTO> penaltyPayments = new ArrayList<>();
            // extract charge payment details (if exists)
            if (map.containsKey("savingsChargesPaid")) {
                @SuppressWarnings("unchecked")
                final List<Map<String, Object>> savingsChargesPaidData = (List<Map<String, Object>>) map.get("savingsChargesPaid");
                for (final Map<String, Object> loanChargePaid : savingsChargesPaidData) {
                    final Long chargeId = (Long) loanChargePaid.get("chargeId");
                    final Long loanChargeId = (Long) loanChargePaid.get("savingsChargeId");
                    final boolean isPenalty = (Boolean) loanChargePaid.get("isPenalty");
                    final BigDecimal chargeAmountPaid = (BigDecimal) loanChargePaid.get("amount");
                    final ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargeId, loanChargeId, chargeAmountPaid);
                    if (isPenalty) {
                        penaltyPayments.add(chargePaymentDTO);
                    } else {
                        feePayments.add(chargePaymentDTO);
                    }
                }
            }
            if (!isAccountTransfer) {
                isAccountTransfer = this.accountTransfersReadPlatformService.isAccountTransfer(Long.parseLong(transactionId),
                        PortfolioAccountType.SAVINGS);
            }
            final SavingsTransactionDTO transaction = new SavingsTransactionDTO(transactionOfficeId, paymentTypeId, transactionId,
                    transactionDate, transactionType, amount, reversed, feePayments, penaltyPayments, overdraftAmount, isAccountTransfer);

            newSavingsTransactions.add(transaction);

        }

        return new SavingsDTO(loanId, loanProductId, officeId, currencyData.code(), cashBasedAccountingEnabled,
                accrualBasedAccountingEnabled, newSavingsTransactions);
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for
     * Accrual Based accounting.
     * 
     * The target accounts for debits and credits are switched in case of a
     * reversal
     * 
     * @param office
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param loanProductId
     * @param paymentTypeId
     * @param loanId
     * @param transactionId
     * @param transactionDate
     * @param amount
     * @param isReversal
     */
    public void createAccrualBasedJournalEntriesAndReversalsForLoan(final Office office, final String currencyCode,
            final Integer accountTypeToBeDebited, final Integer accountTypeToBeCredited, final Long loanProductId,
            final Long paymentTypeId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountTypeToDebitId = accountTypeToBeDebited;
        int accountTypeToCreditId = accountTypeToBeCredited;
        // reverse debits and credits for reversals
        if (isReversal) {
            accountTypeToDebitId = accountTypeToBeCredited;
            accountTypeToCreditId = accountTypeToBeDebited;
        }
        createJournalEntriesForLoan(office, currencyCode, accountTypeToDebitId, accountTypeToCreditId, loanProductId, paymentTypeId,
                loanId, transactionId, transactionDate, amount);
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for
     * Accrual Based accounting.
     * 
     * The target accounts for debits and credits are switched in case of a
     * reversal
     * 
     * @param office
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param loanProductId
     * @param paymentTypeId
     * @param loanId
     * @param transactionId
     * @param transactionDate
     * @param amount
     * @param isReversal
     */
    public void createAccrualBasedJournalEntriesAndReversalsForLoanCharges(final Office office, final String currencyCode,
            final Integer accountTypeToBeDebited, final Integer accountTypeToBeCredited, final Long loanProductId, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal totalAmount, final Boolean isReversal,
            final List<ChargePaymentDTO> chargePaymentDTOs) {

        GLAccount receivableAccount = getLinkedGLAccountForLoanCharges(loanProductId, accountTypeToBeDebited, null);
        final Map<GLAccount, BigDecimal> creditDetailsMap = new LinkedHashMap<>();
        for (final ChargePaymentDTO chargePaymentDTO : chargePaymentDTOs) {
            final Long chargeId = chargePaymentDTO.getChargeId();
            final GLAccount chargeSpecificAccount = getLinkedGLAccountForLoanCharges(loanProductId, accountTypeToBeCredited, chargeId);
            BigDecimal chargeSpecificAmount = chargePaymentDTO.getAmount();

            // adjust net credit amount if the account is already present in the
            // map
            if (creditDetailsMap.containsKey(chargeSpecificAccount)) {
                final BigDecimal existingAmount = creditDetailsMap.get(chargeSpecificAccount);
                chargeSpecificAmount = chargeSpecificAmount.add(existingAmount);
            }
            creditDetailsMap.put(chargeSpecificAccount, chargeSpecificAmount);
        }

        BigDecimal totalCreditedAmount = BigDecimal.ZERO;
        for (final Map.Entry<GLAccount, BigDecimal> entry : creditDetailsMap.entrySet()) {
            final GLAccount account = entry.getKey();
            final BigDecimal amount = entry.getValue();
            totalCreditedAmount = totalCreditedAmount.add(amount);
            if (isReversal) {
                createDebitJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
                createCreditJournalEntryForLoan(office, currencyCode, receivableAccount, loanId, transactionId, transactionDate, amount);
            } else {
                createDebitJournalEntryForLoan(office, currencyCode, receivableAccount, loanId, transactionId, transactionDate, amount);
                createCreditJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
            }
        }

        if (totalAmount.compareTo(totalCreditedAmount) != 0) { throw new PlatformDataIntegrityException(
                "Meltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction",
                "Meltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction",
                totalCreditedAmount, totalAmount); }
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for
     * Cash Based accounting.
     * 
     * The target accounts for debits and credits are switched in case of a
     * reversal
     * 
     * @param office
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param savingsProductId
     * @param paymentTypeId
     * @param loanId
     * @param transactionId
     * @param transactionDate
     * @param amount
     * @param isReversal
     */
    public void createCashBasedJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Integer accountTypeToBeDebited, final Integer accountTypeToBeCredited, final Long savingsProductId,
            final Long paymentTypeId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountTypeToDebitId = accountTypeToBeDebited;
        int accountTypeToCreditId = accountTypeToBeCredited;
        // reverse debits and credits for reversals
        if (isReversal) {
            accountTypeToDebitId = accountTypeToBeCredited;
            accountTypeToCreditId = accountTypeToBeDebited;
        }
        createJournalEntriesForSavings(office, currencyCode, accountTypeToDebitId, accountTypeToCreditId, savingsProductId, paymentTypeId,
                loanId, transactionId, transactionDate, amount);
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for
     * Cash Based accounting.
     * 
     * The target accounts for debits and credits are switched in case of a
     * reversal
     * 
     * @param office
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param loanProductId
     * @param paymentTypeId
     * @param loanId
     * @param transactionId
     * @param transactionDate
     * @param amount
     * @param isReversal
     */
    public void createCashBasedJournalEntriesAndReversalsForLoan(final Office office, final String currencyCode,
            final Integer accountTypeToBeDebited, final Integer accountTypeToBeCredited, final Long loanProductId,
            final Long paymentTypeId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountTypeToDebitId = accountTypeToBeDebited;
        int accountTypeToCreditId = accountTypeToBeCredited;
        // reverse debits and credits for reversals
        if (isReversal) {
            accountTypeToDebitId = accountTypeToBeCredited;
            accountTypeToCreditId = accountTypeToBeDebited;
        }
        createJournalEntriesForLoan(office, currencyCode, accountTypeToDebitId, accountTypeToCreditId, loanProductId, paymentTypeId,
                loanId, transactionId, transactionDate, amount);
    }

    public void createCreditJournalEntryOrReversalForLoan(final Office office, final String currencyCode,
            final CASH_ACCOUNTS_FOR_LOAN accountMappingType, final Long loanProductId, final Long paymentTypeId, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount, final Boolean isReversal) {
        final int accountMappingTypeId = accountMappingType.getValue();
        createCreditJournalEntryOrReversalForLoan(office, currencyCode, accountMappingTypeId, loanProductId, paymentTypeId, loanId,
                transactionId, transactionDate, amount, isReversal);
    }

    public void createCreditJournalEntryOrReversalForLoan(final Office office, final String currencyCode,
            final ACCRUAL_ACCOUNTS_FOR_LOAN accountMappingType, final Long loanProductId, final Long paymentTypeId, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount, final Boolean isReversal) {
        final int accountMappingTypeId = accountMappingType.getValue();
        createCreditJournalEntryOrReversalForLoan(office, currencyCode, accountMappingTypeId, loanProductId, paymentTypeId, loanId,
                transactionId, transactionDate, amount, isReversal);
    }

    /**
     * @param latestGLClosure
     * @param transactionDate
     */
    public void checkForBranchClosures(final GLClosure latestGLClosure, final Date transactionDate) {
        /**
         * check if an accounting closure has happened for this branch after the
         * transaction Date
         **/
        if (latestGLClosure != null) {
            if (latestGLClosure.getClosingDate().after(transactionDate) || latestGLClosure.getClosingDate().equals(transactionDate)) { throw new JournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(), null, null); }
        }
    }

    public GLClosure getLatestClosureByBranch(final long officeId) {
        return this.closureRepository.getLatestGLClosureByBranch(officeId);
    }

    public Office getOfficeById(final long officeId) {
        return this.officeRepository.findOne(officeId);
    }

    private void createJournalEntriesForLoan(final Office office, final String currencyCode, final int accountTypeToDebitId,
            final int accountTypeToCreditId, final Long loanProductId, final Long paymentTypeId, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final GLAccount debitAccount = getLinkedGLAccountForLoanProduct(loanProductId, accountTypeToDebitId, paymentTypeId);
        final GLAccount creditAccount = getLinkedGLAccountForLoanProduct(loanProductId, accountTypeToCreditId, paymentTypeId);
        createDebitJournalEntryForLoan(office, currencyCode, debitAccount, loanId, transactionId, transactionDate, amount);
        createCreditJournalEntryForLoan(office, currencyCode, creditAccount, loanId, transactionId, transactionDate, amount);
    }

    private void createJournalEntriesForSavings(final Office office, final String currencyCode, final int accountTypeToDebitId,
            final int accountTypeToCreditId, final Long savingsProductId, final Long paymentTypeId, final Long savingsId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final GLAccount debitAccount = getLinkedGLAccountForSavingsProduct(savingsProductId, accountTypeToDebitId, paymentTypeId);
        final GLAccount creditAccount = getLinkedGLAccountForSavingsProduct(savingsProductId, accountTypeToCreditId, paymentTypeId);
        createDebitJournalEntryForSavings(office, currencyCode, debitAccount, savingsId, transactionId, transactionDate, amount);
        createCreditJournalEntryForSavings(office, currencyCode, creditAccount, savingsId, transactionId, transactionDate, amount);
    }

    public void createDebitJournalEntryOrReversalForLoan(final Office office, final String currencyCode, final int accountMappingTypeId,
            final Long loanProductId, final Long paymentTypeId, final Long loanId, final String transactionId, final Date transactionDate,
            final BigDecimal amount, final Boolean isReversal) {
        final GLAccount account = getLinkedGLAccountForLoanProduct(loanProductId, accountMappingTypeId, paymentTypeId);
        if (isReversal) {
            createCreditJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        }
    }

    public void createCreditJournalEntryOrReversalForLoanCharges(final Office office, final String currencyCode,
            final int accountMappingTypeId, final Long loanProductId, final Long loanId, final String transactionId,
            final Date transactionDate, final BigDecimal totalAmount, final Boolean isReversal,
            final List<ChargePaymentDTO> chargePaymentDTOs) {
        /***
         * Map to track each account and the net credit to be made for a
         * particular account
         ***/
        final Map<GLAccount, BigDecimal> creditDetailsMap = new LinkedHashMap<>();
        for (final ChargePaymentDTO chargePaymentDTO : chargePaymentDTOs) {
            final Long chargeId = chargePaymentDTO.getChargeId();
            final GLAccount chargeSpecificAccount = getLinkedGLAccountForLoanCharges(loanProductId, accountMappingTypeId, chargeId);
            BigDecimal chargeSpecificAmount = chargePaymentDTO.getAmount();

            // adjust net credit amount if the account is already present in the
            // map
            if (creditDetailsMap.containsKey(chargeSpecificAccount)) {
                final BigDecimal existingAmount = creditDetailsMap.get(chargeSpecificAccount);
                chargeSpecificAmount = chargeSpecificAmount.add(existingAmount);
            }
            creditDetailsMap.put(chargeSpecificAccount, chargeSpecificAmount);
        }

        BigDecimal totalCreditedAmount = BigDecimal.ZERO;
        for (final Map.Entry<GLAccount, BigDecimal> entry : creditDetailsMap.entrySet()) {
            final GLAccount account = entry.getKey();
            final BigDecimal amount = entry.getValue();
            totalCreditedAmount = totalCreditedAmount.add(amount);
            if (isReversal) {
                createDebitJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
            } else {
                createCreditJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
            }
        }

        // TODO: Vishwas Temporary validation to be removed before moving to
        // release branch
        if (totalAmount.compareTo(totalCreditedAmount) != 0) { throw new PlatformDataIntegrityException(
                "Meltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction",
                "Meltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction",
                totalCreditedAmount, totalAmount); }
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for
     * Cash Based accounting.
     * 
     * The target accounts for debits and credits are switched in case of a
     * reversal
     * 
     * @param office
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param savingsProductId
     * @param paymentTypeId
     * @param loanId
     * @param transactionId
     * @param transactionDate
     * @param amount
     * @param isReversal
     */
    public void createCashBasedJournalEntriesAndReversalsForSavingsCharges(final Office office, final String currencyCode,
            final CASH_ACCOUNTS_FOR_SAVINGS accountTypeToBeDebited, final CASH_ACCOUNTS_FOR_SAVINGS accountTypeToBeCredited,
            final Long savingsProductId, final Long paymentTypeId, final Long loanId, final String transactionId,
            final Date transactionDate, final BigDecimal totalAmount, final Boolean isReversal,
            final List<ChargePaymentDTO> chargePaymentDTOs) {
        // TODO Vishwas: Remove this validation, as and when appropriate Junit
        // tests are written for accounting
        /**
         * Accounting module currently supports a single charge per transaction,
         * throw an error if this is not the case here so any developers
         * changing the expected portfolio behavior would also take care of
         * modifying the accounting code appropriately
         **/
        if (chargePaymentDTOs.size() != 1) { throw new PlatformDataIntegrityException(
                "Recent Portfolio changes w.r.t Charges for Savings have Broken the accounting code",
                "Recent Portfolio changes w.r.t Charges for Savings have Broken the accounting code"); }
        ChargePaymentDTO chargePaymentDTO = chargePaymentDTOs.get(0);

        final GLAccount chargeSpecificAccount = getLinkedGLAccountForSavingsCharges(savingsProductId, accountTypeToBeCredited.getValue(),
                chargePaymentDTO.getChargeId());
        final GLAccount savingsControlAccount = getLinkedGLAccountForSavingsProduct(savingsProductId, accountTypeToBeDebited.getValue(),
                paymentTypeId);
        if (isReversal) {
            createDebitJournalEntryForSavings(office, currencyCode, chargeSpecificAccount, loanId, transactionId, transactionDate,
                    totalAmount);
            createCreditJournalEntryForSavings(office, currencyCode, savingsControlAccount, loanId, transactionId, transactionDate,
                    totalAmount);
        } else {
            createDebitJournalEntryForSavings(office, currencyCode, savingsControlAccount, loanId, transactionId, transactionDate,
                    totalAmount);
            createCreditJournalEntryForSavings(office, currencyCode, chargeSpecificAccount, loanId, transactionId, transactionDate,
                    totalAmount);
        }
    }

    public LoanTransaction getLoanTransactionById(final long loanTransactionId) {
        return this.loanTransactionRepository.findOne(loanTransactionId);
    }

    public SavingsAccountTransaction getSavingsTransactionById(final long savingsTransactionId) {
        return this.savingsAccountTransactionRepository.findOne(savingsTransactionId);
    }

    private void createCreditJournalEntryOrReversalForLoan(final Office office, final String currencyCode, final int accountMappingTypeId,
            final Long loanProductId, final Long paymentTypeId, final Long loanId, final String transactionId, final Date transactionDate,
            final BigDecimal amount, final Boolean isReversal) {
        final GLAccount account = getLinkedGLAccountForLoanProduct(loanProductId, accountMappingTypeId, paymentTypeId);
        if (isReversal) {
            createDebitJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        } else {
            createCreditJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        }
    }

    private void createCreditJournalEntryForLoan(final Office office, final String currencyCode, final GLAccount account,
            final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        LoanTransaction loanTransaction = null;
        SavingsAccountTransaction savingsAccountTransaction = null;
        final PaymentDetail paymentDetail = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            long id = Long.parseLong(transactionId);
            loanTransaction = this.loanTransactionRepository.findOne(id);
            modifiedTransactionId = LOAN_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, paymentDetail, account, currencyCode, modifiedTransactionId,
                manualEntry, transactionDate, JournalEntryType.CREDIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId, null,
                loanTransaction, savingsAccountTransaction);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void createCreditJournalEntryForSavings(final Office office, final String currencyCode, final GLAccount account,
            final Long savingsId, final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        LoanTransaction loanTransaction = null;
        SavingsAccountTransaction savingsAccountTransaction = null;
        final PaymentDetail paymentDetail = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            long id = Long.parseLong(transactionId);
            savingsAccountTransaction = this.savingsAccountTransactionRepository.findOne(id);
            modifiedTransactionId = SAVINGS_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, paymentDetail, account, currencyCode, modifiedTransactionId,
                manualEntry, transactionDate, JournalEntryType.CREDIT, amount, null, PortfolioProductType.SAVING.getValue(), savingsId,
                null, loanTransaction, savingsAccountTransaction);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void createDebitJournalEntryForLoan(final Office office, final String currencyCode, final GLAccount account, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        LoanTransaction loanTransaction = null;
        SavingsAccountTransaction savingsAccountTransaction = null;
        final PaymentDetail paymentDetail = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            long id = Long.parseLong(transactionId);
            loanTransaction = this.loanTransactionRepository.findOne(id);
            modifiedTransactionId = LOAN_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, paymentDetail, account, currencyCode, modifiedTransactionId,
                manualEntry, transactionDate, JournalEntryType.DEBIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId, null,
                loanTransaction, savingsAccountTransaction);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void createDebitJournalEntryForSavings(final Office office, final String currencyCode, final GLAccount account,
            final Long savingsId, final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        LoanTransaction loanTransaction = null;
        SavingsAccountTransaction savingsAccountTransaction = null;
        final PaymentDetail paymentDetail = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            long id = Long.parseLong(transactionId);
            savingsAccountTransaction = this.savingsAccountTransactionRepository.findOne(id);
            modifiedTransactionId = SAVINGS_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, paymentDetail, account, currencyCode, modifiedTransactionId,
                manualEntry, transactionDate, JournalEntryType.DEBIT, amount, null, PortfolioProductType.SAVING.getValue(), savingsId,
                null, loanTransaction, savingsAccountTransaction);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private GLAccount getLinkedGLAccountForLoanProduct(final Long loanProductId, final int accountMappingTypeId, final Long paymentTypeId) {
        GLAccount glAccount = null;
        if (isOrganizationAccount(accountMappingTypeId)) {
            FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                    .findByFinancialActivityTypeWithNotFoundDetection(accountMappingTypeId);
            glAccount = financialActivityAccount.getGlAccount();
        } else {
            ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(loanProductId,
                    PortfolioProductType.LOAN.getValue(), accountMappingTypeId);

            /****
             * Get more specific mapping for FUND source accounts (based on
             * payment channels). Note that fund source placeholder ID would be
             * same for both cash and accrual accounts
             ***/
            if (accountMappingTypeId == CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue()) {
                final ProductToGLAccountMapping paymentChannelSpecificAccountMapping = this.accountMappingRepository
                        .findByProductIdAndProductTypeAndFinancialAccountTypeAndPaymentTypeId(loanProductId,
                                PortfolioProductType.LOAN.getValue(), accountMappingTypeId, paymentTypeId);
                if (paymentChannelSpecificAccountMapping != null) {
                    accountMapping = paymentChannelSpecificAccountMapping;
                }
            }

            if (accountMapping == null) { throw new ProductToGLAccountMappingNotFoundException(PortfolioProductType.LOAN, loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.OVERPAYMENT.toString()); }
            glAccount = accountMapping.getGlAccount();
        }
        return glAccount;
    }

    private GLAccount getLinkedGLAccountForLoanCharges(final Long loanProductId, final int accountMappingTypeId, final Long chargeId) {
        ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(loanProductId,
                PortfolioProductType.LOAN.getValue(), accountMappingTypeId);
        /*****
         * Get more specific mappings for Charges and penalties (based on the
         * actual charge /penalty coupled with the loan product). Note the
         * income from fees and income from penalties placeholder ID would be
         * the same for both cash and accrual based accounts
         *****/

        // Vishwas TODO: remove this condition as it should always be true
        if (accountMappingTypeId == CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue()
                || accountMappingTypeId == CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue()) {
            final ProductToGLAccountMapping chargeSpecificIncomeAccountMapping = this.accountMappingRepository
                    .findByProductIdAndProductTypeAndFinancialAccountTypeAndChargeId(loanProductId, PortfolioProductType.LOAN.getValue(),
                            accountMappingTypeId, chargeId);
            if (chargeSpecificIncomeAccountMapping != null) {
                accountMapping = chargeSpecificIncomeAccountMapping;
            }
        }
        return accountMapping.getGlAccount();
    }

    private GLAccount getLinkedGLAccountForSavingsCharges(final Long savingsProductId, final int accountMappingTypeId, final Long chargeId) {
        ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(savingsProductId,
                PortfolioProductType.SAVING.getValue(), accountMappingTypeId);
        /*****
         * Get more specific mappings for Charges and penalties (based on the
         * actual charge /penalty coupled with the loan product). Note the
         * income from fees and income from penalties placeholder ID would be
         * the same for both cash and accrual based accounts
         *****/

        // Vishwas TODO: remove this condition as it should always be true
        if (accountMappingTypeId == CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES.getValue()
                || accountMappingTypeId == CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue()) {
            final ProductToGLAccountMapping chargeSpecificIncomeAccountMapping = this.accountMappingRepository
                    .findByProductIdAndProductTypeAndFinancialAccountTypeAndChargeId(savingsProductId,
                            PortfolioProductType.SAVING.getValue(), accountMappingTypeId, chargeId);
            if (chargeSpecificIncomeAccountMapping != null) {
                accountMapping = chargeSpecificIncomeAccountMapping;
            }
        }
        return accountMapping.getGlAccount();
    }

    private GLAccount getLinkedGLAccountForSavingsProduct(final Long savingsProductId, final int accountMappingTypeId,
            final Long paymentTypeId) {
        GLAccount glAccount = null;
        if (isOrganizationAccount(accountMappingTypeId)) {
            FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                    .findByFinancialActivityTypeWithNotFoundDetection(accountMappingTypeId);
            glAccount = financialActivityAccount.getGlAccount();
        } else {
            ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(savingsProductId,
                    PortfolioProductType.SAVING.getValue(), accountMappingTypeId);
            /****
             * Get more specific mapping for FUND source accounts (based on
             * payment channels). Note that fund source placeholder ID would be
             * same for both cash and accrual accounts
             ***/
            if (accountMappingTypeId == CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue()) {
                final ProductToGLAccountMapping paymentChannelSpecificAccountMapping = this.accountMappingRepository
                        .findByProductIdAndProductTypeAndFinancialAccountTypeAndPaymentTypeId(savingsProductId,
                                PortfolioProductType.SAVING.getValue(), accountMappingTypeId, paymentTypeId);
                if (paymentChannelSpecificAccountMapping != null) {
                    accountMapping = paymentChannelSpecificAccountMapping;
                }
            }
            glAccount = accountMapping.getGlAccount();
        }
        return glAccount;
    }

    private boolean isOrganizationAccount(final int accountMappingTypeId) {
        boolean isOrganizationAccount = false;
        if (FINANCIAL_ACTIVITY.fromInt(accountMappingTypeId) != null) {
            isOrganizationAccount = true;
        }
        return isOrganizationAccount;
    }
}
