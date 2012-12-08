package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingSchedulePeriodData;
import org.mifosplatform.portfolio.savingsaccount.domain.DepositScheduleDateGenerator;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;

public class SavingScheduleGenerator {

    private final DepositScheduleDateGenerator scheduledDateGenerator = new DefaultDepositScheduleDateGenerator();

    public SavingScheduleData generate(final LocalDate scheduleStartDate, final BigDecimal depositAmountPerPeriod,
            final Integer depositFrequency, final PeriodFrequencyType depositFrequencyType, final SavingProduct savingProduct,
            final Integer tenure, final ApplicationCurrency applicationCurrency) {

        LocalDate startDate = scheduleStartDate;
        int periodNumber = 1;

        Integer paymentPeriods = tenure / depositFrequency;
        final Collection<SavingSchedulePeriodData> periods = new ArrayList<SavingSchedulePeriodData>();
        final List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(startDate, paymentPeriods, depositFrequency,
                depositFrequencyType);
        final MonetaryCurrency currency = savingProduct.getCurrency();
        final Money depositAmount = Money.of(currency, depositAmountPerPeriod);
        Money totalDeposit = Money.zero(currency);

        for (LocalDate scheduleDate : scheduledDates) {
            totalDeposit = totalDeposit.plus(depositAmount);
            SavingSchedulePeriodData installment = SavingSchedulePeriodData.addScheduleInformation(periodNumber, scheduleDate,
                    depositAmount.getAmount());
            periods.add(installment);
            periodNumber++;
        }
        CurrencyData currencyData = new CurrencyData(applicationCurrency.getCode(), applicationCurrency.getName(),
                currency.getDigitsAfterDecimal(), applicationCurrency.getDisplaySymbol(), applicationCurrency.getNameCode());

        return new SavingScheduleData(currencyData, totalDeposit.getAmount(), periods);
    }


}
