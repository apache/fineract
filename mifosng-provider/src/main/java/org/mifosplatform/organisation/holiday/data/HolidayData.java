/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.data;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class HolidayData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final LocalDate fromDate;
    @SuppressWarnings("unused")
    private final LocalDate toDate;
    @SuppressWarnings("unused")
    private final LocalDate repaymentsRescheduledTo;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final EnumOptionData status;

    public HolidayData(final Long id, final String name, final String description, final LocalDate fromDate, final LocalDate toDate,
            final LocalDate repaymentsRescheduledTo, final EnumOptionData status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.repaymentsRescheduledTo = repaymentsRescheduledTo;
        this.officeId = null;
        this.status = status;
    }
}
