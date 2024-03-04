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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction.accrueTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class LoanAccrualWritePlatformServiceImpl implements LoanAccrualWritePlatformService {

    private static final String ACCRUAL_ON_CHARGE_DUE_DATE = "due-date";
    private static final String ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE = "submitted-date";
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanRepository loanRepository;
    private final OfficeRepository officeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;
    private final ConfigurationDomainService configurationDomainService;
    private final ExternalIdFactory externalIdFactory;

    @Override
    @Transactional
    public void addAccrualAccounting(final Long loanId, final Collection<LoanScheduleAccrualData> loanScheduleAccrualData) {
        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService.retrieveLoanChargesForAccrual(loanId);
        Collection<LoanSchedulePeriodData> loanWaiverScheduleData = new ArrayList<>(1);
        Collection<LoanTransactionData> loanWaiverTransactionData = new ArrayList<>(1);

        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualData) {
            if (accrualData.getWaivedInterestIncome() != null && loanWaiverScheduleData.isEmpty()) {
                loanWaiverScheduleData = this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId());
                loanWaiverTransactionData = this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId());
            }
            updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate());
            updateInterestIncome(accrualData, loanWaiverTransactionData, loanWaiverScheduleData, accrualData.getDueDateAsLocaldate());
            addAccrualAccounting(accrualData);
        }
    }

    @Override
    @Transactional
    public void addPeriodicAccruals(final LocalDate tillDate, Long loanId, Collection<LoanScheduleAccrualData> loanScheduleAccrualData) {
        boolean firstTime = true;
        LocalDate accruedTill = null;
        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService.retrieveLoanChargesForAccrual(loanId);
        Collection<LoanSchedulePeriodData> loanWaiverScheduleData = new ArrayList<>(1);
        Collection<LoanTransactionData> loanWaiverTransactionData = new ArrayList<>(1);
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualData) {
            if (accrualData.getWaivedInterestIncome() != null && loanWaiverScheduleData.isEmpty()) {
                loanWaiverScheduleData = this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId());
                loanWaiverTransactionData = this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId());
            }

            if (DateUtils.isAfter(accrualData.getDueDateAsLocaldate(), tillDate)) {
                if (accruedTill == null || firstTime) {
                    accruedTill = accrualData.getAccruedTill();
                    firstTime = false;
                }
                if (accruedTill == null || DateUtils.isBefore(accruedTill, tillDate)) {
                    updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), tillDate);
                    updateInterestIncome(accrualData, loanWaiverTransactionData, loanWaiverScheduleData, tillDate);
                    addAccrualTillSpecificDate(tillDate, accrualData);
                }
            } else {
                updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate());
                updateInterestIncome(accrualData, loanWaiverTransactionData, loanWaiverScheduleData, tillDate);
                addAccrualAccounting(accrualData);
                accruedTill = accrualData.getDueDateAsLocaldate();
            }
        }
    }

    private void addAccrualTillSpecificDate(final LocalDate tillDate, final LoanScheduleAccrualData accrualData) {
        LocalDate interestStartDate = accrualData.getFromDateAsLocaldate();
        if (DateUtils.isBefore(accrualData.getFromDateAsLocaldate(), accrualData.getInterestCalculatedFrom())) {
            if (DateUtils.isBefore(accrualData.getInterestCalculatedFrom(), accrualData.getDueDateAsLocaldate())) {
                interestStartDate = accrualData.getInterestCalculatedFrom();
            } else {
                interestStartDate = accrualData.getDueDateAsLocaldate();
            }
        }

        int totalNumberOfDays = Math.toIntExact(ChronoUnit.DAYS.between(interestStartDate, accrualData.getDueDateAsLocaldate()));
        LocalDate startDate = accrualData.getFromDateAsLocaldate();
        if (DateUtils.isBefore(startDate, accrualData.getInterestCalculatedFrom())) {
            if (DateUtils.isBefore(accrualData.getInterestCalculatedFrom(), tillDate)) {
                startDate = accrualData.getInterestCalculatedFrom();
            } else {
                startDate = tillDate;
            }
        }
        int daysToBeAccrued = Math.toIntExact(ChronoUnit.DAYS.between(startDate, tillDate));
        double interestPerDay = accrualData.getAccruableIncome().doubleValue() / totalNumberOfDays;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestPortion;
        BigDecimal feePortion = accrualData.getDueDateFeeIncome();
        BigDecimal penaltyPortion = accrualData.getDueDatePenaltyIncome();
        if (daysToBeAccrued >= totalNumberOfDays) {
            interestPortion = accrualData.getAccruableIncome();
        } else {
            interestPortion = BigDecimal.valueOf(interestPerDay * daysToBeAccrued);
        }
        interestPortion = interestPortion.setScale(accrualData.getCurrencyData().getDecimalPlaces(), MoneyHelper.getRoundingMode());

        BigDecimal totalAccInterest = accrualData.getAccruedInterestIncome();
        BigDecimal totalAccPenalty = accrualData.getAccruedPenaltyIncome();
        BigDecimal totalCreditedPenalty = accrualData.getCreditedPenalty();
        BigDecimal totalAccFee = accrualData.getAccruedFeeIncome();
        BigDecimal totalCreditedFee = accrualData.getCreditedFee();

        if (totalAccInterest == null) {
            totalAccInterest = BigDecimal.ZERO;
        }
        interestPortion = interestPortion.subtract(totalAccInterest);
        amount = amount.add(interestPortion);
        totalAccInterest = totalAccInterest.add(interestPortion);
        if (interestPortion.compareTo(BigDecimal.ZERO) == 0) {
            interestPortion = null;
        }
        if (feePortion != null) {
            if (totalAccFee == null) {
                totalAccFee = BigDecimal.ZERO;
            }
            if (totalCreditedFee == null) {
                totalCreditedFee = BigDecimal.ZERO;
            }
            feePortion = feePortion.subtract(totalAccFee).subtract(totalCreditedFee);
            amount = amount.add(feePortion);
            totalAccFee = totalAccFee.add(feePortion);
            if (feePortion.compareTo(BigDecimal.ZERO) == 0) {
                feePortion = null;
            }
        }

        if (penaltyPortion != null) {
            if (totalAccPenalty == null) {
                totalAccPenalty = BigDecimal.ZERO;
            }
            if (totalCreditedPenalty == null) {
                totalCreditedPenalty = BigDecimal.ZERO;
            }
            penaltyPortion = penaltyPortion.subtract(totalAccPenalty).subtract(totalCreditedPenalty);
            amount = amount.add(penaltyPortion);
            totalAccPenalty = totalAccPenalty.add(penaltyPortion);
            if (penaltyPortion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyPortion = null;
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            addAccrualAccounting(accrualData, amount, interestPortion, totalAccInterest, feePortion, totalAccFee, penaltyPortion,
                    totalAccPenalty, tillDate);
        }
    }

    @Transactional
    public void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData) {

        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestPortion = null;
        BigDecimal totalAccInterest = null;
        if (scheduleAccrualData.getAccruableIncome() != null) {
            interestPortion = scheduleAccrualData.getAccruableIncome();
            totalAccInterest = interestPortion;
            if (scheduleAccrualData.getAccruedInterestIncome() != null) {
                interestPortion = interestPortion.subtract(scheduleAccrualData.getAccruedInterestIncome());
            }
            amount = amount.add(interestPortion);
            if (interestPortion.compareTo(BigDecimal.ZERO) == 0) {
                interestPortion = null;
            }
        }

        BigDecimal feePortion = null;
        BigDecimal totalAccFee = null;
        if (scheduleAccrualData.getDueDateFeeIncome() != null) {
            feePortion = scheduleAccrualData.getDueDateFeeIncome();
            totalAccFee = feePortion;
            if (scheduleAccrualData.getAccruedFeeIncome() != null) {
                feePortion = feePortion.subtract(scheduleAccrualData.getAccruedFeeIncome());
            }
            if (scheduleAccrualData.getCreditedFee() != null) {
                feePortion = feePortion.subtract(scheduleAccrualData.getCreditedFee());
            }
            amount = amount.add(feePortion);
            if (feePortion.compareTo(BigDecimal.ZERO) == 0) {
                feePortion = null;
            }
        }

        BigDecimal penaltyPortion = null;
        BigDecimal totalAccPenalty = null;
        if (scheduleAccrualData.getDueDatePenaltyIncome() != null) {
            penaltyPortion = scheduleAccrualData.getDueDatePenaltyIncome();
            totalAccPenalty = penaltyPortion;
            if (scheduleAccrualData.getAccruedPenaltyIncome() != null) {
                penaltyPortion = penaltyPortion.subtract(scheduleAccrualData.getAccruedPenaltyIncome());
            }
            if (scheduleAccrualData.getCreditedPenalty() != null) {
                penaltyPortion = penaltyPortion.subtract(scheduleAccrualData.getCreditedPenalty());
            }
            amount = amount.add(penaltyPortion);
            if (penaltyPortion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyPortion = null;
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            final String chargeAccrualDateCriteria = configurationDomainService.getAccrualDateConfigForCharge();
            if (chargeAccrualDateCriteria.equalsIgnoreCase(ACCRUAL_ON_CHARGE_DUE_DATE)) {
                addAccrualAccounting(scheduleAccrualData, amount, interestPortion, totalAccInterest, feePortion, totalAccFee,
                        penaltyPortion, totalAccPenalty, scheduleAccrualData.getDueDateAsLocaldate());
            } else if (chargeAccrualDateCriteria.equalsIgnoreCase(ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE)) {
                addAccrualAccounting(scheduleAccrualData, amount, interestPortion, totalAccInterest, feePortion, totalAccFee,
                        penaltyPortion, totalAccPenalty, DateUtils.getBusinessLocalDate());
            }
        }
    }

    private void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData, BigDecimal amount, BigDecimal interestPortion,
            BigDecimal totalAccInterest, BigDecimal feePortion, BigDecimal totalAccFee, BigDecimal penaltyPortion,
            BigDecimal totalAccPenalty, final LocalDate accruedTill) throws DataAccessException {
        AppUser user = context.authenticatedUser();
        Loan loan = loanRepository.getReferenceById(scheduleAccrualData.getLoanId());
        Office office = officeRepository.getReferenceById(scheduleAccrualData.getOfficeId());
        LoanTransaction loanTransaction = loanTransactionRepository.saveAndFlush(accrueTransaction(loan, office, accruedTill, amount,
                interestPortion, feePortion, penaltyPortion, externalIdFactory.create()));

        Map<LoanChargeData, BigDecimal> applicableCharges = scheduleAccrualData.getApplicableCharges();
        String chargesPaidSql = "INSERT INTO m_loan_charge_paid_by (loan_transaction_id, loan_charge_id, amount,installment_number) VALUES (?,?,?,?)";
        for (Map.Entry<LoanChargeData, BigDecimal> entry : applicableCharges.entrySet()) {
            LoanChargeData chargeData = entry.getKey();
            this.jdbcTemplate.update(chargesPaidSql, loanTransaction.getId(), chargeData.getId(), entry.getValue(),
                    scheduleAccrualData.getInstallmentNumber());
        }

        Map<String, Object> transactionMap = toMapData(loanTransaction.getId(), amount, interestPortion, feePortion, penaltyPortion,
                scheduleAccrualData, accruedTill);

        String repaymentUpdateSql = "UPDATE m_loan_repayment_schedule SET accrual_interest_derived=?, accrual_fee_charges_derived=?, "
                + "accrual_penalty_charges_derived=? WHERE  id=?";
        this.jdbcTemplate.update(repaymentUpdateSql, totalAccInterest, totalAccFee, totalAccPenalty,
                scheduleAccrualData.getRepaymentScheduleId());

        String updateLoan = "UPDATE m_loan  SET accrued_till=?, last_modified_by=?, last_modified_on_utc=?  WHERE  id=?";
        this.jdbcTemplate.update(updateLoan, accruedTill, user.getId(), DateUtils.getAuditOffsetDateTime(),
                scheduleAccrualData.getLoanId());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanAccrualTransactionCreatedBusinessEvent(loanTransaction));

        final Map<String, Object> accountingBridgeData = deriveAccountingBridgeData(scheduleAccrualData, transactionMap);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private Map<String, Object> deriveAccountingBridgeData(final LoanScheduleAccrualData loanScheduleAccrualData,
            final Map<String, Object> transactionMap) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("loanId", loanScheduleAccrualData.getLoanId());
        accountingBridgeData.put("loanProductId", loanScheduleAccrualData.getLoanProductId());
        accountingBridgeData.put("officeId", loanScheduleAccrualData.getOfficeId());
        accountingBridgeData.put("currencyCode", loanScheduleAccrualData.getCurrencyData().getCode());
        accountingBridgeData.put("cashBasedAccountingEnabled", false);
        accountingBridgeData.put("upfrontAccrualBasedAccountingEnabled", false);
        accountingBridgeData.put("periodicAccrualBasedAccountingEnabled", true);
        accountingBridgeData.put("isAccountTransfer", false);
        accountingBridgeData.put("isChargeOff", false);
        accountingBridgeData.put("isFraud", false);

        final List<Map<String, Object>> newLoanTransactions = new ArrayList<>();
        newLoanTransactions.add(transactionMap);

        accountingBridgeData.put("newLoanTransactions", newLoanTransactions);
        return accountingBridgeData;
    }

    public Map<String, Object> toMapData(final Long id, final BigDecimal amount, final BigDecimal interestPortion,
            final BigDecimal feePortion, final BigDecimal penaltyPortion, final LoanScheduleAccrualData loanScheduleAccrualData,
            final LocalDate accruedTill) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.ACCRUAL);

        thisTransactionData.put("id", id);
        thisTransactionData.put("officeId", loanScheduleAccrualData.getOfficeId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", false);
        thisTransactionData.put("date", accruedTill);
        thisTransactionData.put("currency", loanScheduleAccrualData.getCurrencyData());
        thisTransactionData.put("amount", amount);
        thisTransactionData.put("principalPortion", null);
        thisTransactionData.put("interestPortion", interestPortion);
        thisTransactionData.put("feeChargesPortion", feePortion);
        thisTransactionData.put("penaltyChargesPortion", penaltyPortion);
        thisTransactionData.put("overPaymentPortion", null);

        Map<LoanChargeData, BigDecimal> applicableCharges = loanScheduleAccrualData.getApplicableCharges();
        if (applicableCharges != null && !applicableCharges.isEmpty()) {
            final List<Map<String, Object>> loanChargesPaidData = new ArrayList<>();
            for (Map.Entry<LoanChargeData, BigDecimal> entry : applicableCharges.entrySet()) {
                LoanChargeData chargeData = entry.getKey();
                final Map<String, Object> loanChargePaidData = new LinkedHashMap<>();
                loanChargePaidData.put("chargeId", chargeData.getChargeId());
                loanChargePaidData.put("isPenalty", chargeData.isPenalty());
                loanChargePaidData.put("loanChargeId", chargeData.getId());
                loanChargePaidData.put("amount", entry.getValue());

                loanChargesPaidData.add(loanChargePaidData);
            }
            thisTransactionData.put("loanChargesPaid", loanChargesPaidData);
        }

        return thisTransactionData;
    }

    private void updateCharges(final Collection<LoanChargeData> chargesData, final LoanScheduleAccrualData accrualData,
            final LocalDate startDate, final LocalDate endDate) {
        final String chargeAccrualDateCriteria = configurationDomainService.getAccrualDateConfigForCharge();
        if (chargeAccrualDateCriteria.equalsIgnoreCase(ACCRUAL_ON_CHARGE_DUE_DATE)) {
            updateChargeForDueDate(chargesData, accrualData, startDate, endDate);
        } else if (chargeAccrualDateCriteria.equalsIgnoreCase(ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE)) {
            updateChargeForSubmittedOnDate(chargesData, accrualData, startDate, endDate);
        }

    }

    private void updateChargeForSubmittedOnDate(Collection<LoanChargeData> chargesData, LoanScheduleAccrualData accrualData,
            LocalDate startDate, LocalDate endDate) {
        final Map<LoanChargeData, BigDecimal> applicableCharges = new HashMap<>();
        BigDecimal submittedDateFeeIncome = BigDecimal.ZERO;
        BigDecimal submittedDatePenaltyIncome = BigDecimal.ZERO;
        LocalDate scheduleEndDate = accrualData.getDueDateAsLocaldate();
        for (LoanChargeData loanCharge : chargesData) {
            BigDecimal chargeAmount = BigDecimal.ZERO;
            if (((accrualData.getInstallmentNumber() == 1 && DateUtils.isEqual(startDate, loanCharge.getSubmittedOnDate())
                    && DateUtils.isEqual(startDate, loanCharge.getDueDate())) || DateUtils.isBefore(startDate, loanCharge.getDueDate()))
                    && !DateUtils.isBefore(endDate, loanCharge.getSubmittedOnDate())
                    && !DateUtils.isBefore(scheduleEndDate, loanCharge.getDueDate())) {
                chargeAmount = loanCharge.getAmount();
                if (loanCharge.getAmountUnrecognized() != null) {
                    chargeAmount = chargeAmount.subtract(loanCharge.getAmountUnrecognized());
                }
                boolean canAddCharge = chargeAmount.compareTo(BigDecimal.ZERO) > 0;
                if (canAddCharge && (loanCharge.getAmountAccrued() == null || chargeAmount.compareTo(loanCharge.getAmountAccrued()) != 0)) {
                    BigDecimal amountForAccrual = chargeAmount;
                    if (loanCharge.getAmountAccrued() != null) {
                        amountForAccrual = chargeAmount.subtract(loanCharge.getAmountAccrued());
                    }
                    applicableCharges.put(loanCharge, amountForAccrual);

                }
            }
            if (loanCharge.isPenalty()) {
                submittedDatePenaltyIncome = submittedDatePenaltyIncome.add(chargeAmount);
            } else {
                submittedDateFeeIncome = submittedDateFeeIncome.add(chargeAmount);
            }

        }

        if (submittedDateFeeIncome.compareTo(BigDecimal.ZERO) == 0) {
            submittedDateFeeIncome = null;
        }

        if (submittedDatePenaltyIncome.compareTo(BigDecimal.ZERO) == 0) {
            submittedDatePenaltyIncome = null;
        }

        accrualData.updateChargeDetails(applicableCharges, submittedDateFeeIncome, submittedDatePenaltyIncome);
    }

    private void updateChargeForDueDate(Collection<LoanChargeData> chargesData, LoanScheduleAccrualData accrualData, LocalDate startDate,
            LocalDate endDate) {
        final Map<LoanChargeData, BigDecimal> applicableCharges = new HashMap<>();
        BigDecimal dueDateFeeIncome = BigDecimal.ZERO;
        BigDecimal dueDatePenaltyIncome = BigDecimal.ZERO;
        for (LoanChargeData loanCharge : chargesData) {
            BigDecimal chargeAmount = BigDecimal.ZERO;
            if (loanCharge.getDueDate() == null) {
                if (loanCharge.isInstallmentFee() && DateUtils.isEqual(endDate, accrualData.getDueDateAsLocaldate())) {
                    Collection<LoanInstallmentChargeData> installmentData = loanCharge.getInstallmentChargeData();
                    for (LoanInstallmentChargeData installmentChargeData : installmentData) {

                        if (installmentChargeData.getInstallmentNumber().equals(accrualData.getInstallmentNumber())) {
                            BigDecimal accruableForInstallment = installmentChargeData.getAmount();
                            if (installmentChargeData.getAmountUnrecognized() != null) {
                                accruableForInstallment = accruableForInstallment.subtract(installmentChargeData.getAmountUnrecognized());
                            }
                            chargeAmount = accruableForInstallment;
                            boolean canAddCharge = chargeAmount.compareTo(BigDecimal.ZERO) > 0;
                            if (canAddCharge && (installmentChargeData.getAmountAccrued() == null
                                    || chargeAmount.compareTo(installmentChargeData.getAmountAccrued()) != 0)) {
                                BigDecimal amountForAccrual = chargeAmount;
                                if (installmentChargeData.getAmountAccrued() != null) {
                                    amountForAccrual = chargeAmount.subtract(installmentChargeData.getAmountAccrued());
                                }
                                applicableCharges.put(loanCharge, amountForAccrual);
                                BigDecimal amountAccrued = chargeAmount;
                                if (loanCharge.getAmountAccrued() != null) {
                                    amountAccrued = amountAccrued.add(loanCharge.getAmountAccrued());
                                }
                                loanCharge.updateAmountAccrued(amountAccrued);
                            }
                            break;
                        }
                    }
                }
            } else if (((accrualData.getInstallmentNumber() == 1 && DateUtils.isEqual(loanCharge.getDueDate(), startDate))
                    || DateUtils.isAfter(loanCharge.getDueDate(), startDate)) && !DateUtils.isAfter(loanCharge.getDueDate(), endDate)) {
                chargeAmount = loanCharge.getAmount();
                if (loanCharge.getAmountUnrecognized() != null) {
                    chargeAmount = chargeAmount.subtract(loanCharge.getAmountUnrecognized());
                }
                boolean canAddCharge = chargeAmount.compareTo(BigDecimal.ZERO) > 0;
                if (canAddCharge && (loanCharge.getAmountAccrued() == null || chargeAmount.compareTo(loanCharge.getAmountAccrued()) != 0)) {
                    BigDecimal amountForAccrual = chargeAmount;
                    if (loanCharge.getAmountAccrued() != null) {
                        amountForAccrual = chargeAmount.subtract(loanCharge.getAmountAccrued());
                    }
                    applicableCharges.put(loanCharge, amountForAccrual);
                }
            }

            if (loanCharge.isPenalty()) {
                dueDatePenaltyIncome = dueDatePenaltyIncome.add(chargeAmount);
            } else {
                dueDateFeeIncome = dueDateFeeIncome.add(chargeAmount);
            }
        }

        if (dueDateFeeIncome.compareTo(BigDecimal.ZERO) == 0) {
            dueDateFeeIncome = null;
        }

        if (dueDatePenaltyIncome.compareTo(BigDecimal.ZERO) == 0) {
            dueDatePenaltyIncome = null;
        }

        accrualData.updateChargeDetails(applicableCharges, dueDateFeeIncome, dueDatePenaltyIncome);
    }

    private void updateInterestIncome(final LoanScheduleAccrualData accrualData,
            final Collection<LoanTransactionData> loanWaiverTransactions,
            final Collection<LoanSchedulePeriodData> loanSchedulePeriodDataList, final LocalDate tillDate) {

        BigDecimal interestIncome = BigDecimal.ZERO;
        if (accrualData.getInterestIncome() != null) {
            interestIncome = accrualData.getInterestIncome();
        }
        if (accrualData.getWaivedInterestIncome() != null) {
            BigDecimal recognized = BigDecimal.ZERO;
            BigDecimal unrecognized = BigDecimal.ZERO;
            BigDecimal remainingAmt = BigDecimal.ZERO;
            Collection<LoanTransactionData> loanTransactionDatas = new ArrayList<>();

            for (LoanTransactionData loanTransactionData : loanWaiverTransactions) {
                LocalDate transactionDate = loanTransactionData.getDate();
                if (!DateUtils.isAfter(transactionDate, accrualData.getFromDateAsLocaldate())
                        || (DateUtils.isAfter(transactionDate, accrualData.getFromDateAsLocaldate())
                                && !DateUtils.isAfter(transactionDate, accrualData.getDueDateAsLocaldate())
                                && !DateUtils.isAfter(transactionDate, tillDate))) {
                    loanTransactionDatas.add(loanTransactionData);
                }
            }

            Iterator<LoanTransactionData> iterator = loanTransactionDatas.iterator();
            for (LoanSchedulePeriodData loanSchedulePeriodData : loanSchedulePeriodDataList) {
                if (MathUtil.isLessThanOrEqualZero(recognized) && MathUtil.isLessThanOrEqualZero(unrecognized) && iterator.hasNext()) {
                    LoanTransactionData loanTransactionData = iterator.next();
                    recognized = recognized.add(loanTransactionData.getInterestPortion());
                    unrecognized = unrecognized.add(loanTransactionData.getUnrecognizedIncomePortion());
                }
                if (DateUtils.isBefore(loanSchedulePeriodData.getDueDate(), accrualData.getDueDateAsLocaldate())) {
                    remainingAmt = remainingAmt.add(loanSchedulePeriodData.getInterestWaived());
                    if (recognized.compareTo(remainingAmt) > 0) {
                        recognized = recognized.subtract(remainingAmt);
                        remainingAmt = BigDecimal.ZERO;
                    } else {
                        remainingAmt = remainingAmt.subtract(recognized);
                        recognized = BigDecimal.ZERO;
                        if (unrecognized.compareTo(remainingAmt) >= 0) {
                            unrecognized = unrecognized.subtract(remainingAmt);
                            remainingAmt = BigDecimal.ZERO;
                        } else if (iterator.hasNext()) {
                            remainingAmt = remainingAmt.subtract(unrecognized);
                            unrecognized = BigDecimal.ZERO;
                        }
                    }

                }
            }

            BigDecimal interestWaived = accrualData.getWaivedInterestIncome();
            if (interestWaived.compareTo(recognized) > 0) {
                interestIncome = interestIncome.subtract(interestWaived.subtract(recognized));
            }
        }

        accrualData.updateAccruableIncome(interestIncome);
    }

    @Override
    @Transactional
    public void addIncomeAndAccrualTransactions(Long loanId) throws LoanNotFoundException {
        if (loanId != null) {
            Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            if (loan == null) {
                throw new LoanNotFoundException(loanId);
            }
            final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
            final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());
            loan.processIncomeTransactions();
            this.loanRepositoryWrapper.saveAndFlush(loan);
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        }
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        final MonetaryCurrency currency = loan.getCurrency();
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.getCode(), existingTransactionIds,
                existingReversedTransactionIds, isAccountTransfer);
        journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }
}
