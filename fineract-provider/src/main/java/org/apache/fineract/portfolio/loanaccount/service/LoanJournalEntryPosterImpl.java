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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.data.AccountingBridgeDataDTO;
import org.apache.fineract.accounting.journalentry.data.AccountingBridgeLoanTransactionDTO;
import org.apache.fineract.accounting.journalentry.data.AdvancedMappingtDTO;
import org.apache.fineract.accounting.journalentry.data.ChargeTaxDetailDTO;
import org.apache.fineract.accounting.journalentry.data.LoanChargeDTO;
import org.apache.fineract.accounting.journalentry.data.LoanChargePaidByDTO;
import org.apache.fineract.accounting.journalentry.service.ExternalOwnerJournalEntryWriteService;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.AmortizationType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAmortizationAllocationMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAmortizationAllocationMappingRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeTaxDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanJournalEntryPosterImpl implements LoanJournalEntryPoster {

    /**
     * Transaction types whose charge tax can be posted, in two streams that are rounded separately: the types the
     * accounting processors dispatch as repayments, and accruals. No other type posts charge tax.
     */
    static final Set<LoanTransactionType> PAYMENT_TAX_STREAM = EnumSet.of(LoanTransactionType.REPAYMENT,
            LoanTransactionType.MERCHANT_ISSUED_REFUND, LoanTransactionType.PAYOUT_REFUND, LoanTransactionType.GOODWILL_CREDIT,
            LoanTransactionType.CHARGE_REFUND, LoanTransactionType.DOWN_PAYMENT, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT,
            LoanTransactionType.CHARGE_PAYMENT);
    static final Set<LoanTransactionType> ACCRUAL_TAX_STREAM = EnumSet.of(LoanTransactionType.ACCRUAL,
            LoanTransactionType.ACCRUAL_ADJUSTMENT);

    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final ExternalOwnerJournalEntryWriteService externalOwnerJournalEntryWriteService;
    private final LoanAmortizationAllocationMappingRepository loanAmortizationAllocationMappingRepository;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public void postJournalEntriesForLoanTransaction(final LoanTransaction loanTransaction, final boolean isAccountTransfer,
            final boolean isLoanToLoanTransfer) {
        AccountingBridgeDataDTO accountingBridgeDataDTO = createAccountingBridgeDataForSingleTransaction(loanTransaction,
                isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoanTransaction(accountingBridgeDataDTO, isLoanToLoanTransfer);
    }

    @Override
    public void postJournalEntriesForExternalOwnerTransfer(final Loan loan, final Object externalAssetOwnerTransfer,
            final Object previousOwner) {
        // Cast to proper types
        final ExternalAssetOwnerTransfer transfer = (ExternalAssetOwnerTransfer) externalAssetOwnerTransfer;
        final ExternalAssetOwner prevOwner = (ExternalAssetOwner) previousOwner;
        this.externalOwnerJournalEntryWriteService.createJournalEntriesForExternalOwnerTransfer(loan, transfer, prevOwner);
    }

    /**
     * Create AccountingBridgeDataDTO for a single loan transaction This converts a single LoanTransaction to the format
     * expected by existing journal entry logic
     */
    private AccountingBridgeDataDTO createAccountingBridgeDataForSingleTransaction(final LoanTransaction loanTransaction,
            final boolean isAccountTransfer) {
        final Loan loan = loanTransaction.getLoan();
        final String currencyCode = loan.getCurrencyCode();

        final AccountingBridgeLoanTransactionDTO transactionDTO = convertToAccountingBridgeTransaction(loanTransaction);

        final List<AccountingBridgeLoanTransactionDTO> transactions = new ArrayList<>();
        transactions.add(transactionDTO);

        boolean wasChargedOffAtTransactionTime = loan.isChargedOff();
        if (loan.isChargedOff() && loan.getChargedOffOnDate() != null) {
            // If transaction date is before charge-off date, treat as non-charged-off
            if (loanTransaction.getTransactionDate().isBefore(loan.getChargedOffOnDate())) {
                wasChargedOffAtTransactionTime = false;
            }
        }

        List<AdvancedMappingtDTO> buydownFeeAdvancedMappingData = null;
        List<AdvancedMappingtDTO> capitalizedIncomeAdvancedMappingData = null;
        if (loanTransaction.isBuyDownFeeAmortization()) {
            buydownFeeAdvancedMappingData = getLoanTransactionClassificationId(loanTransaction);
        } else if (loanTransaction.isCapitalizedIncomeAmortization()) {
            capitalizedIncomeAdvancedMappingData = getLoanTransactionClassificationId(loanTransaction);
        }
        AdvancedMappingtDTO writeOffReasonAdvancedMappingData = null;
        if (loan.isClosedWrittenOff() && loan.getWriteOffReason() != null) {
            writeOffReasonAdvancedMappingData = new AdvancedMappingtDTO(loan.getWriteOffReason().getId(), BigDecimal.ZERO);
        }

        return new AccountingBridgeDataDTO(loan.getId(), loan.productId(), loan.getOfficeId(), currencyCode,
                loan.getSummary().getTotalInterestCharged(), loan.isCashBasedAccountingEnabledOnLoanProduct(),
                loan.isUpfrontAccrualAccountingEnabledOnLoanProduct(), loan.isPeriodicAccrualAccountingEnabledOnLoanProduct(),
                isAccountTransfer, wasChargedOffAtTransactionTime, loan.isFraud(), loan.fetchChargeOffReasonId(), loan.isClosedWrittenOff(),
                transactions, loan.getLoanProductRelatedDetail().isMerchantBuyDownFee(), buydownFeeAdvancedMappingData,
                capitalizedIncomeAdvancedMappingData, writeOffReasonAdvancedMappingData);
    }

    private List<AdvancedMappingtDTO> getLoanTransactionClassificationId(final LoanTransaction loanTransaction) {
        final List<AdvancedMappingtDTO> advancedMappingData = new ArrayList<AdvancedMappingtDTO>();
        if (loanTransaction.isCapitalizedIncomeAmortization() || loanTransaction.isBuyDownFeeAmortization()) {
            final List<LoanAmortizationAllocationMapping> loanTransactionAllocations = loanAmortizationAllocationMappingRepository
                    .fetchLoanTransactionAllocationByAmortizationLoanTransactionId(loanTransaction.getId(),
                            loanTransaction.getLoan().getId());
            loanTransactionAllocations.forEach(loanTransactionAllocation -> {
                final CodeValue classification = loanTransactionRepository
                        .fetchClassificationCodeValueByTransactionId(loanTransactionAllocation.getBaseLoanTransactionId());
                final BigDecimal allocationAmount = loanTransactionAllocation.getAmortizationType().equals(AmortizationType.AM)
                        ? loanTransactionAllocation.getAmount()
                        : loanTransactionAllocation.getAmount().negate();
                if (classification != null) {
                    advancedMappingData.add(new AdvancedMappingtDTO(classification.getId(), allocationAmount));
                } else {
                    advancedMappingData.add(new AdvancedMappingtDTO(null, allocationAmount));
                }
            });
        }
        return advancedMappingData;
    }

    /**
     * Convert LoanTransaction to AccountingBridgeLoanTransactionDTO
     */
    private AccountingBridgeLoanTransactionDTO convertToAccountingBridgeTransaction(LoanTransaction loanTransaction) {
        final MonetaryCurrency currency = loanTransaction.getLoan().getCurrency();
        final AccountingBridgeLoanTransactionDTO transactionDTO = new AccountingBridgeLoanTransactionDTO();

        transactionDTO.setId(loanTransaction.getId());
        transactionDTO.setOfficeId(loanTransaction.getOffice().getId());
        transactionDTO.setType(LoanEnumerations.transactionType(loanTransaction.getTypeOf()));
        transactionDTO.setReversed(loanTransaction.isReversed());
        transactionDTO.setDate(loanTransaction.getTransactionDate());
        transactionDTO.setCurrencyCode(currency.getCode());
        transactionDTO.setAmount(loanTransaction.getAmount());
        transactionDTO.setNetDisbursalAmount(loanTransaction.getLoan().getNetDisbursalAmount());

        // Handle principalPortion for chargeback
        if (transactionDTO.getType().isChargeback() && (loanTransaction.getLoan().getCreditAllocationRules() == null
                || loanTransaction.getLoan().getCreditAllocationRules().isEmpty())) {
            transactionDTO.setPrincipalPortion(loanTransaction.getAmount());
        } else {
            transactionDTO.setPrincipalPortion(loanTransaction.getPrincipalPortion());
        }

        transactionDTO.setInterestPortion(loanTransaction.getInterestPortion());
        transactionDTO.setFeeChargesPortion(loanTransaction.getFeeChargesPortion());
        transactionDTO.setPenaltyChargesPortion(loanTransaction.getPenaltyChargesPortion());
        transactionDTO.setOverPaymentPortion(loanTransaction.getOverPaymentPortion());

        // Handle ChargeRefund transactions
        if (transactionDTO.getType().isChargeRefund()) {
            transactionDTO.setChargeRefundChargeType(loanTransaction.getChargeRefundChargeType());
        }

        if (loanTransaction.getPaymentDetail() != null) {
            transactionDTO.setPaymentTypeId(loanTransaction.getPaymentDetail().getPaymentType().getId());
        }

        // Populate loanChargesPaid from the transaction
        if (!loanTransaction.getLoanChargesPaid().isEmpty()) {
            List<LoanChargePaidByDTO> loanChargesPaidData = new ArrayList<>();
            final Map<LoanChargePaidBy, List<ChargeTaxDetailDTO>> chargeTaxes = prorateChargeTaxes(loanTransaction);
            for (final LoanChargePaidBy chargePaidBy : loanTransaction.getLoanChargesPaid()) {
                final LoanCharge lc = chargePaidBy.getLoanCharge();
                final LoanChargePaidByDTO loanChargePaidData = new LoanChargePaidByDTO();
                loanChargePaidData.setChargeId(lc.getCharge().getId());
                loanChargePaidData.setIsPenalty(lc.isPenaltyCharge());
                loanChargePaidData.setLoanChargeId(lc.getId());
                loanChargePaidData.setAmount(chargePaidBy.getAmount());
                loanChargePaidData.setInstallmentNumber(chargePaidBy.getInstallmentNumber());

                final List<ChargeTaxDetailDTO> taxDetails = chargeTaxes.get(chargePaidBy);
                if (taxDetails != null) {
                    loanChargePaidData.setTaxDetails(taxDetails);
                }

                loanChargesPaidData.add(loanChargePaidData);
            }
            transactionDTO.setLoanChargesPaid(loanChargesPaidData);
        }

        // Handle chargeback principalPaid/feePaid/penaltyPaid
        if (transactionDTO.getType().isChargeback() && loanTransaction.getOverPaymentPortion() != null
                && loanTransaction.getOverPaymentPortion().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal principalPaid = loanTransaction.getOverPaymentPortion();
            BigDecimal feePaid = BigDecimal.ZERO;
            BigDecimal penaltyPaid = BigDecimal.ZERO;
            if (!loanTransaction.getLoanTransactionToRepaymentScheduleMappings().isEmpty()) {
                principalPaid = loanTransaction.getLoanTransactionToRepaymentScheduleMappings().stream()
                        .map(mapping -> Optional.ofNullable(mapping.getPrincipalPortion()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                feePaid = loanTransaction.getLoanTransactionToRepaymentScheduleMappings().stream()
                        .map(mapping -> Optional.ofNullable(mapping.getFeeChargesPortion()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                penaltyPaid = loanTransaction.getLoanTransactionToRepaymentScheduleMappings().stream()
                        .map(mapping -> Optional.ofNullable(mapping.getPenaltyChargesPortion()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            transactionDTO.setPrincipalPaid(principalPaid);
            transactionDTO.setFeePaid(feePaid);
            transactionDTO.setPenaltyPaid(penaltyPaid);
        }

        // Populate loanChargeDTO for CHARGE_ADJUSTMENT transactions
        LoanTransactionRelation loanTransactionRelation = loanTransaction.getLoanTransactionRelations().stream()
                .filter(e -> LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT.equals(e.getRelationType())).findAny().orElse(null);
        if (loanTransactionRelation != null) {
            LoanCharge loanCharge = loanTransactionRelation.getToCharge();
            transactionDTO.setLoanChargeDTO(new LoanChargeDTO(loanCharge.getCharge().getId(), loanCharge.isPenaltyCharge()));
        }

        // Set loanToLoanTransfer
        transactionDTO.setLoanToLoanTransfer(false);

        return transactionDTO;
    }

    /**
     * Pro-rates each tax component of the taxed charges this transaction pays, rounded to the currency's scale with the
     * configured rounding mode. The tax is rounded cumulatively over the charge's earlier transactions of the same
     * stream, and this transaction posts the difference, so that the lines of each tax component sum to that
     * component's tax rounded as a whole.
     * <p>
     * "Earlier" means a lower id, read by a query: the transaction must be saved, and the earlier ones flushed, before
     * its journal entries are posted.
     */
    private Map<LoanChargePaidBy, List<ChargeTaxDetailDTO>> prorateChargeTaxes(final LoanTransaction loanTransaction) {
        final Map<LoanChargePaidBy, List<ChargeTaxDetailDTO>> taxesByChargePaidBy = new HashMap<>();
        final Set<LoanTransactionType> stream = taxStreamOf(loanTransaction);
        if (stream.isEmpty()) {
            // no processor posts this transaction's tax, or it is reversed and its entries are mirrored
            return taxesByChargePaidBy;
        }
        final Map<LoanCharge, List<LoanChargePaidBy>> taxedRowsByCharge = new LinkedHashMap<>();
        for (final LoanChargePaidBy chargePaidBy : loanTransaction.getLoanChargesPaid()) {
            final LoanCharge loanCharge = chargePaidBy.getLoanCharge();
            if (MathUtil.isGreaterThanZero(loanCharge.getAmount()) && !loanCharge.getTaxDetails().isEmpty()) {
                taxedRowsByCharge.computeIfAbsent(loanCharge, k -> new ArrayList<>()).add(chargePaidBy);
            }
        }
        if (taxedRowsByCharge.isEmpty()) {
            return taxesByChargePaidBy;
        }
        Objects.requireNonNull(loanTransaction.getId(), "the transaction must be saved before its charge tax is prorated");
        final int scale = loanTransaction.getLoan().getCurrency().getDigitsAfterDecimal();
        final RoundingMode roundingMode = MoneyHelper.getRoundingMode();
        final boolean accrualAdjustment = loanTransaction.getTypeOf().isAccrualAdjustment();
        for (final Map.Entry<LoanCharge, List<LoanChargePaidBy>> entry : taxedRowsByCharge.entrySet()) {
            final LoanCharge loanCharge = entry.getKey();
            BigDecimal paidBefore = loanTransactionRepository.findChargeAmountPaidByEarlierTransactions(loanCharge, loanTransaction.getId(),
                    stream);
            final List<LoanChargePaidBy> rows = new ArrayList<>(entry.getValue());
            rows.sort(Comparator.comparing(LoanChargePaidBy::getInstallmentNumber, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(LoanChargePaidBy::getId, Comparator.nullsLast(Comparator.naturalOrder())));
            for (final LoanChargePaidBy chargePaidBy : rows) {
                final BigDecimal paid = chargePaidBy.getAmount();
                if (!MathUtil.isGreaterThanZero(paid)) {
                    taxesByChargePaidBy.put(chargePaidBy, new ArrayList<>());
                    continue;
                }
                // an accrual adjustment takes accrued charge back, so it moves the running total down; either way the
                // line is the tax between the two totals
                final BigDecimal paidAfter = accrualAdjustment ? paidBefore.subtract(paid) : paidBefore.add(paid);
                taxesByChargePaidBy.put(chargePaidBy,
                        taxLines(loanCharge, paidBefore.min(paidAfter), paidBefore.max(paidAfter), scale, roundingMode));
                paidBefore = paidAfter;
            }
        }
        return taxesByChargePaidBy;
    }

    /**
     * The tax lines for the part of the charge between {@code paidFrom} and {@code paidTo}: per component, its tax on
     * {@code paidTo} less its tax on {@code paidFrom}, each rounded. A component whose difference is zero gets no line.
     */
    private static List<ChargeTaxDetailDTO> taxLines(final LoanCharge loanCharge, final BigDecimal paidFrom, final BigDecimal paidTo,
            final int scale, final RoundingMode roundingMode) {
        final List<ChargeTaxDetailDTO> taxLines = new ArrayList<>();
        final BigDecimal chargeAmount = loanCharge.getAmount();
        for (final LoanChargeTaxDetails taxDetail : loanCharge.getTaxDetails()) {
            if (taxDetail.getTaxComponent().getCreditAccount() == null) {
                continue;
            }
            final BigDecimal tax = taxDetail.getAmount();
            final BigDecimal line = tax.multiply(paidTo).divide(chargeAmount, scale, roundingMode)
                    .subtract(tax.multiply(paidFrom).divide(chargeAmount, scale, roundingMode));
            if (line.signum() != 0) {
                taxLines.add(new ChargeTaxDetailDTO(taxDetail.getTaxComponent().getCreditAccount().getId(), line));
            }
        }
        return taxLines;
    }

    private static Set<LoanTransactionType> taxStreamOf(final LoanTransaction loanTransaction) {
        if (loanTransaction.isReversed()) {
            return Set.of();
        }
        if (PAYMENT_TAX_STREAM.contains(loanTransaction.getTypeOf())) {
            return PAYMENT_TAX_STREAM;
        }
        if (ACCRUAL_TAX_STREAM.contains(loanTransaction.getTypeOf())) {
            return ACCRUAL_TAX_STREAM;
        }
        return Set.of();
    }
}
