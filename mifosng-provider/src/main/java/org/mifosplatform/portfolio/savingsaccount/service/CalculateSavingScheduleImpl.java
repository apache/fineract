package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.portfolio.savingsaccount.command.CalculateSavingScheduleCommand;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingSchedulePeriodData;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRepository;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingProductNotFoundException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
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
        SavingScheduleData savingScheduleData = null;

        // FIXME Validations should write here
        final SavingProduct savingProduct = this.savingProductRepository.findOne(command.getProductId());
        if (savingProduct == null) throw new SavingProductNotFoundException(command.getProductId());

        final Integer depositFrequency = command.getDepositEvery();
        final SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(command.getPaymentFrequencyType());
        final LocalDate scheduleStartDate = command.getPaymentsStartingFromDate();
        final BigDecimal interestRate = command.getInterestRate();
        final BigDecimal depositAmountPerPeriod = command.getDeposit();
        final Integer tenure = command.getTenure();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(savingProduct.getCurrency()
                .getCode());
        
        SavingProductType savingProductType = SavingProductType.fromInt(savingProduct.getSavingProductRelatedDetail().getSavingProductType());
        TenureTypeEnum tenureType = TenureTypeEnum.fromInt(command.getTenureType());
        SavingInterestCalculationMethod interestCalculationMethod = SavingInterestCalculationMethod.fromInt(command.getInterestCalculationMethod());
        if (savingProductType.isReccuring() && tenureType.isFixedPeriod()) {
			savingScheduleData = savingScheduleGenerator.generate(scheduleStartDate, depositAmountPerPeriod,
	                depositFrequency, savingFrequencyType, savingProduct, tenure, applicationCurrency,interestRate,interestCalculationMethod);
		} else {
			final Collection<SavingSchedulePeriodData> periods = new ArrayList<SavingSchedulePeriodData>();
			 CurrencyData currencyData = new CurrencyData(applicationCurrency.getCode(), applicationCurrency.getName(),
		                savingProduct.getCurrency().getDigitsAfterDecimal(), applicationCurrency.getDisplaySymbol(), applicationCurrency.getNameCode());
			SavingSchedulePeriodData installment = SavingSchedulePeriodData.addScheduleInformation(1, new LocalDate(command.getPaymentsStartingFromDate()).plusMonths(depositFrequency), command.getDeposit(), BigDecimal.ZERO);
			periods.add(installment);
			savingScheduleData = new SavingScheduleData(currencyData, command.getDeposit(), BigDecimal.ZERO, BigDecimal.ZERO, periods);	
		}
         

        return savingScheduleData;
    }

}
