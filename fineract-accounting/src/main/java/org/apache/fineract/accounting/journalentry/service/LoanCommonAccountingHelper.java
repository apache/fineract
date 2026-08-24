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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.ChargeTaxPaymentDTO;
import org.apache.fineract.accounting.journalentry.data.LoanTransactionDTO;
import org.apache.fineract.organisation.office.domain.Office;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanCommonAccountingHelper {

    private final AccountingProcessorHelper helper;

    public List<ChargeTaxPaymentDTO> filterTaxPayments(final LoanTransactionDTO txn, final boolean penalty) {
        return txn.getChargeTaxPayments().stream().filter(t -> t.isPenalty() == penalty).collect(Collectors.toList());
    }

    public BigDecimal sumTaxAmounts(final List<ChargeTaxPaymentDTO> taxPayments) {
        return taxPayments.stream().map(ChargeTaxPaymentDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<ChargePaymentDTO> computeNetChargePayments(final List<ChargePaymentDTO> chargePayments,
            final List<ChargeTaxPaymentDTO> taxPayments) {
        final Map<Long, BigDecimal> taxByChargeId = taxPayments.stream().collect(Collectors.groupingBy(ChargeTaxPaymentDTO::getLoanChargeId,
                Collectors.reducing(BigDecimal.ZERO, ChargeTaxPaymentDTO::getAmount, BigDecimal::add)));
        final List<ChargePaymentDTO> result = new ArrayList<>();
        for (ChargePaymentDTO cp : chargePayments) {
            final BigDecimal tax = taxByChargeId.getOrDefault(cp.getLoanChargeId(), BigDecimal.ZERO);
            result.add(new ChargePaymentDTO(cp.getChargeId(), cp.getAmount().subtract(tax), cp.getLoanChargeId()));
        }
        return result;
    }

    public void createTaxLiabilityCreditEntries(final Office office, final String currencyCode, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final List<ChargeTaxPaymentDTO> taxPayments) {
        final Map<Long, BigDecimal> taxByAccount = taxPayments.stream()
                .collect(Collectors.groupingBy(ChargeTaxPaymentDTO::getCreditAccountId,
                        Collectors.reducing(BigDecimal.ZERO, ChargeTaxPaymentDTO::getAmount, BigDecimal::add)));
        for (Map.Entry<Long, BigDecimal> entry : taxByAccount.entrySet()) {
            this.helper.createCreditJournalEntryForLoanByGLAccountId(office, currencyCode, loanId, transactionId, transactionDate,
                    entry.getValue(), entry.getKey());
        }
    }

    public void createTaxLiabilityDebitEntries(final Office office, final String currencyCode, final Long loanId,
            final String transactionId, final LocalDate transactionDate, final List<ChargeTaxPaymentDTO> taxPayments) {
        final Map<Long, BigDecimal> taxByAccount = taxPayments.stream()
                .collect(Collectors.groupingBy(ChargeTaxPaymentDTO::getCreditAccountId,
                        Collectors.reducing(BigDecimal.ZERO, ChargeTaxPaymentDTO::getAmount, BigDecimal::add)));
        for (Map.Entry<Long, BigDecimal> entry : taxByAccount.entrySet()) {
            this.helper.createDebitJournalEntryForLoanByGLAccountId(office, currencyCode, loanId, transactionId, transactionDate,
                    entry.getValue(), entry.getKey());
        }
    }

    public void createAccrualChargeJournalEntriesWithTax(final Office office, final String currencyCode, final Long loanProductId,
            final Long loanId, final String transactionId, final LocalDate transactionDate, final BigDecimal grossAmount,
            final List<ChargePaymentDTO> chargePayments, final List<ChargeTaxPaymentDTO> taxPayments, final int receivableAccountType,
            final int incomeAccountType, final boolean isAccrualAdjustment) {
        final BigDecimal netAmount = grossAmount.subtract(sumTaxAmounts(taxPayments));
        final List<ChargePaymentDTO> netPayments = computeNetChargePayments(chargePayments, taxPayments);
        if (isAccrualAdjustment) {
            this.helper.createCreditJournalEntryForLoanCharges(office, currencyCode, receivableAccountType, loanProductId, loanId,
                    transactionId, transactionDate, grossAmount, chargePayments);
            this.helper.createDebitJournalEntryForLoanCharges(office, currencyCode, incomeAccountType, loanProductId, loanId, transactionId,
                    transactionDate, netAmount, netPayments);
            createTaxLiabilityDebitEntries(office, currencyCode, loanId, transactionId, transactionDate, taxPayments);
        } else {
            this.helper.createDebitJournalEntryForLoanCharges(office, currencyCode, receivableAccountType, loanProductId, loanId,
                    transactionId, transactionDate, grossAmount, chargePayments);
            this.helper.createCreditJournalEntryForLoanCharges(office, currencyCode, incomeAccountType, loanProductId, loanId,
                    transactionId, transactionDate, netAmount, netPayments);
            createTaxLiabilityCreditEntries(office, currencyCode, loanId, transactionId, transactionDate, taxPayments);
        }
    }

    public void populateDebitAccountEntry(final Long loanProductId, final BigDecimal transactionPartAmount, final Integer debitAccountType,
            final Map<Integer, BigDecimal> accountMapForDebit, final Long paymentTypeId) {
        final Integer accountDebit = returnExistingDebitAccountInMapMatchingGLAccount(loanProductId, paymentTypeId, debitAccountType,
                accountMapForDebit);
        if (accountMapForDebit.containsKey(accountDebit)) {
            accountMapForDebit.put(accountDebit, accountMapForDebit.get(accountDebit).add(transactionPartAmount));
        } else {
            accountMapForDebit.put(accountDebit, transactionPartAmount);
        }
    }

    public Integer returnExistingDebitAccountInMapMatchingGLAccount(final Long loanProductId, final Long paymentTypeId,
            final Integer accountType, final Map<Integer, BigDecimal> accountMap) {
        final GLAccount glAccount = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, accountType, paymentTypeId);
        return accountMap.entrySet().stream().filter(account -> this.helper
                .getLinkedGLAccountForLoanProduct(loanProductId, account.getKey(), paymentTypeId).getGlCode().equals(glAccount.getGlCode()))
                .map(Map.Entry::getKey).findFirst().orElse(accountType);
    }
}
