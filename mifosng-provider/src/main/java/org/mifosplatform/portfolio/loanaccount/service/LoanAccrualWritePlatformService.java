package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;


public interface LoanAccrualWritePlatformService {

    void addAccrualAccounting() throws JobExecutionException;

}
