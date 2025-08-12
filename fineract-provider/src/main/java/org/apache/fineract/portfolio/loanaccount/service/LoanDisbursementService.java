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

import static org.apache.fineract.portfolio.loanaccount.domain.Loan.RECALCULATE_LOAN_SCHEDULE;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheDisbursementCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDisbursementValidator;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;

@RequiredArgsConstructor
public class LoanDisbursementService {

    private final LoanChargeValidator loanChargeValidator;
    private final LoanDisbursementValidator loanDisbursementValidator;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanChargeService loanChargeService;
    private final LoanBalanceService loanBalanceService;
    private final LoanJournalEntryPoster loanJournalEntryPoster;
    private final LoanTransactionRepository loanTransactionRepository;

    public void updateDisbursementDetails(final Loan loan, final JsonCommand jsonCommand, final Map<String, Object> actualChanges) {
        final List<Long> disbursementList = loan.fetchDisbursementIds();
        final List<Long> loanChargeIds = loan.fetchLoanTrancheChargeIds();
        final int chargeIdLength = loanChargeIds.size();
        String chargeIds;
        // From modify application page, if user removes all charges, we should
        // get empty array.
        // So we need to remove all charges applied for this loan
        final boolean removeAllCharges = jsonCommand.parameterExists(LoanApiConstants.chargesParameterName)
                && jsonCommand.arrayOfParameterNamed(LoanApiConstants.chargesParameterName).isEmpty();

        if (jsonCommand.parameterExists(LoanApiConstants.disbursementDataParameterName)) {
            final JsonArray disbursementDataArray = jsonCommand.arrayOfParameterNamed(LoanApiConstants.disbursementDataParameterName);
            if (disbursementDataArray != null && !disbursementDataArray.isEmpty()) {
                String dateFormat;
                Locale locale = null;
                final Map<String, String> dateAndLocale = getDateFormatAndLocale(jsonCommand);
                dateFormat = dateAndLocale.get(LoanApiConstants.dateFormatParameterName);
                if (dateAndLocale.containsKey(LoanApiConstants.localeParameterName)) {
                    locale = JsonParserHelper.localeFromString(dateAndLocale.get(LoanApiConstants.localeParameterName));
                }
                for (JsonElement jsonElement : disbursementDataArray) {
                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    final Map<String, Object> parsedDisbursementData = parseDisbursementDetails(jsonObject, dateFormat, locale);
                    final LocalDate expectedDisbursementDate = (LocalDate) parsedDisbursementData
                            .get(LoanApiConstants.expectedDisbursementDateParameterName);
                    final BigDecimal principal = (BigDecimal) parsedDisbursementData
                            .get(LoanApiConstants.disbursementPrincipalParameterName);
                    final Long disbursementID = (Long) parsedDisbursementData.get(LoanApiConstants.disbursementIdParameterName);
                    chargeIds = (String) parsedDisbursementData.get(LoanApiConstants.loanChargeIdParameterName);
                    if (chargeIds != null) {
                        if (chargeIds.contains(",")) {
                            final Iterable<String> chargeId = Splitter.on(',').split(chargeIds);
                            for (String loanChargeId : chargeId) {
                                loanChargeIds.remove(Long.parseLong(loanChargeId));
                            }
                        } else {
                            loanChargeIds.remove(Long.parseLong(chargeIds));
                        }
                    }
                    createOrUpdateDisbursementDetails(loan, disbursementID, actualChanges, expectedDisbursementDate, principal,
                            disbursementList);
                }
                removeDisbursementAndAssociatedCharges(loan, actualChanges, disbursementList, loanChargeIds, chargeIdLength,
                        removeAllCharges);
            }
        }
    }

    public Money adjustDisburseAmount(final Loan loan, @NotNull final JsonCommand command,
            @NotNull final LocalDate actualDisbursementDate) {
        Money disburseAmount = loan.getLoanRepaymentScheduleDetail().getPrincipal().zero();
        final BigDecimal principalDisbursed = command.bigDecimalValueOfParameterNamed(LoanApiConstants.principalDisbursedParameterName);
        if (loan.getActualDisbursementDate() == null || DateUtils.isBefore(actualDisbursementDate, loan.getActualDisbursementDate())) {
            loan.setActualDisbursementDate(actualDisbursementDate);
        }
        BigDecimal diff = BigDecimal.ZERO;
        final Collection<LoanDisbursementDetails> details = loan.fetchUndisbursedDetail();
        if (principalDisbursed == null) {
            disburseAmount = loan.getLoanRepaymentScheduleDetail().getPrincipal();
            if (!details.isEmpty()) {
                disburseAmount = disburseAmount.zero();
                for (LoanDisbursementDetails disbursementDetails : details) {
                    disbursementDetails.updateActualDisbursementDate(actualDisbursementDate);
                    disburseAmount = disburseAmount.plus(disbursementDetails.principal());
                }
            }
        } else {
            if (loan.getLoanProduct().isMultiDisburseLoan()) {
                disburseAmount = Money.of(loan.getCurrency(), principalDisbursed);
            } else {
                disburseAmount = disburseAmount.plus(principalDisbursed);
            }

            if (details.isEmpty()) {
                diff = loan.getLoanRepaymentScheduleDetail().getPrincipal().minus(principalDisbursed).getAmount();
            } else {
                for (LoanDisbursementDetails disbursementDetails : details) {
                    disbursementDetails.updateActualDisbursementDate(actualDisbursementDate);
                    disbursementDetails.updatePrincipal(principalDisbursed);
                }
            }
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (loan.loanProduct().isMultiDisburseLoan()) {
                Collection<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
                BigDecimal setPrincipalAmount = BigDecimal.ZERO;
                for (LoanDisbursementDetails disbursementDetails : loanDisburseDetails) {
                    if (disbursementDetails.actualDisbursementDate() != null) {
                        setPrincipalAmount = setPrincipalAmount.add(disbursementDetails.principal());
                    }
                    totalAmount = totalAmount.add(disbursementDetails.principal());
                }
                loan.getLoanRepaymentScheduleDetail().setPrincipal(setPrincipalAmount);
            } else {
                loan.getLoanRepaymentScheduleDetail()
                        .setPrincipal(loan.getLoanRepaymentScheduleDetail().getPrincipal().minus(diff).getAmount());
                totalAmount = loan.getLoanRepaymentScheduleDetail().getPrincipal().getAmount();
            }
            loanDisbursementValidator.compareDisbursedToApprovedOrProposedPrincipal(loan, disburseAmount.getAmount(), totalAmount);
        }
        return disburseAmount;
    }

    public void handleDisbursementTransaction(final Loan loan, final LocalDate disbursedOn, final PaymentDetail paymentDetail) {
        // add repayment transaction to track incoming money from client to mfi
        // for (charges due at time of disbursement)

        /*
         * TODO Vishwas: do we need to be able to pass in payment type details for repayments at disbursements too?
         */

        final Money totalFeeChargesDueAtDisbursement = loan.getSummary().getTotalFeeChargesDueAtDisbursement(loan.getCurrency());
        /*
         * all Charges repaid at disbursal is marked as repaid and "APPLY Charge" transactions are created for all other
         * fees ( which are created during disbursal but not repaid)
         */

        Money disbursentMoney = Money.zero(loan.getCurrency());
        final LoanTransaction chargesPayment = LoanTransaction.repaymentAtDisbursement(loan.getOffice(), disbursentMoney, paymentDetail,
                disbursedOn, null);
        final Integer installmentNumber = null;
        for (final LoanCharge charge : loan.getActiveCharges()) {
            LocalDate actualDisbursementDate = loan.getActualDisbursementDate(charge);

            boolean isDisbursementCharge = charge.getCharge().getChargeTimeType().equals(ChargeTimeType.DISBURSEMENT.getValue())
                    && disbursedOn.equals(actualDisbursementDate) && !charge.isWaived() && !charge.isFullyPaid();

            boolean isTrancheDisbursementCharge = charge.getCharge().getChargeTimeType()
                    .equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue()) && disbursedOn.equals(actualDisbursementDate)
                    && !charge.isWaived() && !charge.isFullyPaid();

            /*
             * create a Charge applied transaction if Up front Accrual, None or Cash based accounting is enabled
             */
            if (isDisbursementCharge || isTrancheDisbursementCharge) {
                if (totalFeeChargesDueAtDisbursement.isGreaterThanZero() && !charge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
                    charge.markAsFullyPaid();
                    // Add "Loan Charge Paid By" details to this transaction
                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(chargesPayment, charge, charge.amount(),
                            installmentNumber);
                    chargesPayment.getLoanChargesPaid().add(loanChargePaidBy);
                    disbursentMoney = disbursentMoney.plus(charge.amount());
                }
            } else if (disbursedOn.equals(loan.getActualDisbursementDate())
                    && loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
                final LoanTransaction applyLoanChargeTransaction = loanChargeService.handleChargeAppliedTransaction(loan, charge,
                        disbursedOn);
                if (applyLoanChargeTransaction != null) {
                    loanTransactionRepository.saveAndFlush(applyLoanChargeTransaction);
                    loanJournalEntryPoster.postJournalEntriesForLoanTransaction(applyLoanChargeTransaction, false, false);
                }
            }
        }

        if (disbursentMoney.isGreaterThanZero()) {
            final Money zero = Money.zero(loan.getCurrency());
            chargesPayment.updateComponentsAndTotal(zero, zero, disbursentMoney, zero);
            chargesPayment.updateLoan(loan);
            loan.addLoanTransaction(chargesPayment);
            loanTransactionRepository.saveAndFlush(chargesPayment);
            loanJournalEntryPoster.postJournalEntriesForLoanTransaction(chargesPayment, false, false);
            loanBalanceService.updateLoanOutstandingBalances(loan);
        }

        final LocalDate expectedDate = loan.getExpectedFirstRepaymentOnDate();
        loanDisbursementValidator.validateDisburseDate(loan, disbursedOn, expectedDate);
    }

    private void createOrUpdateDisbursementDetails(final Loan loan, final Long disbursementID, final Map<String, Object> actualChanges,
            final LocalDate expectedDisbursementDate, final BigDecimal principal, final List<Long> existingDisbursementList) {
        if (disbursementID != null) {
            LoanDisbursementDetails loanDisbursementDetail = loan.fetchLoanDisbursementsById(disbursementID);
            existingDisbursementList.remove(disbursementID);
            if (loanDisbursementDetail.actualDisbursementDate() == null) {
                LocalDate actualDisbursementDate = null;
                LoanDisbursementDetails disbursementDetails = new LoanDisbursementDetails(expectedDisbursementDate, actualDisbursementDate,
                        principal, loan.getNetDisbursalAmount(), false);
                disbursementDetails.updateLoan(loan);
                if (!loanDisbursementDetail.equals(disbursementDetails)) {
                    loanDisbursementDetail.copy(disbursementDetails);
                    actualChanges.put("disbursementDetailId", disbursementID);
                    actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
                }
            }
        } else {
            final var disbursementDetails = loan.addLoanDisbursementDetails(expectedDisbursementDate, principal);
            for (LoanTrancheCharge trancheCharge : loan.getTrancheCharges()) {
                Charge chargeDefinition = trancheCharge.getCharge();
                ExternalId externalId = ExternalId.empty();
                if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
                    externalId = ExternalId.generate();
                }
                final LoanCharge loanCharge = loanChargeService.create(loan, chargeDefinition, principal, null, null, null,
                        expectedDisbursementDate, null, null, BigDecimal.ZERO, externalId);
                LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge,
                        disbursementDetails);
                loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);

                loanChargeValidator.validateChargeAdditionForDisbursedLoan(loan, loanCharge);
                loanChargeValidator.validateChargeHasValidSpecifiedDateIfApplicable(loan, loanCharge, loan.getDisbursementDate());
                loanChargeService.addLoanCharge(loan, loanCharge);
            }
            actualChanges.put(LoanApiConstants.disbursementDataParameterName, expectedDisbursementDate + "-" + principal);
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
        }
    }

    private void removeDisbursementAndAssociatedCharges(final Loan loan, final Map<String, Object> actualChanges,
            final List<Long> disbursementList, final List<Long> loanChargeIds, final int chargeIdLength, final boolean removeAllCharges) {
        if (removeAllCharges) {
            final LoanCharge[] tempCharges = new LoanCharge[loan.getCharges().size()];
            loan.getCharges().toArray(tempCharges);
            for (LoanCharge loanCharge : tempCharges) {
                loanChargeValidator.validateLoanIsNotClosed(loan, loanCharge);
                loanChargeValidator.validateLoanChargeIsNotWaived(loan, loanCharge);
                reprocessLoanTransactionsService.removeLoanCharge(loan, loanCharge);
            }
            loan.getTrancheCharges().clear();
        } else {
            if (!loanChargeIds.isEmpty() && loanChargeIds.size() != chargeIdLength) {
                for (Long chargeId : loanChargeIds) {
                    final LoanCharge deleteCharge = loanChargeService.fetchLoanChargesById(loan, chargeId);
                    if (loan.getCharges().contains(deleteCharge)) {
                        loanChargeValidator.validateLoanIsNotClosed(loan, deleteCharge);
                        loanChargeValidator.validateLoanChargeIsNotWaived(loan, deleteCharge);
                        reprocessLoanTransactionsService.removeLoanCharge(loan, deleteCharge);
                    }
                }
            }
        }
        for (Long id : disbursementList) {
            removeChargesByDisbursementID(loan, id);
            loan.removeDisbursementDetails(id);
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
        }
    }

    private void removeChargesByDisbursementID(final Loan loan, final Long id) {
        loan.getCharges().stream() //
                .filter(charge -> { //
                    final LoanTrancheDisbursementCharge transCharge = charge.getTrancheDisbursementCharge(); //
                    if (transCharge == null || !Objects.equals(id, transCharge.getloanDisbursementDetails().getId())) {
                        return false;
                    }
                    loanChargeValidator.validateLoanIsNotClosed(loan, charge); //
                    loanChargeValidator.validateLoanChargeIsNotWaived(loan, charge); //
                    return true; //
                }) //
                .forEach(loanCharge -> reprocessLoanTransactionsService.removeLoanCharge(loan, loanCharge));
    }

    // This method returns date format and locale if present in the JsonCommand
    private Map<String, String> getDateFormatAndLocale(final JsonCommand jsonCommand) {
        Map<String, String> returnObject = new HashMap<>();
        JsonElement jsonElement = jsonCommand.parsedJson();
        if (jsonElement.isJsonObject()) {
            JsonObject topLevel = jsonElement.getAsJsonObject();
            if (topLevel.has(LoanApiConstants.dateFormatParameterName)
                    && topLevel.get(LoanApiConstants.dateFormatParameterName).isJsonPrimitive()) {
                final JsonPrimitive primitive = topLevel.get(LoanApiConstants.dateFormatParameterName).getAsJsonPrimitive();
                returnObject.put(LoanApiConstants.dateFormatParameterName, primitive.getAsString());
            }
            if (topLevel.has(LoanApiConstants.localeParameterName)
                    && topLevel.get(LoanApiConstants.localeParameterName).isJsonPrimitive()) {
                final JsonPrimitive primitive = topLevel.get(LoanApiConstants.localeParameterName).getAsJsonPrimitive();
                String localeString = primitive.getAsString();
                returnObject.put(LoanApiConstants.localeParameterName, localeString);
            }
        }
        return returnObject;
    }

    private Map<String, Object> parseDisbursementDetails(final JsonObject jsonObject, String dateFormat, Locale locale) {
        Map<String, Object> returnObject = new HashMap<>();
        if (jsonObject.get(LoanApiConstants.expectedDisbursementDateParameterName) != null
                && jsonObject.get(LoanApiConstants.expectedDisbursementDateParameterName).isJsonPrimitive()) {
            final JsonPrimitive primitive = jsonObject.get(LoanApiConstants.expectedDisbursementDateParameterName).getAsJsonPrimitive();
            final String valueAsString = primitive.getAsString();
            if (StringUtils.isNotBlank(valueAsString)) {
                LocalDate date = JsonParserHelper.convertFrom(valueAsString, LoanApiConstants.expectedDisbursementDateParameterName,
                        dateFormat, locale);
                if (date != null) {
                    returnObject.put(LoanApiConstants.expectedDisbursementDateParameterName, date);
                }
            }
        }

        if (jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).isJsonPrimitive()
                && StringUtils.isNotBlank(jsonObject.get(LoanApiConstants.disbursementPrincipalParameterName).getAsString())) {
            BigDecimal principal = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementPrincipalParameterName).getAsBigDecimal();
            returnObject.put(LoanApiConstants.disbursementPrincipalParameterName, principal);
        }

        if (jsonObject.has(LoanApiConstants.disbursementIdParameterName)
                && jsonObject.get(LoanApiConstants.disbursementIdParameterName).isJsonPrimitive()
                && StringUtils.isNotBlank(jsonObject.get(LoanApiConstants.disbursementIdParameterName).getAsString())) {
            Long id = jsonObject.getAsJsonPrimitive(LoanApiConstants.disbursementIdParameterName).getAsLong();
            returnObject.put(LoanApiConstants.disbursementIdParameterName, id);
        }

        if (jsonObject.has(LoanApiConstants.loanChargeIdParameterName)
                && jsonObject.get(LoanApiConstants.loanChargeIdParameterName).isJsonPrimitive()
                && StringUtils.isNotBlank(jsonObject.get(LoanApiConstants.loanChargeIdParameterName).getAsString())) {
            returnObject.put(LoanApiConstants.loanChargeIdParameterName,
                    jsonObject.getAsJsonPrimitive(LoanApiConstants.loanChargeIdParameterName).getAsString());
        }
        return returnObject;
    }

}
