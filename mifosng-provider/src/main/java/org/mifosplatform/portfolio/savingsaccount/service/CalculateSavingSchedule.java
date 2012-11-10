package org.mifosplatform.portfolio.savingsaccount.service;

import org.mifosplatform.portfolio.savingsaccount.command.CalculateSavingScheduleCommand;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;

public interface CalculateSavingSchedule {
    SavingScheduleData calculateSavingSchedule(CalculateSavingScheduleCommand command);
}

