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
package org.apache.fineract.portfolio.loanaccount.payoffpreview.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanRepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleModelData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.ProgressiveLoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffScheduleData;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffScheduleInstallmentData;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffSchedulePreviewParameters;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.validator.PayoffSchedulePreviewParseAndValidator;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.ILoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PayoffSchedulePreviewServiceImpl implements PayoffSchedulePreviewService {

    private static final String VALIDATION_CODE_PREFIX = "validation.msg.payoffSchedulePreview.";
    private static final int INITIAL_RATE_FACTOR_PERIODS = 16;

    private final PayoffSchedulePreviewParseAndValidator validator;
    private final ProgressiveLoanScheduleGenerator progressiveLoanScheduleGenerator;
    private final EMICalculator emiCalculator;
    private final ConfigurationDomainService configurationDomainService;

    @Override
    public PayoffScheduleData generatePreview(final String apiRequestBodyAsJson) {
        final PayoffSchedulePreviewParameters parameters = validator.validateAndParse(apiRequestBodyAsJson);
        final MathContext mc = MoneyHelper.getMathContext();
        final List<PayoffScheduleInstallmentData> installments = parameters.isTermMode() ? calculateTermSchedule(parameters, mc)
                : calculateFixedPaymentSchedule(parameters, mc);

        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalRepayment = BigDecimal.ZERO;
        for (final PayoffScheduleInstallmentData installment : installments) {
            totalPrincipal = totalPrincipal.add(installment.getPrincipalDue());
            totalInterest = totalInterest.add(installment.getInterestDue());
            totalRepayment = totalRepayment.add(installment.getTotalDue());
        }
        return PayoffScheduleData.builder() //
                .currencyCode(parameters.currency().getCode()) //
                .amount(parameters.amount()) //
                .annualNominalInterestRate(parameters.annualNominalInterestRate()) //
                .interestMethod(InterestMethod.DECLINING_BALANCE.name()) //
                .term(parameters.isTermMode() ? parameters.term().iso() : null) //
                .frequency(parameters.frequency().iso()) //
                .startDate(parameters.startDate()) //
                .numberOfInstallments(installments.size()) //
                .totalPrincipal(totalPrincipal) //
                .totalInterest(totalInterest) //
                .totalRepayment(totalRepayment) //
                .installments(installments) //
                .build();
    }

    private List<PayoffScheduleInstallmentData> calculateTermSchedule(final PayoffSchedulePreviewParameters parameters,
            final MathContext mc) {
        final int numberOfRepayments = parameters.term().value() / parameters.frequency().value();
        final int maxTermPeriods = requireConfiguredMaxPeriods(GlobalConfigurationConstants.PAYOFF_SCHEDULE_PREVIEW_MAX_TERM_PERIODS,
                configurationDomainService.retrievePayoffSchedulePreviewMaxTermPeriods());
        if (numberOfRepayments > maxTermPeriods) {
            throw validationError("term", parameters.term().iso(), "exceeds.max.term.periods", "The term results in " + numberOfRepayments
                    + " installments, which exceeds the maximum of " + maxTermPeriods + " allowed for a payoff schedule preview.");
        }

        final LoanSchedulePlan plan = progressiveLoanScheduleGenerator.generate(mc, toModelData(parameters, numberOfRepayments));
        return plan.getPeriods().stream() //
                .filter(LoanSchedulePlanRepaymentPeriod.class::isInstance) //
                .map(LoanSchedulePlanRepaymentPeriod.class::cast) //
                .map(period -> PayoffScheduleInstallmentData.builder() //
                        .installmentNumber(period.getPeriodNumber()) //
                        .fromDate(period.getPeriodFromDate()) //
                        .dueDate(period.getPeriodDueDate()) //
                        .principalDue(period.getPrincipalAmount()) //
                        .interestDue(period.getInterestAmount()) //
                        .totalDue(period.getTotalDueAmount()) //
                        .outstandingPrincipalBalance(period.getOutstandingLoanBalance()) //
                        .build()) //
                .toList();
    }

    /**
     * The progressive engine cannot amortize towards a given installment amount, so the balance is run down here. The
     * interest of every period still comes from the engine: rate factors are taken from an interest schedule model over
     * the same dates, seeded at the start date exactly like a term schedule, and applied to the outstanding balance the
     * way the engine applies them for declining-balance interest.
     */
    private List<PayoffScheduleInstallmentData> calculateFixedPaymentSchedule(final PayoffSchedulePreviewParameters parameters,
            final MathContext mc) {
        final int maxPayoffPeriods = requireConfiguredMaxPeriods(GlobalConfigurationConstants.PAYOFF_SCHEDULE_PREVIEW_MAX_PAYOFF_PERIODS,
                configurationDomainService.retrievePayoffSchedulePreviewMaxPayoffPeriods());
        final CurrencyData currency = parameters.currency();
        final ILoanConfigurationDetails configuration = LoanApplicationTerms.assembleFrom(toModelData(parameters, 1), mc)
                .toLoanConfigurationDetails();
        final Money fixedPayment = Money.of(currency, parameters.fixedPaymentAmount(), mc);

        final List<PayoffScheduleInstallmentData> installments = new ArrayList<>();
        List<RepaymentPeriod> ratePeriods = List.of();
        Money outstandingBalance = Money.of(currency, parameters.amount(), mc);
        int installmentNumber = 0;
        while (outstandingBalance.isGreaterThanZero()) {
            installmentNumber++;
            if (installmentNumber > maxPayoffPeriods) {
                throw validationError("fixedPaymentAmount", parameters.fixedPaymentAmount(), "exceeds.max.payoff.periods",
                        "The fixedPaymentAmount does not pay off the amount within the maximum of " + maxPayoffPeriods
                                + " installments allowed for a payoff schedule preview.");
            }
            if (installmentNumber > ratePeriods.size()) {
                // Grown geometrically so short schedules stay cheap while long ones are rebuilt only a few times.
                final int size = Math.min(maxPayoffPeriods, Math.max(INITIAL_RATE_FACTOR_PERIODS, ratePeriods.size() * 2));
                ratePeriods = calculateRateFactorPeriods(parameters, configuration, size, mc);
            }

            final RepaymentPeriod period = ratePeriods.get(installmentNumber - 1);
            final BigDecimal rateFactor = period.getInterestPeriods().getFirst().getRateFactorTillPeriodDueDate();
            final Money interestDue = Money.of(currency, outstandingBalance.getAmount().multiply(rateFactor, mc), mc);
            if (!fixedPayment.isGreaterThan(interestDue)) {
                throw validationError("fixedPaymentAmount", parameters.fixedPaymentAmount(), "does.not.cover.interest",
                        "The fixedPaymentAmount " + fixedPayment.getAmount() + " does not cover the interest of " + interestDue.getAmount()
                                + " due in installment " + installmentNumber + ", so the outstanding balance would not decrease.");
            }

            final Money remainder = fixedPayment.minus(interestDue, mc);
            final Money principalDue = remainder.isGreaterThan(outstandingBalance) ? outstandingBalance : remainder;
            outstandingBalance = outstandingBalance.minus(principalDue, mc);
            installments.add(PayoffScheduleInstallmentData.builder() //
                    .installmentNumber(installmentNumber) //
                    .fromDate(period.getFromDate()) //
                    .dueDate(period.getDueDate()) //
                    .principalDue(principalDue.getAmount()) //
                    .interestDue(interestDue.getAmount()) //
                    .totalDue(principalDue.plus(interestDue, mc).getAmount()) //
                    .outstandingPrincipalBalance(outstandingBalance.getAmount()) //
                    .build());
        }
        return installments;
    }

    private List<RepaymentPeriod> calculateRateFactorPeriods(final PayoffSchedulePreviewParameters parameters,
            final ILoanConfigurationDetails configuration, final int numberOfPeriods, final MathContext mc) {
        final List<LoanScheduleModelRepaymentPeriod> periods = new ArrayList<>(numberOfPeriods);
        LocalDate fromDate = parameters.startDate();
        for (int periodNumber = 1; periodNumber <= numberOfPeriods; periodNumber++) {
            final LocalDate dueDate = parameters.frequency().addTo(parameters.startDate(), periodNumber);
            periods.add(LoanScheduleModelRepaymentPeriod.repayment(periodNumber, fromDate, dueDate, null, null, null, null, null, null,
                    false, mc));
            fromDate = dueDate;
        }
        final ProgressiveLoanInterestScheduleModel model = emiCalculator.generatePeriodInterestScheduleModel(periods, configuration, null,
                mc);
        model.repaymentPeriods().forEach(repaymentPeriod -> emiCalculator.calculateRateFactorForRepaymentPeriod(repaymentPeriod, model));
        return model.repaymentPeriods();
    }

    // The limits are seeded by Liquibase, so a missing value is a configuration error rather than something to default.
    private static int requireConfiguredMaxPeriods(final String configurationName, final Long configuredMaxPeriods) {
        if (configuredMaxPeriods == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.configuration." + configurationName + ".value.is.not.set",
                    "Global configuration '" + configurationName + "' has no value", configurationName);
        }
        return (int) Math.min(configuredMaxPeriods, Integer.MAX_VALUE);
    }

    private LoanRepaymentScheduleModelData toModelData(final PayoffSchedulePreviewParameters parameters, final int numberOfRepayments) {
        return new LoanRepaymentScheduleModelData(parameters.startDate(), parameters.currency(), parameters.amount(),
                parameters.startDate(), numberOfRepayments, parameters.frequency().value(), parameters.frequency().unit().name(),
                parameters.annualNominalInterestRate(), false, parameters.daysInMonthType(), parameters.daysInYearType(), null, null, null,
                false, null, InterestMethod.DECLINING_BALANCE, true, false, null);
    }

    private PlatformApiDataValidationException validationError(final String parameterName, final Object value, final String code,
            final String message) {
        return new PlatformApiDataValidationException(List
                .of(ApiParameterError.parameterError(VALIDATION_CODE_PREFIX + parameterName + "." + code, message, parameterName, value)));
    }
}
