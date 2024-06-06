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
package org.apache.fineract.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanScheduleValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoanScheduleCalculationPlatformServiceImpl implements LoanScheduleCalculationPlatformService {

    private final LoanScheduleValidator fromApiJsonDeserializer;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanApplicationValidator loanApiJsonDeserializer;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final LoanUtilService loanUtilService;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;
    private final LoanSummaryWrapper loanSummaryWrapper;

    @Override
    public LoanScheduleModel calculateLoanSchedule(final JsonQuery query, Boolean validateParams) {

        /***
         * TODO: Vishwas, this is probably not required, test and remove the same
         **/

        if (validateParams) {
            this.loanApiJsonDeserializer.validateForCreate(query);
        }
        this.fromApiJsonDeserializer.validate(query.json());

        return this.loanScheduleAssembler.assembleLoanScheduleFrom(query.parsedJson());
    }

    @Override
    public void updateFutureSchedule(LoanScheduleData loanScheduleData, final Long loanId) {

        final Loan loan = fetchLoan(loanId);

        LocalDate today = DateUtils.getBusinessLocalDate();
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(loan.transactionProcessingStrategy());

        if (!loan.repaymentScheduleDetail().isInterestRecalculationEnabled() || loan.isNpa() || loan.isChargedOff()
                || !loan.getStatus().isActive()
                || !loanRepaymentScheduleTransactionProcessor.isInterestFirstRepaymentScheduleTransactionProcessor()) {
            return;
        }

        if (loan.loanProduct().isMultiDisburseLoan()) {
            BigDecimal disbursedAmount = loan.getDisbursedAmount();
            BigDecimal principalRepaid = loan.getLoanSummary().getTotalPrincipalRepaid();
            BigDecimal principalWrittenOff = loan.getLoanSummary().getTotalPrincipalWrittenOff();
            if (disbursedAmount.subtract(principalWrittenOff).subtract(principalRepaid).compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
        }
        MonetaryCurrency currency = loan.getCurrency();
        Money totalPrincipal = Money.zero(currency);
        final List<LoanSchedulePeriodData> futureInstallments = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            if (currentInstallment.isNotFullyPaidOff()) {
                if (!DateUtils.isAfter(currentInstallment.getDueDate(), today)) {
                    totalPrincipal = totalPrincipal.plus(currentInstallment.getPrincipalOutstanding(currency));
                }
            }
        }
        LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(loan);
        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = this.loanScheduleAssembler.calculatePrepaymentAmount(currency,
                today, loanApplicationTerms, loan, loan.getOfficeId(), loanRepaymentScheduleTransactionProcessor);
        Money totalAmount = totalPrincipal.plus(loanRepaymentScheduleInstallment.getFeeChargesOutstanding(currency))
                .plus(loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency));
        Money interestDue = Money.zero(currency);
        if (loanRepaymentScheduleInstallment.isInterestDue(currency)) {
            interestDue = loanRepaymentScheduleInstallment.getInterestOutstanding(currency);
            totalAmount = totalAmount.plus(interestDue);
        }
        boolean isNewPaymentRequired = loanRepaymentScheduleInstallment.isInterestDue(currency) || totalPrincipal.isGreaterThanZero();

        LoanScheduleModel model = this.loanScheduleAssembler.assembleForInterestRecalculation(loanApplicationTerms, loan.getOfficeId(),
                loan, loanRepaymentScheduleTransactionProcessor, loan.fetchInterestRecalculateFromDate());
        LoanScheduleData scheduleDate = model.toData();
        Collection<LoanSchedulePeriodData> periodDatas = scheduleDate.getPeriods();
        for (LoanSchedulePeriodData periodData : periodDatas) {
            if (isNewPaymentRequired && !DateUtils.isBefore(periodData.getDueDate(), today)) {
                LoanSchedulePeriodData loanSchedulePeriodData = LoanSchedulePeriodData.repaymentOnlyPeriod(periodData.getPeriod(),
                        periodData.getFromDate(), periodData.getDueDate(), totalPrincipal.getAmount(),
                        periodData.getPrincipalLoanBalanceOutstanding(), interestDue.getAmount(),
                        loanRepaymentScheduleInstallment.getFeeChargesCharged(currency).getAmount(),
                        loanRepaymentScheduleInstallment.getPenaltyChargesCharged(currency).getAmount(), totalAmount.getAmount(),
                        totalPrincipal.plus(interestDue).getAmount());
                futureInstallments.add(loanSchedulePeriodData);
                isNewPaymentRequired = false;
            } else if (DateUtils.isAfter(periodData.getDueDate(), today)) {
                futureInstallments.add(periodData);
            }

        }
        loanScheduleData.updateFuturePeriods(futureInstallments);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanScheduleData generateLoanScheduleForVariableInstallmentRequest(Long loanId, final String json) {
        final Loan loan = fetchLoan(loanId);
        this.loanScheduleAssembler.assempleVariableScheduleFrom(loan, json);
        return constructLoanScheduleData(loan);
    }

    private LoanScheduleData constructLoanScheduleData(Loan loan) {
        Collection<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        final List<LoanSchedulePeriodData> installmentData = new ArrayList<>();
        final MonetaryCurrency currency = loan.getCurrency();
        Money outstanding = loan.getPrincipal();

        List<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();
        if (loan.isMultiDisburmentLoan()) {
            disbursementDetails = loan.getDisbursementDetails();
            outstanding = outstanding.zero();
        }
        Money principal = outstanding;
        Iterator<LoanDisbursementDetails> disbursementItr = disbursementDetails.iterator();
        LoanDisbursementDetails loanDisbursementDetails = null;
        if (disbursementItr.hasNext()) {
            loanDisbursementDetails = disbursementItr.next();
        }

        Money totalInterest = principal.zero();
        Money totalCharge = principal.zero();
        Money totalPenalty = principal.zero();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (loanDisbursementDetails != null
                    && !DateUtils.isAfter(loanDisbursementDetails.expectedDisbursementDateAsLocalDate(), installment.getDueDate())) {
                outstanding = outstanding.plus(loanDisbursementDetails.principal());
                principal = principal.plus(loanDisbursementDetails.principal());
                if (disbursementItr.hasNext()) {
                    loanDisbursementDetails = disbursementItr.next();
                } else {
                    loanDisbursementDetails = null;
                }
            }
            outstanding = outstanding.minus(installment.getPrincipal(currency));
            LoanSchedulePeriodData loanSchedulePeriodData = LoanSchedulePeriodData.repaymentOnlyPeriod(installment.getInstallmentNumber(),
                    installment.getFromDate(), installment.getDueDate(), installment.getPrincipal(currency).getAmount(),
                    outstanding.getAmount(), installment.getInterestCharged(currency).getAmount(),
                    installment.getFeeChargesCharged(currency).getAmount(), installment.getPenaltyChargesCharged(currency).getAmount(),
                    installment.getDue(currency).getAmount(), installment.getTotalPrincipalAndInterest(currency).getAmount());
            installmentData.add(loanSchedulePeriodData);
            totalInterest = totalInterest.plus(installment.getInterestCharged(currency));
            totalCharge = totalCharge.plus(installment.getFeeChargesCharged(currency));
            totalPenalty = totalPenalty.plus(installment.getPenaltyChargesCharged(currency));
        }

        CurrencyData currencyData = this.currencyReadPlatformService.retrieveCurrency(currency.getCode());

        return new LoanScheduleData(currencyData, installmentData, loan.getLoanRepaymentScheduleDetail().getNumberOfRepayments(),
                principal.getAmount(), principal.getAmount(), totalInterest.getAmount(), totalCharge.getAmount(), totalPenalty.getAmount(),
                principal.plus(totalCharge).plus(totalInterest).plus(totalPenalty).getAmount());
    }

    private LoanApplicationTerms constructLoanApplicationTerms(final Loan loan) {
        final LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        return loan.constructLoanApplicationTerms(scheduleGeneratorDTO);
    }

    private Loan fetchLoan(final Long accountId) {
        final Loan loanAccount = this.loanRepository.findOneWithNotFoundDetection(accountId, true);
        loanAccount.setHelpers(defaultLoanLifecycleStateMachine, this.loanSummaryWrapper,
                this.loanRepaymentScheduleTransactionProcessorFactory);

        return loanAccount;
    }

}
