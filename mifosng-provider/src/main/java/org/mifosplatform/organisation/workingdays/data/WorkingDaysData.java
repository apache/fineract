/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.data;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

import java.util.Collection;

public class WorkingDaysData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String recurrence;

    @SuppressWarnings("unused")
    private final EnumOptionData repaymentRescheduleType;

    // template date
    @SuppressWarnings("unused")
    private final Collection<EnumOptionData> repaymentRescheduleOptions;

    public WorkingDaysData(Long id, String recurrence, EnumOptionData repaymentRescheduleType) {
        this.id = id;
        this.recurrence = recurrence;
        this.repaymentRescheduleType = repaymentRescheduleType;
        this.repaymentRescheduleOptions = null;
    }

    public WorkingDaysData(Long id, String recurrence, EnumOptionData repaymentRescheduleType,
            Collection<EnumOptionData> repaymentRescheduleOptions) {
        this.id = id;
        this.recurrence = recurrence;
        this.repaymentRescheduleType = repaymentRescheduleType;
        this.repaymentRescheduleOptions = repaymentRescheduleOptions;
    }
}
