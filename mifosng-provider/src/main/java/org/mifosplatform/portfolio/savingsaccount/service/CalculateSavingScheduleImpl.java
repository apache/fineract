package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccount.command.CalculateSavingScheduleCommand;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRepository;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculateSavingScheduleImpl implements CalculateSavingSchedule {

    private final PlatformSecurityContext context;
    private final SavingProductRepository savingProductRepository;
    private final SavingScheduleGenerator savingScheduleGenerator;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;

    @Autowired
    public CalculateSavingScheduleImpl(final PlatformSecurityContext context, final SavingProductRepository savingProductRepository,
            final ApplicationCurrencyRepository applicationCurrencyRepository) {
        this.context = context;
        this.savingProductRepository = savingProductRepository;
        this.savingScheduleGenerator = new SavingScheduleGenerator();
        this.applicationCurrencyRepository = applicationCurrencyRepository;
    }

    @Override
    public SavingScheduleData calculateSavingSchedule(CalculateSavingScheduleCommand command) {

        context.authenticatedUser();

        // FIXME Validations should write here
        final SavingProduct savingProduct = this.savingProductRepository.findOne(command.getProductId());
        if (savingProduct == null) throw new SavingProductNotFoundException(command.getProductId());

        final Integer depositFrequency = command.getDepositEvery();
        final PeriodFrequencyType depositFrequencyType = PeriodFrequencyType.fromInt(command.getPaymentFrequencyType());
        final LocalDate scheduleStartDate = command.getPaymentsStartingFromDate();
        @SuppressWarnings("unused")
        final BigDecimal interestRate = command.getInterestRate();
        final BigDecimal depositAmountPerPeriod = command.getDeposit();
        final Integer tenure = command.getTenure();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(savingProduct.getCurrency()
                .getCode());

        SavingScheduleData savingScheduleData = savingScheduleGenerator.generate(scheduleStartDate, depositAmountPerPeriod,
                depositFrequency, depositFrequencyType, savingProduct, tenure, applicationCurrency);

        return savingScheduleData;
    }

}
