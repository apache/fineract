/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanaccount.data.LoanScheduleAccrualData;

public interface LoanAccrualWritePlatformService {

    void addAccrualAccounting(Long loanId, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas, StringBuilder sb);

    void addPeriodicAccruals(LocalDate tilldate, Long loanId, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas, StringBuilder sb);

}
