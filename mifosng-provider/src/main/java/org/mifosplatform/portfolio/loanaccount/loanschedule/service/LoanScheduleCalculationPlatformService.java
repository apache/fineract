package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;

public interface LoanScheduleCalculationPlatformService {

    LoanScheduleData calculateLoanSchedule(JsonQuery query);
}
