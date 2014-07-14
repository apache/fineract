package org.mifosplatform.portfolio.loanaccount.service;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;


public interface LoanAccrualWritePlatformService {

    void addAccrualAccounting() throws JobExecutionException;

    void addPeriodicAccruals() throws JobExecutionException;

    String addPeriodicAccruals(LocalDate tilldate);

}
