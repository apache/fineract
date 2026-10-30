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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanAddChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.common.service.Validator;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheDisbursementCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDisbursementValidator;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.jspecify.annotations.NonNull;

@Slf4j
@RequiredArgsConstructor
public class LoanDisbursementService {

    private final LoanChargeValidator loanChargeValidator;
    private final LoanDisbursementValidator loanDisbursementValidator;
    private final LoanChargeService loanChargeService;
    private final LoanBalanceService loanBalanceService;
    private final LoanJournalEntryPoster loanJournalEntryPoster;
    private final LoanTransactionRepository loanTransactionRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final BusinessEventNotifierService businessEventNotifierService;

    private static final Comparator<LoanCharge> BY_ID = Comparator.comparing(LoanCharge::getId, Comparator.nullsLast(Long::compareTo));

    public void updateDisbursementDetails(final Loan loan, final JsonCommand jsonCommand, final Map<String, Object> actualChanges) {
        updateDisbursementDetails(loan, jsonCommand, actualChanges, true);
    }

    /**
     * @param createDownPaymentFees
     *            false when the request's charges bring the down payment fee of every tranche themselves (modified
     *            application, see {@link LoanChargeAssembler}), so a tranche added here gets none of its own
     */
    public void updateDisbursementDetails(final Loan loan, final JsonCommand jsonCommand, final Map<String, Object> actualChanges,
            final boolean createDownPaymentFees) {
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
                            disbursementList, createDownPaymentFees);
                }
                removeDisbursementAndAssociatedCharges(loan, actualChanges, disbursementList, loanChargeIds, chargeIdLength,
                        removeAllCharges);
            }
        }
    }

    public Money adjustDisburseAmount(final Loan loan, @NonNull final JsonCommand command,
            @NonNull final LocalDate actualDisbursementDate) {
        Money disburseAmount = loan.getLoanRepaymentScheduleDetail().getPrincipal().zero();
        final BigDecimal principalDisbursed = command.bigDecimalValueOfParameterNamed(LoanApiConstants.principalDisbursedParameterName);
        if (loan.getActualDisbursementDate() == null || DateUtils.isBefore(actualDisbursementDate, loan.getActualDisbursementDate())) {
            loan.setActualDisbursementDate(actualDisbursementDate);
        }
        BigDecimal diff = BigDecimal.ZERO;
        final Collection<LoanDisbursementDetails> rawDetails = loan.fetchUndisbursedDetail();
        final Collection<LoanDisbursementDetails> details = hasMultipleTranchesOnSameDateWithSameExpectedDate(rawDetails,
                actualDisbursementDate) ? sortDisbursementDetailsByBusinessRules(rawDetails) : rawDetails;
        if (principalDisbursed == null) {
            disburseAmount = loan.getLoanRepaymentScheduleDetail().getPrincipal();
            if (!details.isEmpty()) {
                // When no specific amount provided, disburse ALL undisbursed tranches for the date
                disburseAmount = disburseAmount.zero();
                for (LoanDisbursementDetails disbursementDetails : details) {
                    disbursementDetails.updateActualDisbursementDate(actualDisbursementDate);
                    disburseAmount = disburseAmount.plus(disbursementDetails.getPrincipal());
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
                // Check if this is a tranche-based loan (has multiple predefined disbursement details)
                // versus a non-tranche multi-disbursal loan (creates disbursement details on-the-fly)
                boolean isTrancheBasedLoan = hasMultipleOrPreDefinedDisbursementDetails(loan, details);

                if (isTrancheBasedLoan && details.size() >= 1) {
                    // For tranche-based loans, find the matching tranche by amount first, then by order
                    LoanDisbursementDetails selectedTranche = null;

                    // First try to find a tranche that exactly matches the requested disbursement amount
                    for (LoanDisbursementDetails disbursementDetails : details) {
                        if (disbursementDetails.actualDisbursementDate() == null
                                && disbursementDetails.getPrincipal().compareTo(principalDisbursed) == 0) {
                            selectedTranche = disbursementDetails;
                            break;
                        }
                    }

                    // If no exact match found, take the first available tranche (next in line)
                    if (selectedTranche == null) {
                        for (LoanDisbursementDetails disbursementDetails : details) {
                            if (disbursementDetails.actualDisbursementDate() == null) {
                                selectedTranche = disbursementDetails;
                                break;
                            }
                        }
                    }

                    if (selectedTranche != null) {
                        // Update the selected tranche with the actual disbursement
                        selectedTranche.updateActualDisbursementDate(actualDisbursementDate);
                        selectedTranche.updatePrincipal(principalDisbursed);
                    }
                } else {
                    // For non-tranche multi-disbursal loans: preserve original behavior
                    // Update all available disbursement details with the actual disbursement date and amount
                    for (LoanDisbursementDetails disbursementDetails : details) {
                        disbursementDetails.updateActualDisbursementDate(actualDisbursementDate);
                        disbursementDetails.updatePrincipal(principalDisbursed);
                    }
                }
            }
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (loan.loanProduct().isMultiDisburseLoan()) {
                Collection<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
                BigDecimal setPrincipalAmount = BigDecimal.ZERO;
                for (LoanDisbursementDetails disbursementDetails : loanDisburseDetails) {
                    if (disbursementDetails.actualDisbursementDate() != null) {
                        setPrincipalAmount = setPrincipalAmount.add(disbursementDetails.getPrincipal());
                    }
                    totalAmount = totalAmount.add(disbursementDetails.getPrincipal());
                }
                loan.getLoanRepaymentScheduleDetail().setPrincipal(setPrincipalAmount);
            } else {
                loan.getLoanRepaymentScheduleDetail()
                        .setPrincipal(loan.getLoanRepaymentScheduleDetail().getPrincipal().minus(diff).getAmount());
                totalAmount = loan.getLoanRepaymentScheduleDetail().getPrincipal().getAmount();
            }
            loanDisbursementValidator.compareDisbursedToApprovedOrProposedPrincipal(loan, totalAmount);
        }
        return disburseAmount;
    }

    public void handleDisbursementTransaction(final Loan loan, final LocalDate disbursedOn, final PaymentDetail paymentDetail,
            final boolean isAccountTransfer) {
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
             * create a Charge applied transaction only when Up front Accrual accounting is enabled. No accrual
             * transactions should be created for None or Cash based accounting.
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
            } else if (disbursedOn.equals(loan.getActualDisbursementDate()) && (configurationDomainService.isAllowCashAndNonCashAccrual()
                    ? loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()
                    : loan.isUpfrontAccrualAccountingEnabledOnLoanProduct())) {
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
            loanJournalEntryPoster.postJournalEntriesForLoanTransaction(chargesPayment, isAccountTransfer, false);
            loanBalanceService.updateLoanOutstandingBalances(loan);
        }

        final LocalDate expectedDate = loan.getExpectedFirstRepaymentOnDate();
        loanDisbursementValidator.validateDisburseDate(loan, disbursedOn, expectedDate);
    }

    private void createOrUpdateDisbursementDetails(final Loan loan, final Long disbursementID, final Map<String, Object> actualChanges,
            final LocalDate expectedDisbursementDate, final BigDecimal principal, final List<Long> existingDisbursementList,
            final boolean createDownPaymentFees) {
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
                    updateDownPaymentFeesOfTranche(loan, loanDisbursementDetail);
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
            if (createDownPaymentFees) {
                addDownPaymentFeesForTranche(loan, disbursementDetails);
            }
            actualChanges.put(LoanApiConstants.disbursementDataParameterName, expectedDisbursementDate + "-" + principal);
            actualChanges.put(RECALCULATE_LOAN_SCHEDULE, true);
        }
    }

    /**
     * Before schedule regeneration on actual disbursement: with skipDownPaymentFee the unpaid fees of the disbursed
     * tranches are deactivated, so they can be added manually later; otherwise they move onto the actual disbursement
     * date. Like a disbursement fee, a fee created with the application of a loan without expected tranches is not
     * linked to a tranche and belongs to the first disbursement.
     *
     * @param disbursedTranches
     *            tranches disbursed now; empty for a loan without tranches
     * @param downPaymentFeeAmount
     *            amount overriding the configured fee of the disbursed down payment period; null to use the configured
     *            charge
     * @return true if fees were deactivated (caller should regenerate the schedule)
     */
    public boolean prepareDownPaymentFeesForDisbursement(final Loan loan, final List<LoanDisbursementDetails> disbursedTranches,
            final LocalDate actualDisbursementDate, final boolean skipDownPaymentFee, final BigDecimal downPaymentFeeAmount) {
        if (downPaymentFeeAmount != null) {
            validateDownPaymentFeeAmount(loan, disbursedTranches, downPaymentFeeAmount);
        }
        if (!loan.getLoanRepaymentScheduleDetail().isEnableDownPayment()) {
            return false;
        }
        final boolean firstDisbursement = loan.isApproved();
        final List<LoanCharge> unpaidFees = loan.getActiveCharges().stream().filter(fee -> fee.isDueAtDownPayment() && !fee.isPaid()
                && (fee.getDisbursementDetail() == null ? firstDisbursement : disbursedTranches.stream().anyMatch(fee::isDownPaymentFeeOf)))
                .toList();
        if (skipDownPaymentFee) {
            unpaidFees.forEach(fee -> fee.setActive(false));
            if (!unpaidFees.isEmpty()) {
                final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
                wrapper.reprocess(loan.getCurrency(), loan.getDisbursementDate(), loan.getRepaymentScheduleInstallments(),
                        loan.getActiveCharges());
                loanBalanceService.updateLoanSummaryDerivedFields(loan);
            }
            return !unpaidFees.isEmpty();
        }
        for (final LoanCharge fee : unpaidFees) {
            if (downPaymentFeeAmount == null) {
                loanChargeService.updateDownPaymentFee(loan, fee, actualDisbursementDate);
            } else {
                loanChargeService.overrideDownPaymentFeeAmount(loan, fee, downPaymentFeeAmount, actualDisbursementDate);
            }
        }
        return false;
    }

    /**
     * The override needs exactly one down payment period created by this disbursement and exactly one down payment
     * charge to apply to it.
     */
    private void validateDownPaymentFeeAmount(final Loan loan, final List<LoanDisbursementDetails> disbursedTranches,
            final BigDecimal downPaymentFeeAmount) {
        Validator.validateOrThrow("loan.disbursement", baseDataValidator -> {
            baseDataValidator.reset().parameter(LoanApiConstants.downPaymentFeeAmountParameterName).value(downPaymentFeeAmount);
            final LoanDisbursementDetails tranche = disbursedTranches.isEmpty() ? null : disbursedTranches.getFirst();
            final int downPaymentCharges = downPaymentChargeDefinitionsOn(loan).size();
            if (!loan.getLoanRepaymentScheduleDetail().isEnableDownPayment()
                    || loanChargeService.calculateDownPaymentAmountBase(loan, tranche).signum() == 0) {
                baseDataValidator.failWithCodeAndMessage("not.supported.without.down.payment.period",
                        "Down payment fee amount cannot be passed because the disbursement does not create a down payment period.");
            } else if (disbursedTranches.size() > 1) {
                baseDataValidator.failWithCodeAndMessage("not.supported.with.multiple.down.payment.periods",
                        "Down payment fee amount cannot be passed because the disbursement creates more than one down payment period.");
            } else if (downPaymentCharges == 0) {
                baseDataValidator.failWithCodeAndMessage("not.supported.without.down.payment.fee.charge",
                        "Down payment fee amount cannot be passed because the loan has no down payment fee charge.");
            } else if (downPaymentCharges > 1) {
                baseDataValidator.failWithCodeAndMessage("not.supported.with.multiple.down.payment.fee.charges",
                        "Down payment fee amount cannot be passed because the loan has more than one down payment fee charge.");
            }
        });
    }

    /**
     * After schedule regeneration on actual disbursement: each disbursed tranche (or the loan without tranches) has one
     * fee per down payment charge of the loan. Raises {@link LoanAddChargeBusinessEvent} for a fee already on the loan
     * (created with the application); a fee created here raises it on creation. No-op when the fee was skipped.
     */
    public void completeDownPaymentFeesForDisbursement(final Loan loan, final List<LoanDisbursementDetails> disbursedTranches,
            final LocalDate actualDisbursementDate, final boolean skipDownPaymentFee, final BigDecimal downPaymentFeeAmount) {
        if (skipDownPaymentFee || !loan.getLoanRepaymentScheduleDetail().isEnableDownPayment()) {
            return;
        }
        if (disbursedTranches.isEmpty()) {
            ensureDownPaymentFees(loan, null, actualDisbursementDate, true, downPaymentFeeAmount);
        }
        // the application fees (not linked to a tranche) belong to the first tranche of the first disbursement
        final boolean firstDisbursement = loan.isApproved();
        for (int index = 0; index < disbursedTranches.size(); index++) {
            final LoanDisbursementDetails tranche = disbursedTranches.get(index);
            ensureDownPaymentFees(loan, tranche, actualDisbursementDate, firstDisbursement && index == 0, downPaymentFeeAmount);
        }
    }

    private void ensureDownPaymentFees(final Loan loan, final LoanDisbursementDetails tranche, final LocalDate dueDate,
            final boolean includeApplicationFees, final BigDecimal downPaymentFeeAmount) {
        for (final Charge chargeDefinition : downPaymentChargeDefinitionsOn(loan)) {
            final LoanCharge fee = loan.getActiveCharges().stream()
                    .filter(charge -> charge.isDueAtDownPayment() && Objects.equals(charge.getCharge().getId(), chargeDefinition.getId())
                            && (charge.isDownPaymentFeeOf(tranche) || (includeApplicationFees && charge.getDisbursementDetail() == null
                                    && DateUtils.isEqual(dueDate, charge.getDueLocalDate()))))
                    .min(BY_ID).orElse(null);
            if (fee == null) {
                createDownPaymentFee(loan, chargeDefinition, tranche, dueDate, downPaymentFeeAmount);
            } else {
                notifyLoanAddCharge(fee);
            }
        }
    }

    /** Moves the unpaid down payment fees not linked to a tranche to the loan's new expected disbursement date. */
    public void moveDownPaymentFeesToExpectedDisbursementDate(final Loan loan, final LocalDate expectedDisbursementDate) {
        if (!loan.getLoanRepaymentScheduleDetail().isEnableDownPayment() || expectedDisbursementDate == null) {
            return;
        }
        for (final LoanCharge fee : List.copyOf(loan.getActiveCharges())) {
            if (fee.isDueAtDownPayment() && !fee.isPaid() && fee.getDisbursementDetail() == null) {
                loanChargeService.updateDownPaymentFee(loan, fee, expectedDisbursementDate);
            }
        }
    }

    public void updateDownPaymentFeesOfTranche(final Loan loan, final LoanDisbursementDetails tranche) {
        for (final LoanCharge fee : List.copyOf(loan.getActiveCharges())) {
            if (fee.isDownPaymentFeeOf(tranche) && !fee.isPaid()) {
                loanChargeService.updateDownPaymentFee(loan, fee, tranche.actualOrExpectedDisbursementDate());
            }
        }
    }

    private void addDownPaymentFeesForTranche(final Loan loan, final LoanDisbursementDetails tranche) {
        if (!loan.getLoanRepaymentScheduleDetail().isEnableDownPayment()) {
            return;
        }
        for (final Charge chargeDefinition : downPaymentChargeDefinitionsOn(loan)) {
            createDownPaymentFee(loan, chargeDefinition, tranche, tranche.expectedDisbursementDate(), null);
        }
    }

    /**
     * Undo of the last disbursement (or full undo of a loan whose tranches are created on disbursement) reverses the
     * tranche, so the tranche's down payment fees are left without a down payment period. Like tranche disbursement
     * charges, they are reset and deactivated; the next disbursement creates its own fees.
     */
    public void deactivateDownPaymentFeesOfTranche(final Loan loan, final LoanDisbursementDetails tranche) {
        for (final LoanCharge fee : List.copyOf(loan.getActiveCharges())) {
            if (fee.isDownPaymentFeeOf(tranche)) {
                fee.resetPaidAmount(loan.getCurrency());
                fee.setActive(false);
            }
        }
    }

    private Set<Charge> downPaymentChargeDefinitionsOn(final Loan loan) {
        return loan.getCharges().stream().filter(LoanCharge::isDueAtDownPayment).map(LoanCharge::getCharge)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** An overridden fee is a flat fee of {@code downPaymentFeeAmount}, so recalculation keeps the amount. */
    private void createDownPaymentFee(final Loan loan, final Charge chargeDefinition, final LoanDisbursementDetails tranche,
            final LocalDate dueDate, final BigDecimal downPaymentFeeAmount) {
        ExternalId externalId = ExternalId.empty();
        if (TemporaryConfigurationServiceContainer.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }
        final ChargeCalculationType chargeCalculation = downPaymentFeeAmount == null ? null : ChargeCalculationType.FLAT;
        final LoanCharge loanCharge = loanChargeService.create(loan, chargeDefinition,
                loanChargeService.calculateDownPaymentAmountBase(loan, tranche), downPaymentFeeAmount, null, chargeCalculation, dueDate,
                null, null, BigDecimal.ZERO, externalId);
        loanCharge.setDisbursementDetail(tranche);
        loanChargeValidator.validateChargeAdditionForDisbursedLoan(loan, loanCharge);
        loanChargeValidator.validateChargeHasValidSpecifiedDateIfApplicable(loan, loanCharge, loan.getDisbursementDate());
        businessEventNotifierService.notifyPreBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
        loanChargeService.addLoanCharge(loan, loanCharge);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
    }

    private void notifyLoanAddCharge(final LoanCharge loanCharge) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
    }

    private void removeDisbursementAndAssociatedCharges(final Loan loan, final Map<String, Object> actualChanges,
            final List<Long> disbursementList, final List<Long> loanChargeIds, final int chargeIdLength, final boolean removeAllCharges) {
        if (removeAllCharges) {
            final LoanCharge[] tempCharges = new LoanCharge[loan.getCharges().size()];
            loan.getCharges().toArray(tempCharges);
            for (LoanCharge loanCharge : tempCharges) {
                loanChargeValidator.validateLoanIsNotClosed(loan, loanCharge);
                loanChargeValidator.validateLoanChargeIsNotWaived(loan, loanCharge);
                loanChargeService.removeLoanCharge(loan, loanCharge);
            }
            loan.getTrancheCharges().clear();
        } else {
            if (!loanChargeIds.isEmpty() && loanChargeIds.size() != chargeIdLength) {
                for (Long chargeId : loanChargeIds) {
                    final LoanCharge deleteCharge = loanChargeService.fetchLoanChargesById(loan, chargeId);
                    if (loan.getCharges().contains(deleteCharge)) {
                        loanChargeValidator.validateLoanIsNotClosed(loan, deleteCharge);
                        loanChargeValidator.validateLoanChargeIsNotWaived(loan, deleteCharge);
                        loanChargeService.removeLoanCharge(loan, deleteCharge);
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
        final LoanDisbursementDetails detail = loan.fetchLoanDisbursementsById(id);
        loan.getCharges().stream() //
                .filter(charge -> { //
                    final LoanTrancheDisbursementCharge transCharge = charge.getTrancheDisbursementCharge(); //
                    if ((transCharge == null || !Objects.equals(id, transCharge.getloanDisbursementDetails().getId()))
                            && !charge.isDownPaymentFeeOf(detail)) {
                        return false;
                    }
                    loanChargeValidator.validateLoanIsNotClosed(loan, charge); //
                    loanChargeValidator.validateLoanChargeIsNotWaived(loan, charge); //
                    return true; //
                }) //
                .toList().forEach(loanCharge -> loanChargeService.removeLoanCharge(loan, loanCharge));
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

    private boolean hasMultipleOrPreDefinedDisbursementDetails(final Loan loan,
            final Collection<LoanDisbursementDetails> undisbursedDetails) {
        Collection<LoanDisbursementDetails> allDisbursementDetails = loan.getDisbursementDetails();

        if (undisbursedDetails.size() > 1) {
            return true;
        }

        if (allDisbursementDetails.size() > 1 && !undisbursedDetails.isEmpty()) {
            return true;
        }

        if (undisbursedDetails.size() == 1) {
            LoanDisbursementDetails singleDetail = undisbursedDetails.iterator().next();
            BigDecimal loanPrincipal = loan.getLoanRepaymentScheduleDetail().getPrincipal().getAmount();

            if (singleDetail.getPrincipal().compareTo(loanPrincipal) == 0) {
                return false;
            }
        }

        // Default to tranche behavior for safety in ambiguous cases
        return true;
    }

    public static List<LoanDisbursementDetails> sortDisbursementDetailsByBusinessRules(
            Collection<LoanDisbursementDetails> disbursementDetails) {
        if (disbursementDetails == null || disbursementDetails.isEmpty()) {
            return List.of();
        }

        return disbursementDetails.stream()
                .sorted(Comparator.comparing(LoanDisbursementDetails::expectedDisbursementDate)
                        .thenComparing(
                                (LoanDisbursementDetails d1, LoanDisbursementDetails d2) -> d2.getPrincipal().compareTo(d1.getPrincipal()))
                        .thenComparing(LoanDisbursementDetails::getId))
                .collect(Collectors.toList());
    }

    public static boolean hasMultipleTranchesOnSameDate(Collection<LoanDisbursementDetails> disbursementDetails) {
        if (disbursementDetails == null || disbursementDetails.size() <= 1) {
            return false;
        }

        return disbursementDetails.stream()
                .collect(Collectors.groupingBy(LoanDisbursementDetails::expectedDisbursementDate, Collectors.counting())).values().stream()
                .anyMatch(count -> count > 1);
    }

    public static boolean hasMultipleTranchesOnSameDateWithSameExpectedDate(Collection<LoanDisbursementDetails> disbursementDetails,
            LocalDate actualDisbursementDate) {
        if (disbursementDetails == null || disbursementDetails.size() <= 1 || actualDisbursementDate == null) {
            return false;
        }

        long tranchesForActualDate = disbursementDetails.stream()
                .filter(detail -> actualDisbursementDate.equals(detail.expectedDisbursementDate())).count();

        return tranchesForActualDate > 1;
    }

}
