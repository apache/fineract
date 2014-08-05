/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;


public interface LoanAccrualWritePlatformService {

    void addAccrualAccounting() throws JobExecutionException;

    void addPeriodicAccruals() throws JobExecutionException;

    String addPeriodicAccruals(LocalDate tilldate);

}
