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
package org.apache.fineract.accounting.journalentry.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForShares;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.ClientChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.ClientTransactionDTO;
import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.apache.fineract.accounting.journalentry.data.LoanTransactionDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsTransactionDTO;
import org.apache.fineract.accounting.journalentry.data.SharesDTO;
import org.apache.fineract.accounting.journalentry.data.SharesTransactionDTO;
import org.apache.fineract.accounting.journalentry.data.TaxPaymentDTO;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.journalentry.exception.JournalEntryInvalidException;
import org.apache.fineract.accounting.journalentry.exception.JournalEntryInvalidException.GlJournalEntryInvalidReason;
import org.apache.fineract.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingNotFoundException;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.journalentry.LoanJournalEntryCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientTransaction;
import org.apache.fineract.portfolio.client.domain.ClientTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountTransactionEnumData;
import org.springframework.dao.DataAccessException;

@RequiredArgsConstructor
public class AccountingProcessorHelper {

    public static final String LOAN_TRANSACTION_IDENTIFIER = "L";
    public static final String SAVINGS_TRANSACTION_IDENTIFIER = "S";
    public static final String CLIENT_TRANSACTION_IDENTIFIER = "C";
    public static final String PROVISIONING_TRANSACTION_IDENTIFIER = "P";
    public static final String SHARE_TRANSACTION_IDENTIFIER = "SH";

    private final JournalEntryRepository glJournalEntryRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository;
    private final GLClosureRepository closureRepository;
    private final GLAccountRepository glAccountRepository;
    private final OfficeRepository officeRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final ClientTransactionRepository clientTransactionRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;
    private final BusinessEventNotifierService businessEventNotifierService;

    public LoanDTO populateLoanDtoFromMap(final Map<String, Object> accountingBridgeData, final boolean cashBasedAccountingEnabled,
            final boolean upfrontAccrualBasedAccountingEnabled, final boolean periodicAccrualBasedAccountingEnabled) {
        final Long loanId = (Long) accountingBridgeData.get("loanId");
        final Long loanProductId = (Long) accountingBridgeData.get("loanProductId");
        final Long officeId = (Long) accountingBridgeData.get("officeId");
        final String currencyCode = (String) accountingBridgeData.get("currencyCode");
        final List<LoanTransactionDTO> newLoanTransactions = new ArrayList<>();
        boolean isAccountTransfer = (Boolean) accountingBridgeData.get("isAccountTransfer");
        boolean isLoanMarkedAsChargeOff = (Boolean) accountingBridgeData.get("isChargeOff");
        boolean isLoanMarkedAsFraud = (Boolean) accountingBridgeData.get("isFraud");

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> newTransactionsMap = (List<Map<String, Object>>) accountingBridgeData.get("newLoanTransactions");

        for (final Map<String, Object> map : newTransactionsMap) {
            final Long transactionOfficeId = (Long) map.get("officeId");
            final String transactionId = ((Long) map.get("id")).toString();
            final LocalDate transactionDate = ((LocalDate) map.get("date"));
            final LoanTransactionEnumData transactionType = (LoanTransactionEnumData) map.get("type");
            final BigDecimal amount = (BigDecimal) map.get("amount");
            final BigDecimal principal = (BigDecimal) map.get("principalPortion");
            final BigDecimal interest = (BigDecimal) map.get("interestPortion");
            final BigDecimal fees = (BigDecimal) map.get("feeChargesPortion");
            final BigDecimal penalties = (BigDecimal) map.get("penaltyChargesPortion");
            final BigDecimal overPayments = (BigDecimal) map.get("overPaymentPortion");
            final boolean reversed = (Boolean) map.get("reversed");
            final Long paymentTypeId = (Long) map.get("paymentTypeId");
            final String chargeRefundChargeType = (String) map.get("chargeRefundChargeType");
            final LoanChargeData loanChargeData = (LoanChargeData) map.get("loanChargeData");

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
                    final ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargeId, chargeAmountPaid, loanChargeId);
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
                    penaltyPaymentDetails, feePaymentDetails, isAccountTransfer, chargeRefundChargeType, loanChargeData);
            Boolean isLoanToLoanTransfer = (Boolean) accountingBridgeData.get("isLoanToLoanTransfer");
            transaction.setLoanToLoanTransfer(isLoanToLoanTransfer != null && isLoanToLoanTransfer);
            newLoanTransactions.add(transaction);

        }

        return new LoanDTO(loanId, loanProductId, officeId, currencyCode, cashBasedAccountingEnabled, upfrontAccrualBasedAccountingEnabled,
                periodicAccrualBasedAccountingEnabled, newLoanTransactions, isLoanMarkedAsChargeOff, isLoanMarkedAsFraud);
    }

    public SavingsDTO populateSavingsDtoFromMap(final Map<String, Object> accountingBridgeData, final boolean cashBasedAccountingEnabled,
            final boolean accrualBasedAccountingEnabled) {
        final Long loanId = (Long) accountingBridgeData.get("savingsId");
        final Long loanProductId = (Long) accountingBridgeData.get("savingsProductId");
        final Long officeId = (Long) accountingBridgeData.get("officeId");
        final String currencyCode = (String) accountingBridgeData.get("currencyCode");
        final List<SavingsTransactionDTO> newSavingsTransactions = new ArrayList<>();
        boolean isAccountTransfer = (Boolean) accountingBridgeData.get("isAccountTransfer");

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> newTransactionsMap = (List<Map<String, Object>>) accountingBridgeData.get("newSavingsTransactions");

        for (final Map<String, Object> map : newTransactionsMap) {
            final Long transactionOfficeId = (Long) map.get("officeId");
            final String transactionId = ((Long) map.get("id")).toString();
            final LocalDate transactionDate = ((LocalDate) map.get("date"));
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
                    final ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargeId, chargeAmountPaid, loanChargeId);
                    if (isPenalty) {
                        penaltyPayments.add(chargePaymentDTO);
                    } else {
                        feePayments.add(chargePaymentDTO);
                    }
                }
            }

            final List<TaxPaymentDTO> taxPayments = new ArrayList<>();
            if (map.containsKey("taxDetails")) {
                @SuppressWarnings("unchecked")
                final List<Map<String, Object>> taxDataList = (List<Map<String, Object>>) map.get("taxDetails");
                for (final Map<String, Object> taxData : taxDataList) {
                    final BigDecimal taxAmount = (BigDecimal) taxData.get("amount");
                    final Long creditAccountId = (Long) taxData.get("creditAccountId");
                    final Long debitAccountId = (Long) taxData.get("debitAccountId");
                    taxPayments.add(new TaxPaymentDTO(debitAccountId, creditAccountId, taxAmount));
                }
            }

            if (!isAccountTransfer) {
                isAccountTransfer = this.accountTransfersReadPlatformService.isAccountTransfer(Long.parseLong(transactionId),
                        PortfolioAccountType.SAVINGS);
            }
            final SavingsTransactionDTO transaction = new SavingsTransactionDTO(transactionOfficeId, paymentTypeId, transactionId,
                    transactionDate, transactionType, amount, reversed, feePayments, penaltyPayments, overdraftAmount, isAccountTransfer,
                    taxPayments);

            newSavingsTransactions.add(transaction);

        }

        return new SavingsDTO(loanId, loanProductId, officeId, currencyCode, cashBasedAccountingEnabled, accrualBasedAccountingEnabled,
                newSavingsTransactions);
    }

    public SharesDTO populateSharesDtoFromMap(final Map<String, Object> accountingBridgeData, final boolean cashBasedAccountingEnabled,
            final boolean accrualBasedAccountingEnabled) {
        final Long shareAccountId = (Long) accountingBridgeData.get("shareAccountId");
        final Long shareProductId = (Long) accountingBridgeData.get("shareProductId");
        final Long officeId = (Long) accountingBridgeData.get("officeId");
        final String currencyCode = (String) accountingBridgeData.get("currencyCode");
        final List<SharesTransactionDTO> newTransactions = new ArrayList<>();

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> newTransactionsMap = (List<Map<String, Object>>) accountingBridgeData.get("newTransactions");

        for (final Map<String, Object> map : newTransactionsMap) {
            final Long transactionOfficeId = (Long) map.get("officeId");
            final String transactionId = ((Long) map.get("id")).toString();
            final LocalDate transactionDate = ((LocalDate) map.get("date"));
            final ShareAccountTransactionEnumData transactionType = (ShareAccountTransactionEnumData) map.get("type");
            final ShareAccountTransactionEnumData transactionStatus = (ShareAccountTransactionEnumData) map.get("status");
            final BigDecimal amount = (BigDecimal) map.get("amount");
            final BigDecimal chargeAmount = (BigDecimal) map.get("chargeAmount");
            final Long paymentTypeId = (Long) map.get("paymentTypeId");

            final List<ChargePaymentDTO> feePayments = new ArrayList<>();
            // extract charge payment details (if exists)
            if (map.containsKey("chargesPaid")) {
                @SuppressWarnings("unchecked")
                final List<Map<String, Object>> chargesPaidData = (List<Map<String, Object>>) map.get("chargesPaid");
                for (final Map<String, Object> chargePaid : chargesPaidData) {
                    final Long chargeId = (Long) chargePaid.get("chargeId");
                    final Long loanChargeId = (Long) chargePaid.get("sharesChargeId");
                    final BigDecimal chargeAmountPaid = (BigDecimal) chargePaid.get("amount");
                    final ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargeId, chargeAmountPaid, loanChargeId);
                    feePayments.add(chargePaymentDTO);
                }
            }
            final SharesTransactionDTO transaction = new SharesTransactionDTO(transactionOfficeId, paymentTypeId, transactionId,
                    transactionDate, transactionType, transactionStatus, amount, chargeAmount, feePayments);

            newTransactions.add(transaction);

        }

        return new SharesDTO(shareAccountId, shareProductId, officeId, currencyCode, cashBasedAccountingEnabled,
                accrualBasedAccountingEnabled, newTransactions);
    }

    public ClientTransactionDTO populateClientTransactionDtoFromMap(final Map<String, Object> accountingBridgeData) {

        final Long transactionOfficeId = (Long) accountingBridgeData.get("officeId");
        final Long clientId = (Long) accountingBridgeData.get("clientId");
        final Long transactionId = (Long) accountingBridgeData.get("id");
        final LocalDate transactionDate = ((LocalDate) accountingBridgeData.get("date"));
        final EnumOptionData transactionType = (EnumOptionData) accountingBridgeData.get("type");
        final BigDecimal amount = (BigDecimal) accountingBridgeData.get("amount");
        final boolean reversed = (Boolean) accountingBridgeData.get("reversed");
        final Long paymentTypeId = (Long) accountingBridgeData.get("paymentTypeId");
        final String currencyCode = (String) accountingBridgeData.get("currencyCode");
        final Boolean accountingEnabled = (Boolean) accountingBridgeData.get("accountingEnabled");

        final List<ClientChargePaymentDTO> clientChargePaymentDTOs = new ArrayList<>();
        // extract client charge payment details (if exists)
        if (accountingBridgeData.containsKey("clientChargesPaid")) {
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> clientChargesPaidData = (List<Map<String, Object>>) accountingBridgeData
                    .get("clientChargesPaid");
            for (final Map<String, Object> clientChargePaid : clientChargesPaidData) {
                final Long chargeId = (Long) clientChargePaid.get("chargeId");
                final Long clientChargeId = (Long) clientChargePaid.get("clientChargeId");
                final boolean isPenalty = (Boolean) clientChargePaid.get("isPenalty");
                final BigDecimal chargeAmountPaid = (BigDecimal) clientChargePaid.get("amount");
                final Long incomeAccountId = (Long) clientChargePaid.get("incomeAccountId");
                final ClientChargePaymentDTO clientChargePaymentDTO = new ClientChargePaymentDTO(chargeId, chargeAmountPaid, clientChargeId,
                        isPenalty, incomeAccountId);
                clientChargePaymentDTOs.add(clientChargePaymentDTO);
            }
        }

        return new ClientTransactionDTO(clientId, transactionOfficeId, paymentTypeId, transactionId, transactionDate, transactionType,
                currencyCode, amount, reversed, accountingEnabled, clientChargePaymentDTOs);

    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for Accrual Based accounting.
     *
     * The target accounts for debits and credits are switched in case of a reversal
     *
     * @param office
     *            office
     * @param currencyCode
     *            currencyCode
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param loanProductId
     *            loanProductId
     * @param loanId
     *            loanId
     * @param transactionId
     *            transactionId
     * @param transactionDate
     *            transactionDate
     * @param totalAmount
     *            totalAmount
     * @param isReversal
     *            isReversal
     * @param chargePaymentDTOs
     *            chargePaymentDTOs
     */
    public void createJournalEntriesAndReversalsForLoanCharges(final Office office, final String currencyCode,
            final Integer accountTypeToBeDebited, final Integer accountTypeToBeCredited, final Long loanProductId, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal totalAmount, final Boolean isReversal,
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

        if (totalAmount.compareTo(totalCreditedAmount) != 0) {
            throw new PlatformDataIntegrityException(
                    "Meltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction",
                    "Meltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction",
                    totalCreditedAmount, totalAmount);
        }
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for Cash Based accounting.
     *
     * The target accounts for debits and credits are switched in case of a reversal
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
            final Long paymentTypeId, final Long loanId, final String transactionId, final LocalDate transactionDate,
            final BigDecimal amount, final Boolean isReversal) {
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
     * Convenience method that creates a pair of related Debits and Credits for Cash Based accounting.
     *
     * The target accounts for debits and credits are switched in case of a reversal
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
    public void createJournalEntriesAndReversalsForLoan(final Office office, final String currencyCode,
            final Integer accountTypeToBeDebited, final Integer accountTypeToBeCredited, final Long loanProductId, final Long paymentTypeId,
            final Long loanId, final String transactionId, final LocalDate transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountTypeToDebitId = accountTypeToBeDebited;
        int accountTypeToCreditId = accountTypeToBeCredited;
        // reverse debits and credits for reversals
        if (isReversal) {
            accountTypeToDebitId = accountTypeToBeCredited;
            accountTypeToCreditId = accountTypeToBeDebited;
        }
        createJournalEntriesForLoan(office, currencyCode, accountTypeToDebitId, accountTypeToCreditId, loanProductId, paymentTypeId, loanId,
                transactionId, transactionDate, amount);
    }

    public void createSplitJournalEntriesAndReversalsForLoan(Office office, String currencyCode,
            List<JournalAmountHolder> splitAccountsHolder, JournalAmountHolder totalAccountHolder, Long loanProductId, Long paymentTypeId,
            Long loanId, String transactionId, LocalDate transactionDate, Boolean isReversal) {
        splitAccountsHolder.forEach(journalItemHolder -> {
            final GLAccount account = getLinkedGLAccountForLoanProduct(loanProductId, journalItemHolder.getAccountType(), paymentTypeId);
            if (isReversal) {
                createCreditJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate,
                        journalItemHolder.getAmount());
            } else {
                createDebitJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate,
                        journalItemHolder.getAmount());
            }

        });
        final GLAccount totalAccount = getLinkedGLAccountForLoanProduct(loanProductId, totalAccountHolder.getAccountType(), paymentTypeId);
        if (isReversal) {
            createDebitJournalEntryForLoan(office, currencyCode, totalAccount, loanId, transactionId, transactionDate,
                    totalAccountHolder.getAmount());
        } else {
            createCreditJournalEntryForLoan(office, currencyCode, totalAccount, loanId, transactionId, transactionDate,
                    totalAccountHolder.getAmount());
        }
    }

    public void createCreditJournalEntryOrReversalForLoan(final Office office, final String currencyCode,
            final CashAccountsForLoan accountMappingType, final Long loanProductId, final Long paymentTypeId, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        final int accountMappingTypeId = accountMappingType.getValue();
        createCreditJournalEntryOrReversalForLoan(office, currencyCode, accountMappingTypeId, loanProductId, paymentTypeId, loanId,
                transactionId, transactionDate, amount, isReversal);
    }

    public void createCreditJournalEntryOrReversalForLoan(final Office office, final String currencyCode,
            final AccrualAccountsForLoan accountMappingType, final Long loanProductId, final Long paymentTypeId, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        final int accountMappingTypeId = accountMappingType.getValue();
        createCreditJournalEntryOrReversalForLoan(office, currencyCode, accountMappingTypeId, loanProductId, paymentTypeId, loanId,
                transactionId, transactionDate, amount, isReversal);
    }

    /**
     * @param latestGLClosure
     * @param transactionDate
     */
    public void checkForBranchClosures(final GLClosure latestGLClosure, final LocalDate transactionDate) {
        // check if an accounting closure has happened for this branch after the transaction Date
        if (latestGLClosure != null) {
            if (!DateUtils.isBefore(latestGLClosure.getClosingDate(), transactionDate)) {
                throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(),
                        null, null);
            }
        }
    }

    public GLClosure getLatestClosureByBranch(final long officeId) {
        return this.closureRepository.getLatestGLClosureByBranch(officeId);
    }

    private void createJournalEntriesForLoan(final Office office, final String currencyCode, final int accountTypeToDebitId,
            final int accountTypeToCreditId, final Long loanProductId, final Long paymentTypeId, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final GLAccount debitAccount = getLinkedGLAccountForLoanProduct(loanProductId, accountTypeToDebitId, paymentTypeId);
        final GLAccount creditAccount = getLinkedGLAccountForLoanProduct(loanProductId, accountTypeToCreditId, paymentTypeId);
        createDebitJournalEntryForLoan(office, currencyCode, debitAccount, loanId, transactionId, transactionDate, amount);
        createCreditJournalEntryForLoan(office, currencyCode, creditAccount, loanId, transactionId, transactionDate, amount);
    }

    private void createJournalEntriesForSavings(final Office office, final String currencyCode, final int accountTypeToDebitId,
            final int accountTypeToCreditId, final Long savingsProductId, final Long paymentTypeId, final Long savingsId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final GLAccount debitAccount = getLinkedGLAccountForSavingsProduct(savingsProductId, accountTypeToDebitId, paymentTypeId);
        final GLAccount creditAccount = getLinkedGLAccountForSavingsProduct(savingsProductId, accountTypeToCreditId, paymentTypeId);
        createDebitJournalEntryForSavings(office, currencyCode, debitAccount, savingsId, transactionId, transactionDate, amount);
        createCreditJournalEntryForSavings(office, currencyCode, creditAccount, savingsId, transactionId, transactionDate, amount);
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for Cash Based accounting.
     *
     * The target accounts for debits and credits are switched in case of a reversal
     *
     * @param office
     * @param currencyCode
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param savingsProductId
     * @param paymentTypeId
     * @param savingsId
     * @param transactionId
     * @param transactionDate
     * @param amount
     * @param isReversal
     * @param taxDetails
     */
    public void createCashBasedJournalEntriesAndReversalsForSavingsTax(final Office office, final String currencyCode,
            final CashAccountsForSavings accountTypeToBeDebited, final CashAccountsForSavings accountTypeToBeCredited,
            final Long savingsProductId, final Long paymentTypeId, final Long savingsId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal, final List<TaxPaymentDTO> taxDetails) {

        for (TaxPaymentDTO taxPaymentDTO : taxDetails) {
            if (taxPaymentDTO.getAmount() != null) {
                if (taxPaymentDTO.getCreditAccountId() == null) {
                    createCashBasedCreditJournalEntriesAndReversalsForSavings(office, currencyCode, accountTypeToBeCredited.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, taxPaymentDTO.getAmount(),
                            isReversal);
                } else {
                    createCashBasedCreditJournalEntriesAndReversalsForSavings(office, currencyCode, taxPaymentDTO.getCreditAccountId(),
                            savingsId, transactionId, transactionDate, taxPaymentDTO.getAmount(), isReversal);
                }
            }
        }
        createCashBasedDebitJournalEntriesAndReversalsForSavings(office, currencyCode, accountTypeToBeDebited.getValue(), savingsProductId,
                paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
    }

    public void createAccrualBasedJournalEntriesAndReversalsForSavingsTax(final Office office, final String currencyCode,
            final AccrualAccountsForSavings accountTypeToBeDebited, final AccrualAccountsForSavings accountTypeToBeCredited,
            final Long savingsProductId, final Long paymentTypeId, final Long savingsId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal, final List<TaxPaymentDTO> taxDetails) {

        for (TaxPaymentDTO taxPaymentDTO : taxDetails) {
            if (taxPaymentDTO.getAmount() != null) {
                if (taxPaymentDTO.getCreditAccountId() == null) {
                    createAccrualBasedCreditJournalEntriesAndReversalsForSavings(office, currencyCode, accountTypeToBeCredited.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, taxPaymentDTO.getAmount(),
                            isReversal);
                } else {
                    createAccrualBasedBasedCreditJournalEntriesAndReversalsForSavings(office, currencyCode,
                            taxPaymentDTO.getCreditAccountId(), savingsId, transactionId, transactionDate, taxPaymentDTO.getAmount(),
                            isReversal);
                }
            }
        }
        createAccrualBasedDebitJournalEntriesAndReversalsForSavings(office, currencyCode, accountTypeToBeDebited.getValue(),
                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
    }

    public void createCashBasedDebitJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Integer accountTypeToBeDebited, final Long savingsProductId, final Long paymentTypeId, final Long savingsId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        // reverse debits and credits for reversals
        if (isReversal) {
            createCreditJournalEntriesForSavings(office, currencyCode, accountTypeToBeDebited, savingsProductId, paymentTypeId, savingsId,
                    transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntriesForSavings(office, currencyCode, accountTypeToBeDebited, savingsProductId, paymentTypeId, savingsId,
                    transactionId, transactionDate, amount);
        }
    }

    public void createCashBasedCreditJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Integer accountTypeToBeCredited, final Long savingsProductId, final Long paymentTypeId, final Long savingsId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        // reverse debits and credits for reversals
        if (isReversal) {
            createDebitJournalEntriesForSavings(office, currencyCode, accountTypeToBeCredited, savingsProductId, paymentTypeId, savingsId,
                    transactionId, transactionDate, amount);
        } else {
            createCreditJournalEntriesForSavings(office, currencyCode, accountTypeToBeCredited, savingsProductId, paymentTypeId, savingsId,
                    transactionId, transactionDate, amount);
        }
    }

    public void createCashBasedDebitJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Long debitAccountId, final Long savingsId, final String transactionId, final LocalDate transactionDate,
            final BigDecimal amount, final Boolean isReversal) {
        // reverse debits and credits for reversals
        final GLAccount debitAccount = getGLAccountById(debitAccountId);
        if (isReversal) {
            createCreditJournalEntryForSavings(office, currencyCode, debitAccount, savingsId, transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntryForSavings(office, currencyCode, debitAccount, savingsId, transactionId, transactionDate, amount);
        }
    }

    public void createCashBasedCreditJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Long creditAccountId, final Long savingsId, final String transactionId, final LocalDate transactionDate,
            final BigDecimal amount, final Boolean isReversal) {
        // reverse debits and credits for reversals
        final GLAccount creditAccount = getGLAccountById(creditAccountId);
        if (isReversal) {
            createDebitJournalEntryForSavings(office, currencyCode, creditAccount, savingsId, transactionId, transactionDate, amount);
        } else {
            createCreditJournalEntryForSavings(office, currencyCode, creditAccount, savingsId, transactionId, transactionDate, amount);
        }
    }

    public void createAccrualBasedDebitJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Integer accountTypeToBeDebited, final Long savingsProductId, final Long paymentTypeId, final Long savingsId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        // reverse debits and credits for reversals
        if (isReversal) {
            createCreditJournalEntriesForSavings(office, currencyCode, accountTypeToBeDebited, savingsProductId, paymentTypeId, savingsId,
                    transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntriesForSavings(office, currencyCode, accountTypeToBeDebited, savingsProductId, paymentTypeId, savingsId,
                    transactionId, transactionDate, amount);
        }
    }

    public void createAccrualBasedCreditJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Integer accountTypeToBeCredited, final Long savingsProductId, final Long paymentTypeId, final Long savingsId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        // reverse debits and credits for reversals
        if (isReversal) {
            createDebitJournalEntriesForSavings(office, currencyCode, accountTypeToBeCredited, savingsProductId, paymentTypeId, savingsId,
                    transactionId, transactionDate, amount);
        } else {
            createCreditJournalEntriesForSavings(office, currencyCode, accountTypeToBeCredited, savingsProductId, paymentTypeId, savingsId,
                    transactionId, transactionDate, amount);
        }
    }

    public void createAccrualBasedDebitJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Long debitAccountId, final Long savingsId, final String transactionId, final LocalDate transactionDate,
            final BigDecimal amount, final Boolean isReversal) {
        // reverse debits and credits for reversals
        final GLAccount debitAccount = getGLAccountById(debitAccountId);
        if (isReversal) {
            createCreditJournalEntryForSavings(office, currencyCode, debitAccount, savingsId, transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntryForSavings(office, currencyCode, debitAccount, savingsId, transactionId, transactionDate, amount);
        }
    }

    public void createAccrualBasedBasedCreditJournalEntriesAndReversalsForSavings(final Office office, final String currencyCode,
            final Long creditAccountId, final Long savingsId, final String transactionId, final LocalDate transactionDate,
            final BigDecimal amount, final Boolean isReversal) {
        // reverse debits and credits for reversals
        final GLAccount creditAccount = getGLAccountById(creditAccountId);
        if (isReversal) {
            createDebitJournalEntryForSavings(office, currencyCode, creditAccount, savingsId, transactionId, transactionDate, amount);
        } else {
            createCreditJournalEntryForSavings(office, currencyCode, creditAccount, savingsId, transactionId, transactionDate, amount);
        }
    }

    private void createDebitJournalEntriesForSavings(final Office office, final String currencyCode, final int accountTypeToDebitId,
            final Long savingsProductId, final Long paymentTypeId, final Long savingsId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount) {
        final GLAccount debitAccount = getLinkedGLAccountForSavingsProduct(savingsProductId, accountTypeToDebitId, paymentTypeId);
        createDebitJournalEntryForSavings(office, currencyCode, debitAccount, savingsId, transactionId, transactionDate, amount);
    }

    private void createCreditJournalEntriesForSavings(final Office office, final String currencyCode, final int accountTypeToCreditId,
            final Long savingsProductId, final Long paymentTypeId, final Long savingsId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount) {
        final GLAccount creditAccount = getLinkedGLAccountForSavingsProduct(savingsProductId, accountTypeToCreditId, paymentTypeId);
        createCreditJournalEntryForSavings(office, currencyCode, creditAccount, savingsId, transactionId, transactionDate, amount);
    }

    public void createDebitJournalEntryOrReversalForLoan(final Office office, final String currencyCode, final int accountMappingTypeId,
            final Long loanProductId, final Long paymentTypeId, final Long loanId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        final GLAccount account = getLinkedGLAccountForLoanProduct(loanProductId, accountMappingTypeId, paymentTypeId);
        if (isReversal) {
            createCreditJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        }
    }

    public void createDebitJournalEntryOrReversalForLoanCharges(final Office office, final String currencyCode,
            final int accountMappingTypeId, final Long loanProductId, final Long chargeId, final Long loanId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        final GLAccount account = getLinkedGLAccountForLoanCharges(loanProductId, accountMappingTypeId, chargeId);
        if (isReversal) {
            createCreditJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        }
    }

    public void createCreditJournalEntryOrReversalForLoanCharges(final Office office, final String currencyCode,
            final int accountMappingTypeId, final Long loanProductId, final Long loanId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal totalAmount, final Boolean isReversal,
            final List<ChargePaymentDTO> chargePaymentDTOs) {
        /***
         * Map to track each account and the net credit to be made for a particular account
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
        if (totalAmount.compareTo(totalCreditedAmount) != 0) {
            throw new PlatformDataIntegrityException(
                    "Meltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction",
                    "Meltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction",
                    totalCreditedAmount, totalAmount);
        }
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for Cash Based accounting.
     *
     * The target accounts for debits and credits are switched in case of a reversal
     *
     * @param office
     *            office
     * @param currencyCode
     *            currencyCode
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param savingsProductId
     *            savingsProductId
     * @param paymentTypeId
     *            paymentTypeId
     * @param loanId
     *            loanId
     * @param transactionId
     *            transactionId
     * @param transactionDate
     *            transactionDate
     * @param totalAmount
     *            totalAmount
     * @param isReversal
     *            isReversal
     * @param chargePaymentDTOs
     *            chargePaymentDTOs
     */
    public void createCashBasedJournalEntriesAndReversalsForSavingsCharges(final Office office, final String currencyCode,
            final CashAccountsForSavings accountTypeToBeDebited, CashAccountsForSavings accountTypeToBeCredited,
            final Long savingsProductId, final Long paymentTypeId, final Long loanId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal totalAmount, final Boolean isReversal,
            final List<ChargePaymentDTO> chargePaymentDTOs) {
        // TODO Vishwas: Remove this validation, as and when appropriate Junit
        // tests are written for accounting
        /**
         * Accounting module currently supports a single charge per transaction, throw an error if this is not the case
         * here so any developers changing the expected portfolio behavior would also take care of modifying the
         * accounting code appropriately
         **/
        if (chargePaymentDTOs.size() != 1) {
            throw new PlatformDataIntegrityException("Recent Portfolio changes w.r.t Charges for Savings have Broken the accounting code",
                    "Recent Portfolio changes w.r.t Charges for Savings have Broken the accounting code");
        }
        ChargePaymentDTO chargePaymentDTO = chargePaymentDTOs.get(0);
        GLAccount chargeSpecificAccount = getLinkedGLAccountForSavingsCharges(savingsProductId, accountTypeToBeCredited.getValue(),
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

    public void createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(final Office office, final String currencyCode,
            final AccrualAccountsForSavings accountTypeToBeDebited, AccrualAccountsForSavings accountTypeToBeCredited,
            final Long savingsProductId, final Long paymentTypeId, final Long loanId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal totalAmount, final Boolean isReversal,
            final List<ChargePaymentDTO> chargePaymentDTOs) {
        // TODO Vishwas: Remove this validation, as and when appropriate Junit
        // tests are written for accounting
        /**
         * Accounting module currently supports a single charge per transaction, throw an error if this is not the case
         * here so any developers changing the expected portfolio behavior would also take care of modifying the
         * accounting code appropriately
         **/
        if (chargePaymentDTOs.size() != 1) {
            throw new PlatformDataIntegrityException("Recent Portfolio changes w.r.t Charges for Savings have Broken the accounting code",
                    "Recent Portfolio changes w.r.t Charges for Savings have Broken the accounting code");
        }
        ChargePaymentDTO chargePaymentDTO = chargePaymentDTOs.get(0);
        GLAccount chargeSpecificAccount = getLinkedGLAccountForSavingsCharges(savingsProductId, accountTypeToBeCredited.getValue(),
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

    public LoanTransaction getLoanTransactionById(final Long loanTransactionId) {
        return this.loanTransactionRepository.getReferenceById(loanTransactionId);
    }

    public SavingsAccountTransaction getSavingsTransactionById(final Long savingsTransactionId) {
        return this.savingsAccountTransactionRepository.getReferenceById(savingsTransactionId);
    }

    public ClientTransaction getClientTransactionById(final Long clientTransactionId) {
        return this.clientTransactionRepository.getReferenceById(clientTransactionId);
    }

    public Office getOfficeById(final long officeId) {
        return this.officeRepository.getReferenceById(officeId);
    }

    public void createCreditJournalEntryOrReversalForLoan(final Office office, final String currencyCode, final int accountMappingTypeId,
            final Long loanProductId, final Long paymentTypeId, final Long loanId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal) {
        final GLAccount account = getLinkedGLAccountForLoanProduct(loanProductId, accountMappingTypeId, paymentTypeId);
        createCreditJournalEntryOrReversalForLoan(office, currencyCode, loanId, transactionId, transactionDate, amount, isReversal,
                account);
    }

    public void createCreditJournalEntryOrReversalForLoan(final Office office, final String currencyCode, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversal,
            final GLAccount account) {
        if (isReversal) {
            createDebitJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        } else {
            createCreditJournalEntryForLoan(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        }
    }

    private void createCreditJournalEntryForClientPayments(final Office office, final String currencyCode, final GLAccount account,
            final Long clientId, final Long transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;

        String modifiedTransactionId = CLIENT_TRANSACTION_IDENTIFIER + transactionId;
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.CREDIT, amount, null, PortfolioProductType.CLIENT.getValue(), clientId, null, null, null,
                transactionId, null);
        persistJournalEntry(journalEntry);
    }

    private void createCreditJournalEntryForSavings(final Office office, final String currencyCode, final GLAccount account,
            final Long savingsId, final String transactionId, final LocalDate transactionDate, final BigDecimal amount)
            throws DataAccessException {
        final boolean manualEntry = false;
        Long savingsAccountTransactionId = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            savingsAccountTransactionId = Long.parseLong(transactionId);
            modifiedTransactionId = SAVINGS_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.CREDIT, amount, null, PortfolioProductType.SAVING.getValue(), savingsId, null, null,
                savingsAccountTransactionId, null, null);

        persistJournalEntry(journalEntry);
    }

    private void createCreditJournalEntryForLoan(final Office office, final String currencyCode, final GLAccount account, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        Long loanTransactionId = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            loanTransactionId = Long.parseLong(transactionId);
            modifiedTransactionId = LOAN_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.CREDIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId, null,
                loanTransactionId, null, null, null);
        persistJournalEntry(journalEntry);
    }

    public void createProvisioningDebitJournalEntry(LocalDate transactionDate, Long provisioningEntryId, Office office, String currencyCode,
            GLAccount account, BigDecimal amount) {
        final boolean manualEntry = false;
        String modifiedTransactionId = PROVISIONING_TRANSACTION_IDENTIFIER + provisioningEntryId;
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.DEBIT, amount, null, PortfolioProductType.PROVISIONING.getValue(), provisioningEntryId,
                null, null, null, null, null);
        persistJournalEntry(journalEntry);
    }

    public void createProvisioningCreditJournalEntry(LocalDate transactionDate, Long provisioningEntryId, Office office,
            String currencyCode, GLAccount account, BigDecimal amount) {
        final boolean manualEntry = false;
        String modifiedTransactionId = PROVISIONING_TRANSACTION_IDENTIFIER + provisioningEntryId;
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.CREDIT, amount, null, PortfolioProductType.PROVISIONING.getValue(), provisioningEntryId,
                null, null, null, null, null);
        persistJournalEntry(journalEntry);
    }

    private void createDebitJournalEntryForLoan(final Office office, final String currencyCode, final GLAccount account, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        Long loanTransactionId = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            loanTransactionId = Long.parseLong(transactionId);
            modifiedTransactionId = LOAN_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.DEBIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId, null,
                loanTransactionId, null, null, null);
        persistJournalEntry(journalEntry);
    }

    private void createDebitJournalEntryForSavings(final Office office, final String currencyCode, final GLAccount account,
            final Long savingsId, final String transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        Long savingsAccountTransactionId = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            savingsAccountTransactionId = Long.parseLong(transactionId);
            modifiedTransactionId = SAVINGS_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.DEBIT, amount, null, PortfolioProductType.SAVING.getValue(), savingsId, null, null,
                savingsAccountTransactionId, null, null);

        persistJournalEntry(journalEntry);
    }

    private void createDebitJournalEntryForClientPayments(final Office office, final String currencyCode, final GLAccount account,
            final Long clientId, final Long transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        String modifiedTransactionId = CLIENT_TRANSACTION_IDENTIFIER + transactionId;
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.DEBIT, amount, null, PortfolioProductType.CLIENT.getValue(), clientId, null, null, null,
                transactionId, null);
        persistJournalEntry(journalEntry);
    }

    public void createJournalEntriesForShares(final Office office, final String currencyCode, final int accountTypeToDebitId,
            final int accountTypeToCreditId, final Long shareProductId, final Long paymentTypeId, final Long shareAccountId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        createDebitJournalEntryForShares(office, currencyCode, accountTypeToDebitId, shareProductId, paymentTypeId, shareAccountId,
                transactionId, transactionDate, amount);
        createCreditJournalEntryForShares(office, currencyCode, accountTypeToCreditId, shareProductId, paymentTypeId, shareAccountId,
                transactionId, transactionDate, amount);
    }

    public void createDebitJournalEntryForShares(final Office office, final String currencyCode, final int accountTypeToDebitId,
            final Long shareProductId, final Long paymentTypeId, final Long shareAccountId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount) {
        final GLAccount debitAccount = getLinkedGLAccountForShareProduct(shareProductId, accountTypeToDebitId, paymentTypeId);
        createDebitJournalEntryForShares(office, currencyCode, debitAccount, shareAccountId, transactionId, transactionDate, amount);
    }

    public void createCreditJournalEntryForShares(final Office office, final String currencyCode, final int accountTypeToCreditId,
            final Long shareProductId, final Long paymentTypeId, final Long shareAccountId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal amount) {
        final GLAccount creditAccount = getLinkedGLAccountForShareProduct(shareProductId, accountTypeToCreditId, paymentTypeId);
        createCreditJournalEntryForShares(office, currencyCode, creditAccount, shareAccountId, transactionId, transactionDate, amount);
    }

    public void createCashBasedJournalEntriesForSharesCharges(final Office office, final String currencyCode,
            final CashAccountsForShares accountTypeToBeDebited, final CashAccountsForShares accountTypeToBeCredited,
            final Long shareProductId, final Long paymentTypeId, final Long shareAccountId, final String transactionId,
            final LocalDate transactionDate, final BigDecimal totalAmount, final List<ChargePaymentDTO> chargePaymentDTOs) {

        createDebitJournalEntryForShares(office, currencyCode, accountTypeToBeDebited.getValue(), shareProductId, paymentTypeId,
                shareAccountId, transactionId, transactionDate, totalAmount);
        createCashBasedJournalEntryForSharesCharges(office, currencyCode, accountTypeToBeCredited, shareProductId, shareAccountId,
                transactionId, transactionDate, totalAmount, chargePaymentDTOs);
    }

    public void createCashBasedJournalEntryForSharesCharges(final Office office, final String currencyCode,
            final CashAccountsForShares accountTypeToBeCredited, final Long shareProductId, final Long shareAccountId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal totalAmount,
            final List<ChargePaymentDTO> chargePaymentDTOs) {
        final Map<GLAccount, BigDecimal> creditDetailsMap = new LinkedHashMap<>();
        for (final ChargePaymentDTO chargePaymentDTO : chargePaymentDTOs) {
            final GLAccount chargeSpecificAccount = getLinkedGLAccountForShareCharges(shareProductId, accountTypeToBeCredited.getValue(),
                    chargePaymentDTO.getChargeId());
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
            createCreditJournalEntryForShares(office, currencyCode, account, shareAccountId, transactionId, transactionDate, amount);
        }
        if (totalAmount.compareTo(totalCreditedAmount) != 0) {
            throw new PlatformDataIntegrityException("Recent Portfolio changes w.r.t Charges for shares have Broken the accounting code",
                    "Recent Portfolio changes w.r.t Charges for shares have Broken the accounting code");
        }
    }

    public void revertCashBasedJournalEntryForSharesCharges(final Office office, final String currencyCode,
            final CashAccountsForShares accountTypeToBeCredited, final Long shareProductId, final Long shareAccountId,
            final String transactionId, final LocalDate transactionDate, final BigDecimal totalAmount,
            final List<ChargePaymentDTO> chargePaymentDTOs) {
        final Map<GLAccount, BigDecimal> creditDetailsMap = new LinkedHashMap<>();
        for (final ChargePaymentDTO chargePaymentDTO : chargePaymentDTOs) {
            final GLAccount chargeSpecificAccount = getLinkedGLAccountForShareCharges(shareProductId, accountTypeToBeCredited.getValue(),
                    chargePaymentDTO.getChargeId());
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
            createDebitJournalEntryForShares(office, currencyCode, account, shareAccountId, transactionId, transactionDate, amount);
        }
        if (totalAmount.compareTo(totalCreditedAmount) != 0) {
            throw new PlatformDataIntegrityException("Recent Portfolio changes w.r.t Charges for shares have Broken the accounting code",
                    "Recent Portfolio changes w.r.t Charges for shares have Broken the accounting code");
        }
    }

    private void createDebitJournalEntryForShares(final Office office, final String currencyCode, final GLAccount account,
            final Long shareAccountId, final String transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        Long shareTransactionId = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            shareTransactionId = Long.parseLong(transactionId);
            modifiedTransactionId = SHARE_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.DEBIT, amount, null, PortfolioProductType.SHARES.getValue(), shareAccountId, null, null,
                null, null, shareTransactionId);
        persistJournalEntry(journalEntry);
    }

    private void createCreditJournalEntryForShares(final Office office, final String currencyCode, final GLAccount account,
            final Long shareAccountId, final String transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        Long shareTransactionId = null;
        String modifiedTransactionId = transactionId;
        if (StringUtils.isNumeric(transactionId)) {
            shareTransactionId = Long.parseLong(transactionId);
            modifiedTransactionId = SHARE_TRANSACTION_IDENTIFIER + transactionId;
        }
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.CREDIT, amount, null, PortfolioProductType.SHARES.getValue(), shareAccountId, null, null,
                null, null, shareTransactionId);
        persistJournalEntry(journalEntry);
    }

    public GLAccount getLinkedGLAccountForLoanProduct(final Long loanProductId, final int accountMappingTypeId, final Long paymentTypeId) {
        GLAccount glAccount;
        if (isOrganizationAccount(accountMappingTypeId)) {
            FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                    .findByFinancialActivityTypeWithNotFoundDetection(accountMappingTypeId);
            glAccount = financialActivityAccount.getGlAccount();
        } else {
            ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(loanProductId,
                    PortfolioProductType.LOAN.getValue(), accountMappingTypeId);

            /****
             * Get more specific mapping for FUND source accounts (based on payment channels). Note that fund source
             * placeholder ID would be same for both cash and accrual accounts
             ***/
            if (accountMappingTypeId == CashAccountsForLoan.FUND_SOURCE.getValue()) {
                final ProductToGLAccountMapping paymentChannelSpecificAccountMapping = this.accountMappingRepository
                        .findByProductIdAndProductTypeAndFinancialAccountTypeAndPaymentTypeId(loanProductId,
                                PortfolioProductType.LOAN.getValue(), accountMappingTypeId, paymentTypeId);
                if (paymentChannelSpecificAccountMapping != null) {
                    accountMapping = paymentChannelSpecificAccountMapping;
                }
            }

            if (accountMapping == null) {
                throw new ProductToGLAccountMappingNotFoundException(PortfolioProductType.LOAN, loanProductId,
                        AccrualAccountsForLoan.fromInt(accountMappingTypeId).toString());

            }
            glAccount = accountMapping.getGlAccount();
        }
        return glAccount;
    }

    private GLAccount getLinkedGLAccountForLoanCharges(final Long loanProductId, final int accountMappingTypeId, final Long chargeId) {
        ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(loanProductId,
                PortfolioProductType.LOAN.getValue(), accountMappingTypeId);
        /*****
         * Get more specific mappings for Charges and penalties (based on the actual charge /penalty coupled with the
         * loan product). Note the income from fees and income from penalties placeholder ID would be the same for both
         * cash and accrual based accounts
         *****/

        // Vishwas TODO: remove this condition as it should always be true
        if (accountMappingTypeId == CashAccountsForLoan.INCOME_FROM_FEES.getValue()
                || accountMappingTypeId == CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue()) {
            final ProductToGLAccountMapping chargeSpecificIncomeAccountMapping = this.accountMappingRepository
                    .findProductIdAndProductTypeAndFinancialAccountTypeAndChargeId(loanProductId, PortfolioProductType.LOAN.getValue(),
                            accountMappingTypeId, chargeId);
            if (chargeSpecificIncomeAccountMapping != null) {
                accountMapping = chargeSpecificIncomeAccountMapping;
            }
        }
        return accountMapping.getGlAccount();
    }

    private GLAccount getLinkedGLAccountForSavingsCharges(final Long savingsProductId, final int accountMappingTypeId,
            final Long chargeId) {

        ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(savingsProductId,
                PortfolioProductType.SAVING.getValue(), accountMappingTypeId);
        /*****
         * Get more specific mappings for Charges and penalties (based on the actual charge /penalty coupled with the
         * loan product). Note the income from fees and income from penalties placeholder ID would be the same for both
         * cash and accrual based accounts
         *****/

        // Vishwas TODO: remove this condition as it should always be true

        if (accountMappingTypeId == CashAccountsForSavings.INCOME_FROM_FEES.getValue()
                || accountMappingTypeId == CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue()) {
            GLAccount glAccount = chargeRepositoryWrapper.findOneWithNotFoundDetection(chargeId).getAccount();
            if (glAccount != null) {
                return glAccount;
            }
            final ProductToGLAccountMapping chargeSpecificIncomeAccountMapping = this.accountMappingRepository
                    .findProductIdAndProductTypeAndFinancialAccountTypeAndChargeId(savingsProductId, PortfolioProductType.SAVING.getValue(),
                            accountMappingTypeId, chargeId);
            if (chargeSpecificIncomeAccountMapping != null) {

                accountMapping = chargeSpecificIncomeAccountMapping;
            }
        }

        return accountMapping.getGlAccount();
    }

    private GLAccount getLinkedGLAccountForSavingsProduct(final Long savingsProductId, final int accountMappingTypeId,
            final Long paymentTypeId) {
        GLAccount glAccount;
        if (isOrganizationAccount(accountMappingTypeId)) {
            FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                    .findByFinancialActivityTypeWithNotFoundDetection(accountMappingTypeId);
            glAccount = financialActivityAccount.getGlAccount();
        } else {
            ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(savingsProductId,
                    PortfolioProductType.SAVING.getValue(), accountMappingTypeId);
            /****
             * Get more specific mapping for FUND source accounts (based on payment channels). Note that fund source
             * placeholder ID would be same for both cash and accrual accounts
             ***/
            if (accountMappingTypeId == CashAccountsForSavings.SAVINGS_REFERENCE.getValue()) {
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

    private GLAccount getLinkedGLAccountForShareProduct(final Long shareProductId, final int accountMappingTypeId,
            final Long paymentTypeId) {
        GLAccount glAccount;
        if (isOrganizationAccount(accountMappingTypeId)) {
            FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                    .findByFinancialActivityTypeWithNotFoundDetection(accountMappingTypeId);
            glAccount = financialActivityAccount.getGlAccount();
        } else {
            ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(shareProductId,
                    PortfolioProductType.SHARES.getValue(), accountMappingTypeId);

            if (accountMappingTypeId == CashAccountsForShares.SHARES_REFERENCE.getValue()) {
                final ProductToGLAccountMapping paymentChannelSpecificAccountMapping = this.accountMappingRepository
                        .findByProductIdAndProductTypeAndFinancialAccountTypeAndPaymentTypeId(shareProductId,
                                PortfolioProductType.SHARES.getValue(), accountMappingTypeId, paymentTypeId);
                if (paymentChannelSpecificAccountMapping != null) {
                    accountMapping = paymentChannelSpecificAccountMapping;
                }
            }
            glAccount = accountMapping.getGlAccount();
        }
        return glAccount;
    }

    private GLAccount getLinkedGLAccountForShareCharges(final Long shareProductId, final int accountMappingTypeId, final Long chargeId) {
        ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(shareProductId,
                PortfolioProductType.SHARES.getValue(), accountMappingTypeId);
        /*****
         * Get more specific mappings for Charges and penalties (based on the actual charge /penalty coupled with the
         * loan product). Note the income from fees and income from penalties placeholder ID would be the same for both
         * cash and accrual based accounts
         *****/

        final ProductToGLAccountMapping chargeSpecificIncomeAccountMapping = this.accountMappingRepository
                .findProductIdAndProductTypeAndFinancialAccountTypeAndChargeId(shareProductId, PortfolioProductType.SHARES.getValue(),
                        accountMappingTypeId, chargeId);
        if (chargeSpecificIncomeAccountMapping != null) {
            accountMapping = chargeSpecificIncomeAccountMapping;
        }
        return accountMapping.getGlAccount();
    }

    private boolean isOrganizationAccount(final int accountMappingTypeId) {
        return FinancialActivity.fromInt(accountMappingTypeId) != null;
    }

    public BigDecimal createCreditJournalEntryOrReversalForClientPayments(final Office office, final String currencyCode,
            final Long clientId, final Long transactionId, final LocalDate transactionDate, final Boolean isReversal,
            final List<ClientChargePaymentDTO> clientChargePaymentDTOs) {
        /***
         * Map to track each account affected and the net credit to be made for a particular account
         ***/
        final Map<Long, BigDecimal> creditDetailsMap = new LinkedHashMap<>();
        for (final ClientChargePaymentDTO clientChargePaymentDTO : clientChargePaymentDTOs) {
            if (clientChargePaymentDTO.getIncomeAccountId() != null) {
                final Long accountId = clientChargePaymentDTO.getIncomeAccountId();
                BigDecimal chargeSpecificAmount = clientChargePaymentDTO.getAmount();

                // adjust net credit amount if the account is already present in
                // the map
                if (creditDetailsMap.containsKey(accountId)) {
                    final BigDecimal existingAmount = creditDetailsMap.get(accountId);
                    chargeSpecificAmount = chargeSpecificAmount.add(existingAmount);
                }
                creditDetailsMap.put(accountId, chargeSpecificAmount);
            }
        }

        BigDecimal totalCreditedAmount = BigDecimal.ZERO;
        for (final Map.Entry<Long, BigDecimal> entry : creditDetailsMap.entrySet()) {
            final GLAccount account = getGLAccountById(entry.getKey());
            final BigDecimal amount = entry.getValue();
            totalCreditedAmount = totalCreditedAmount.add(amount);
            if (isReversal) {
                createDebitJournalEntryForClientPayments(office, currencyCode, account, clientId, transactionId, transactionDate, amount);
            } else {
                createCreditJournalEntryForClientPayments(office, currencyCode, account, clientId, transactionId, transactionDate, amount);
            }
        }
        return totalCreditedAmount;
    }

    public void createDebitJournalEntryOrReversalForClientChargePayments(final Office office, final String currencyCode,
            final Long clientId, final Long transactionId, final LocalDate transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        final GLAccount account = financialActivityAccountRepository
                .findByFinancialActivityTypeWithNotFoundDetection(FinancialActivity.ASSET_FUND_SOURCE.getValue()).getGlAccount();
        if (isReversal) {
            createCreditJournalEntryForClientPayments(office, currencyCode, account, clientId, transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntryForClientPayments(office, currencyCode, account, clientId, transactionId, transactionDate, amount);
        }
    }

    private GLAccount getGLAccountById(final Long accountId) {
        return this.glAccountRepository.getReferenceById(accountId);
    }

    public Integer getValueForFeeOrPenaltyIncomeAccount(final String chargeRefundChargeType) {
        if (chargeRefundChargeType == null
                || !(chargeRefundChargeType.equalsIgnoreCase("P") || chargeRefundChargeType.equalsIgnoreCase("F"))) {
            String errorValue;
            errorValue = Objects.requireNonNullElse(chargeRefundChargeType, "Null");
            throw new PlatformDataIntegrityException("error.msg.chargeRefundChargeType.can.only.be.P.or.F",
                    "chargeRefundChargeType can only be P (Penalty) or F(Fee) - Value is: " + errorValue);
        }
        Integer incomeAccount;
        if (chargeRefundChargeType.equalsIgnoreCase("P")) {
            incomeAccount = AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue();
        } else {
            incomeAccount = AccrualAccountsForLoan.INCOME_FROM_FEES.getValue();

        }
        return incomeAccount;
    }

    public JournalEntry persistJournalEntry(JournalEntry journalEntry) {
        boolean isNew = journalEntry.isNew();
        JournalEntry savedJournalEntry = this.glJournalEntryRepository.saveAndFlush(journalEntry);
        if (isNew && journalEntry.getLoanTransactionId() != null) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanJournalEntryCreatedBusinessEvent(savedJournalEntry));
        }
        return savedJournalEntry;
    }

}
