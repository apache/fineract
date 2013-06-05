/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.service.LoanChargeAssembler;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
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
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final AprCalculator aprCalculator;

    @Autowired
    public LoanScheduleAssembler(final FromJsonHelper fromApiJsonHelper, final LoanProductRepository loanProductRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final LoanScheduleGeneratorFactory loanScheduleFactory, final AprCalculator aprCalculator,
            final LoanChargeAssembler loanChargeAssembler) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.loanProductRepository = loanProductRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.loanScheduleFactory = loanScheduleFactory;
        this.aprCalculator = aprCalculator;
        this.loanChargeAssembler = loanChargeAssembler;
    }

    public LoanApplicationTerms assembleLoanTerms(final JsonElement element) {
        final Long loanProductId = fromApiJsonHelper.extractLongNamed("productId", element);

        final LoanProduct loanProduct = this.loanProductRepository.findOne(loanProductId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(loanProductId); }

        return assembleLoanApplicationTermsFrom(element, loanProduct);
    }

    private LoanApplicationTerms assembleLoanApplicationTermsFrom(final JsonElement element, final LoanProduct loanProduct) {

        final MonetaryCurrency currency = loanProduct.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        // loan terms
        final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequency", element);
        final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("loanTermFrequencyType", element);
        final PeriodFrequencyType loanTermPeriodFrequencyType = PeriodFrequencyType.fromInt(loanTermFrequencyType);

        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        final Integer repaymentFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentFrequencyType", element);
        final PeriodFrequencyType repaymentPeriodFrequencyType = PeriodFrequencyType.fromInt(repaymentFrequencyType);

        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed("amortizationType", element);
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(amortizationType);

        // interest terms
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        final InterestMethod interestMethod = InterestMethod.fromInt(interestType);

        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationPeriodType",
                element);
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod
                .fromInt(interestCalculationPeriodType);

        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProduct.getInterestPeriodFrequencyType();

        final BigDecimal annualNominalInterestRate = this.aprCalculator.calculateFrom(interestRatePeriodFrequencyType,
                interestRatePerPeriod);

        // disbursement details
        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final Money principalMoney = Money.of(currency, principal);

        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed("expectedDisbursementDate", element);
        final LocalDate repaymentsStartingFromDate = this.fromApiJsonHelper.extractLocalDateNamed("repaymentsStartingFromDate", element);

        // grace details
        final Integer graceOnPrincipalPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnPrincipalPayment", element);
        final Integer graceOnInterestPayment = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestPayment", element);
        final Integer graceOnInterestCharged = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("graceOnInterestCharged", element);
        final LocalDate interestChargedFromDate = fromApiJsonHelper.extractLocalDateNamed("interestChargedFromDate", element);

        // other
        final BigDecimal inArrearsTolerance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        final Money inArrearsToleranceMoney = Money.of(currency, inArrearsTolerance);

        return LoanApplicationTerms.assembleFrom(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod, principalMoney,
                expectedDisbursementDate, repaymentsStartingFromDate, graceOnPrincipalPayment, graceOnInterestPayment,
                graceOnInterestCharged, interestChargedFromDate, inArrearsToleranceMoney);
    }

    public LoanProductRelatedDetail assembleLoanProductRelatedDetail(final JsonElement element) {
        LoanApplicationTerms loanApplicationTerms = assembleLoanTerms(element);
        return loanApplicationTerms.toLoanProductRelatedDetail();
    }

    public LoanScheduleModel assembleLoanScheduleFrom(final JsonElement element) {

        final LoanApplicationTerms loanApplicationTerms = assembleLoanTerms(element);

        return assembleLoanScheduleFrom(loanApplicationTerms, element);
    }

    public LoanScheduleModel assembleLoanScheduleFrom(final LoanApplicationTerms loanApplicationTerms, final JsonElement element) {

        final Set<LoanCharge> loanCharges = this.loanChargeAssembler.fromParsedJson(element);

        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getInterestMethod());

        final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        final MathContext mc = new MathContext(8, roundingMode);

        final MonetaryCurrency currency = loanApplicationTerms.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        return loanScheduleGenerator.generate(mc, applicationCurrency, loanApplicationTerms, loanCharges);
    }
}