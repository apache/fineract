/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.organisation.workingdays.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_working_days")
public class WorkingDays extends AbstractPersistable<Long> {

    @Column(name = "recurrence", length = 100, nullable = true)
    private String recurrence;

    @Column(name = "repayment_rescheduling_enum", nullable = false)
    private Integer repaymentReschedulingType;

    protected WorkingDays() {

    }

    protected WorkingDays(final String recurrence, final Integer repaymentReschedulingType) {
        this.recurrence = recurrence;
        this.repaymentReschedulingType = repaymentReschedulingType;
    }

    /**
     * @return the recurrence
     */
    public String getRecurrence() {
        return this.recurrence;
    }

    /**
     * @return the repaymentReschedulingType
     */
    public Integer getRepaymentReschedulingType() {
        return this.repaymentReschedulingType;
    }

}
