/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;

public interface LoanScheduleCalculationPlatformService {

    LoanScheduleModel calculateLoanSchedule(JsonQuery query, Boolean validateParams);

    void updateFutureSchedule(LoanScheduleData loanScheduleData, Long loanId);
}
