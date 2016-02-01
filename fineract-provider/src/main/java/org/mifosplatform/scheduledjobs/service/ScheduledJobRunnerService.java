/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.scheduledjobs.service;

import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;

public interface ScheduledJobRunnerService {

    void updateLoanSummaryDetails();

    void updateLoanPaidInAdvance();

    void applyAnnualFeeForSavings();

    void applyDueChargesForSavings() throws JobExecutionException;

    void updateNPA();

    void updateMaturityDetailsOfDepositAccounts();

    void generateRDSchedule();
}
