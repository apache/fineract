package org.mifosplatform.portfolio.savingsaccount.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;

public interface DepositScheduleDateGenerator {
	
    List<LocalDate> generate(LocalDate startDate, Integer paymentPeriods, Integer depositFrequency, SavingFrequencyType savingFrequencyType);

}

