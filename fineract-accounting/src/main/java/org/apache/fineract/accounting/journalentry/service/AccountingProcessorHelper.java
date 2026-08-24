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
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForShares;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.data.AccountingBridgeDataDTO;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.ClientChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.ClientTransactionDTO;
import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsDTO;
import org.apache.fineract.accounting.journalentry.data.SharesDTO;
import org.apache.fineract.accounting.journalentry.data.TaxPaymentDTO;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;

public interface AccountingProcessorHelper {

    String LOAN_TRANSACTION_IDENTIFIER = "L";
    String SAVINGS_TRANSACTION_IDENTIFIER = "S";
    String CLIENT_TRANSACTION_IDENTIFIER = "C";
    String PROVISIONING_TRANSACTION_IDENTIFIER = "P";
    String SHARE_TRANSACTION_IDENTIFIER = "SH";
    String WORKING_CAPITAL_LOAN_TRANSACTION_IDENTIFIER = "WC";

    LoanDTO populateLoanDtoFromDTO(AccountingBridgeDataDTO accountingBridgeData);

    ProductToGLAccountMapping getChargeOffMappingByCodeValue(Long loanProductId, PortfolioProductType productType, Long chargeOffReasonId);

    ProductToGLAccountMapping getWriteOffMappingByCodeValue(Long loanProductId, PortfolioProductType productType, Long writeOffReasonId);

    ProductToGLAccountMapping getClassificationMappingByCodeValue(Long loanProductId, PortfolioProductType productType,
            Long classificationId, String classificationType);

    SavingsDTO populateSavingsDtoFromMap(Map<String, Object> accountingBridgeData, boolean cashBasedAccountingEnabled,
            boolean accrualBasedAccountingEnabled);

    SharesDTO populateSharesDtoFromMap(Map<String, Object> accountingBridgeData, boolean cashBasedAccountingEnabled,
            boolean accrualBasedAccountingEnabled);

    ClientTransactionDTO populateClientTransactionDtoFromMap(Map<String, Object> accountingBridgeData);

    void createJournalEntriesForLoanCharges(Office office, String currencyCode, Integer accountTypeToBeDebited,
            Integer accountTypeToBeCredited, Long loanProductId, Long loanId, String transactionId, LocalDate transactionDate,
            BigDecimal totalAmount, List<ChargePaymentDTO> chargePaymentDTOs);

    void createCashBasedJournalEntriesAndReversalsForSavings(Office office, String currencyCode, Integer accountTypeToBeDebited,
            Integer accountTypeToBeCredited, Long savingsProductId, Long paymentTypeId, Long loanId, String transactionId,
            LocalDate transactionDate, BigDecimal amount, Boolean isReversal);

    void createJournalEntriesForLoan(Office office, String currencyCode, Integer accountTypeToBeDebited, Integer accountTypeToBeCredited,
            Long loanProductId, Long paymentTypeId, Long loanId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void createJournalEntriesForLoan(Office office, String currencyCode, Integer accountTypeToBeDebited, GLAccount accountToBeCredited,
            Long loanProductId, Long paymentTypeId, Long loanId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void createSplitJournalEntriesForLoan(Office office, String currencyCode, List<JournalAmountHolder> splitAccountsHolder,
            JournalAmountHolder totalAccountHolder, Long loanProductId, Long paymentTypeId, Long loanId, String transactionId,
            LocalDate transactionDate);

    void createCreditJournalEntryForLoan(Office office, String currencyCode, CashAccountsForLoan accountMappingType, Long loanProductId,
            Long paymentTypeId, Long loanId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void createCreditJournalEntryForLoan(Office office, String currencyCode, AccrualAccountsForLoan accountMappingType, Long loanProductId,
            Long paymentTypeId, Long loanId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void checkForBranchClosures(GLClosure latestGLClosure, LocalDate transactionDate);

    GLClosure getLatestClosureByBranch(long officeId);

    void createCashBasedJournalEntriesAndReversalsForSavingsTax(Office office, String currencyCode,
            CashAccountsForSavings accountTypeToBeDebited, CashAccountsForSavings accountTypeToBeCredited, Long savingsProductId,
            Long paymentTypeId, Long savingsId, String transactionId, LocalDate transactionDate, BigDecimal amount, Boolean isReversal,
            List<TaxPaymentDTO> taxDetails);

    void createAccrualBasedJournalEntriesAndReversalsForSavingsTax(Office office, String currencyCode,
            AccountingConstants.AccrualAccountsForSavings accountTypeToBeDebited,
            AccountingConstants.AccrualAccountsForSavings accountTypeToBeCredited, Long savingsProductId, Long paymentTypeId,
            Long savingsId, String transactionId, LocalDate transactionDate, BigDecimal amount, Boolean isReversal,
            List<TaxPaymentDTO> taxDetails);

    void createCashBasedDebitJournalEntriesAndReversalsForSavings(Office office, String currencyCode, Integer accountTypeToBeDebited,
            Long savingsProductId, Long paymentTypeId, Long savingsId, String transactionId, LocalDate transactionDate, BigDecimal amount,
            Boolean isReversal);

    void createCashBasedCreditJournalEntriesAndReversalsForSavings(Office office, String currencyCode, Integer accountTypeToBeCredited,
            Long savingsProductId, Long paymentTypeId, Long savingsId, String transactionId, LocalDate transactionDate, BigDecimal amount,
            Boolean isReversal);

    void createCashBasedCreditJournalEntriesAndReversalsForSavings(Office office, String currencyCode, Long creditAccountId, Long savingsId,
            String transactionId, LocalDate transactionDate, BigDecimal amount, Boolean isReversal);

    void createAccrualBasedDebitJournalEntriesAndReversalsForSavings(Office office, String currencyCode, Integer accountTypeToBeDebited,
            Long savingsProductId, Long paymentTypeId, Long savingsId, String transactionId, LocalDate transactionDate, BigDecimal amount,
            Boolean isReversal);

    void createAccrualBasedCreditJournalEntriesAndReversalsForSavings(Office office, String currencyCode, Integer accountTypeToBeCredited,
            Long savingsProductId, Long paymentTypeId, Long savingsId, String transactionId, LocalDate transactionDate, BigDecimal amount,
            Boolean isReversal);

    void createAccrualBasedBasedCreditJournalEntriesAndReversalsForSavings(Office office, String currencyCode, Long creditAccountId,
            Long savingsId, String transactionId, LocalDate transactionDate, BigDecimal amount, Boolean isReversal);

    void createDebitJournalEntryForLoan(Office office, String currencyCode, int accountMappingTypeId, Long loanProductId,
            Long paymentTypeId, Long loanId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void createDebitJournalEntryForLoan(Office office, String currencyCode, Long loanId, String transactionId, LocalDate transactionDate,
            BigDecimal amount, GLAccount account);

    void createDebitJournalEntryForLoanCharges(Office office, String currencyCode, int accountMappingTypeId, Long loanProductId,
            Long chargeId, Long loanId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void createCreditJournalEntryForLoanCharges(Office office, String currencyCode, int accountMappingTypeId, Long loanProductId,
            Long loanId, String transactionId, LocalDate transactionDate, BigDecimal totalAmount, List<ChargePaymentDTO> chargePaymentDTOs);

    void createDebitJournalEntryForLoanCharges(Office office, String currencyCode, int accountMappingTypeId, Long loanProductId,
            Long loanId, String transactionId, LocalDate transactionDate, BigDecimal totalAmount, List<ChargePaymentDTO> chargePaymentDTOs);

    void createCashBasedJournalEntriesAndReversalsForSavingsCharges(Office office, String currencyCode,
            CashAccountsForSavings accountTypeToBeDebited, CashAccountsForSavings accountTypeToBeCredited, Long savingsProductId,
            Long paymentTypeId, Long loanId, String transactionId, LocalDate transactionDate, BigDecimal totalAmount, Boolean isReversal,
            List<ChargePaymentDTO> chargePaymentDTOs);

    void createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(Office office, String currencyCode,
            AccountingConstants.AccrualAccountsForSavings accountTypeToBeDebited,
            AccountingConstants.AccrualAccountsForSavings accountTypeToBeCredited, Long savingsProductId, Long paymentTypeId, Long loanId,
            String transactionId, LocalDate transactionDate, BigDecimal totalAmount, Boolean isReversal,
            List<ChargePaymentDTO> chargePaymentDTOs);

    Office getOfficeById(long officeId);

    void createCreditJournalEntryForLoan(Office office, String currencyCode, int accountMappingTypeId, Long loanProductId,
            Long paymentTypeId, Long loanId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void createCreditJournalEntryForLoan(Office office, String currencyCode, Long loanId, String transactionId, LocalDate transactionDate,
            BigDecimal amount, GLAccount account);

    void createCreditJournalEntryForLoanByGLAccountId(Office office, String currencyCode, Long loanId, String transactionId,
            LocalDate transactionDate, BigDecimal amount, Long glAccountId);

    void createDebitJournalEntryForLoanByGLAccountId(Office office, String currencyCode, Long loanId, String transactionId,
            LocalDate transactionDate, BigDecimal amount, Long glAccountId);

    void createProvisioningDebitJournalEntry(LocalDate transactionDate, Long provisioningEntryId, Office office, String currencyCode,
            GLAccount account, BigDecimal amount);

    void createProvisioningCreditJournalEntry(LocalDate transactionDate, Long provisioningEntryId, Office office, String currencyCode,
            GLAccount account, BigDecimal amount);

    void createDebitJournalEntryForLoan(Office office, String currencyCode, GLAccount account, Long loanId, String transactionId,
            LocalDate transactionDate, BigDecimal amount);

    void createJournalEntriesForShares(Office office, String currencyCode, int accountTypeToDebitId, int accountTypeToCreditId,
            Long shareProductId, Long paymentTypeId, Long shareAccountId, String transactionId, LocalDate transactionDate,
            BigDecimal amount);

    void createDebitJournalEntryForShares(Office office, String currencyCode, int accountTypeToDebitId, Long shareProductId,
            Long paymentTypeId, Long shareAccountId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void createCreditJournalEntryForShares(Office office, String currencyCode, int accountTypeToCreditId, Long shareProductId,
            Long paymentTypeId, Long shareAccountId, String transactionId, LocalDate transactionDate, BigDecimal amount);

    void createCashBasedJournalEntriesForSharesCharges(Office office, String currencyCode, CashAccountsForShares accountTypeToBeDebited,
            CashAccountsForShares accountTypeToBeCredited, Long shareProductId, Long paymentTypeId, Long shareAccountId,
            String transactionId, LocalDate transactionDate, BigDecimal totalAmount, List<ChargePaymentDTO> chargePaymentDTOs);

    void createCashBasedJournalEntryForSharesCharges(Office office, String currencyCode, CashAccountsForShares accountTypeToBeCredited,
            Long shareProductId, Long shareAccountId, String transactionId, LocalDate transactionDate, BigDecimal totalAmount,
            List<ChargePaymentDTO> chargePaymentDTOs);

    void revertCashBasedJournalEntryForSharesCharges(Office office, String currencyCode, CashAccountsForShares accountTypeToBeCredited,
            Long shareProductId, Long shareAccountId, String transactionId, LocalDate transactionDate, BigDecimal totalAmount,
            List<ChargePaymentDTO> chargePaymentDTOs);

    GLAccount getLinkedGLAccountForLoanProduct(Long loanProductId, int accountMappingTypeId, Long paymentTypeId);

    BigDecimal createCreditJournalEntryOrReversalForClientPayments(Office office, String currencyCode, Long clientId, Long transactionId,
            LocalDate transactionDate, Boolean isReversal, List<ClientChargePaymentDTO> clientChargePaymentDTOs);

    void createDebitJournalEntryOrReversalForClientChargePayments(Office office, String currencyCode, Long clientId, Long transactionId,
            LocalDate transactionDate, BigDecimal amount, Boolean isReversal);

    Integer getValueForFeeOrPenaltyIncomeAccount(String chargeRefundChargeType);

    GLAccount getLinkedGLAccountForWorkingCapitalLoanProduct(Long workingCapitalLoanProductId, int accountMappingTypeId,
            Long paymentTypeId);

    void createCreditJournalEntryForWorkingCapitalLoan(Office office, String currencyCode, GLAccount account, Long workingCapitalLoanId,
            Long workingCapitalLoanTransactionId, LocalDate transactionDate, BigDecimal amount, PaymentDetail paymentDetail);

    void createDebitJournalEntryForWorkingCapitalLoan(Office office, String currencyCode, GLAccount account, Long workingCapitalLoanId,
            Long workingCapitalLoanTransactionId, LocalDate transactionDate, BigDecimal amount, PaymentDetail paymentDetail);

    JournalEntry persistJournalEntry(JournalEntry journalEntry);
}
