package org.mifosplatform.organisation.workingdays.service;

import org.joda.time.LocalDate;

public interface WorkingDaysWritePlatformService {
    
    boolean isWorkingDay(LocalDate transactionDate);
    
    boolean isTransactionAllowedOnNonWorkingDay();
}
