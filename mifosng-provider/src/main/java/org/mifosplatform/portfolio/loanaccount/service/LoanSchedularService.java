package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;


public interface LoanSchedularService {

    void applyChargeForOverdueLoans() throws JobExecutionException;

}
