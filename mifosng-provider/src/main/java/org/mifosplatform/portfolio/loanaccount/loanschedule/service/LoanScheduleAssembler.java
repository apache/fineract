package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanSchedule;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.service.LoanChargeAssembler;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class LoanScheduleAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanProductRepository loanProductRepository;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final AprCalculator aprCalculator;

    @Autowired
    public LoanScheduleAssembler(final FromJsonHelper fromApiJsonHelper, final LoanProductRepository loanProductRepository,
            final ApplicationCurrencyRepository applicationCurrencyRepository, final LoanScheduleGeneratorFactory loanScheduleFactory,
            final AprCalculator aprCalculator, final LoanChargeAssembler loanChargeAssembler) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.loanProductRepository = loanProductRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.loanScheduleFactory = loanScheduleFactory;
        this.aprCalculator = aprCalculator;
        this.loanChargeAssembler = loanChargeAssembler;
    }

    public LoanSchedule fromJson(final String json) {
        final JsonElement element = fromApiJsonHelper.parse(json);
        return fromJson(element);
    }
    
    public LoanSchedule fromJson(final JsonElement element) {
        return fromJson(element, null);
    }

    public LoanSchedule fromJson(final JsonElement element, final BigDecimal inArrearsTolerance) {
        final Long loanProductId = fromApiJsonHelper.extractLongNamed("productId", element);

        final LoanProduct loanProduct = this.loanProductRepository.findOne(loanProductId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(loanProductId); }

        final MonetaryCurrency currency = loanProduct.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(currency.getCode());

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestRateFrequencyType", element);
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);

        final InterestMethod interestMethod = InterestMethod.fromInt(interestType);
        final PeriodFrequencyType interestRatePeriodFrequencyType = PeriodFrequencyType.fromInt(interestRateFrequencyType);
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                .fromInt(interestCalculationPeriodType);

        final BigDecimal defaultAnnualNominalInterestRate = this.aprCalculator.calculateFrom(interestRatePeriodFrequencyType,
                interestRatePerPeriod);

        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        final Integer repaymentFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element);
        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element);
        final PeriodFrequencyType repaymentPeriodFrequencyType = PeriodFrequencyType.fromInt(repaymentFrequencyType);
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(amortizationType);

        final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequency", element);
        final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequencyType", element);
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(loanTermFrequencyType);

        final LocalDate expectedDisbursementDate = fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        final LocalDate repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed("repaymentsStartingFromDate", element);
        final LocalDate interestChargedFromDate = fromApiJsonHelper.extractLocalDateNamed("interestChargedFromDate", element);

        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element);

        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(interestMethod);

        return new LoanSchedule(loanScheduleGenerator, applicationCurrency, principal, interestRatePerPeriod,
                interestRatePeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod,
                repaymentEvery, repaymentPeriodFrequencyType, numberOfRepayments, amortizationMethod, loanTermFrequency,
                loanTermPeriodFrequencyType, loanCharges, expectedDisbursementDate, repaymentsStartingFromDate, interestChargedFromDate, inArrearsTolerance);
    }
}