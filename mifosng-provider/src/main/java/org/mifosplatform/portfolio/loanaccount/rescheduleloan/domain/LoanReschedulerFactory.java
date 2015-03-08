/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain;

import java.math.MathContext;

import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.loanaccount.data.HolidayDetailDTO;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;

public interface LoanReschedulerFactory {

    public LoanRescheduleModel reschedule(final MathContext mathContext, final InterestMethod interestMethod,
            final LoanRescheduleRequest loanRescheduleRequest, final ApplicationCurrency applicationCurrency,
            final HolidayDetailDTO holidayDetailDTO, CalendarInstance restCalendarInstance);
}
