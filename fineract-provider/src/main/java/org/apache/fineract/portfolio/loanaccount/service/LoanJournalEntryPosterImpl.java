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
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanJournalEntryPosterImpl implements LoanJournalEntryPoster {

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
            final MathContext mc = MoneyHelper.getMathContext();
            for (final LoanChargePaidBy chargePaidBy : loanTransaction.getLoanChargesPaid()) {
                final LoanCharge lc = chargePaidBy.getLoanCharge();
                final LoanChargePaidByDTO loanChargePaidData = new LoanChargePaidByDTO();
                loanChargePaidData.setChargeId(lc.getCharge().getId());
                loanChargePaidData.setIsPenalty(lc.isPenaltyCharge());
                loanChargePaidData.setLoanChargeId(lc.getId());
                loanChargePaidData.setAmount(chargePaidBy.getAmount());
                loanChargePaidData.setInstallmentNumber(chargePaidBy.getInstallmentNumber());

                // Pro-rate each TaxComponent's tax proportionally to the paid amount
                final BigDecimal chargeAmount = lc.getAmount();
                final BigDecimal paidAmount = chargePaidBy.getAmount();
                if (chargeAmount != null && chargeAmount.compareTo(BigDecimal.ZERO) > 0 && !lc.getTaxDetails().isEmpty()) {
                    final List<ChargeTaxDetailDTO> taxDetails = new ArrayList<>();
                    for (LoanChargeTaxDetails taxDetail : lc.getTaxDetails()) {
                        if (taxDetail.getTaxComponent().getCreditAccount() != null) {
                            final BigDecimal proRatedTax = taxDetail.getAmount().multiply(paidAmount, mc).divide(chargeAmount, mc);
                            taxDetails.add(new ChargeTaxDetailDTO(taxDetail.getTaxComponent().getCreditAccount().getId(), proRatedTax));
                        }
                    }
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
}
