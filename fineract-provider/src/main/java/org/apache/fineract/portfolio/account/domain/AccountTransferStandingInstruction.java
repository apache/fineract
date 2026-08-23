/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Getter
@Setter
@Table(name = "m_account_transfer_standing_instructions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "name" }, name = "name") })
public class AccountTransferStandingInstruction extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "account_transfer_details_id", nullable = true)
    private AccountTransferDetails accountTransferDetails;

    @Column(name = "name")
    private String name;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "instruction_type")
    private Integer instructionType;

    @Column(name = "status")
    private Integer status;

    @Column(name = "amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal amount;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_till")
    private LocalDate validTill;

    @Column(name = "recurrence_type")
    private Integer recurrenceType;

    @Column(name = "recurrence_frequency")
    private Integer recurrenceFrequency;

    @Column(name = "recurrence_interval")
    private Integer recurrenceInterval;

    @Column(name = "recurrence_on_day")
    private Integer recurrenceOnDay;

    @Column(name = "recurrence_on_month")
    private Integer recurrenceOnMonth;

    @Column(name = "last_run_date")
    private LocalDate latsRunDate;

    protected AccountTransferStandingInstruction() {

    }

    public static AccountTransferStandingInstruction create(final AccountTransferDetails accountTransferDetails, final String name,
            final Integer priority, final Integer instructionType, final Integer status, final BigDecimal amount, final LocalDate validFrom,
            final LocalDate validTill, final Integer recurrenceType, final Integer recurrenceFrequency, final Integer recurrenceInterval,
            final MonthDay recurrenceOnMonthDay) {
        Integer recurrenceOnDay = null;
        Integer recurrenceOnMonth = null;
        if (recurrenceOnMonthDay != null) {
            recurrenceOnDay = recurrenceOnMonthDay.getDayOfMonth();
            recurrenceOnMonth = recurrenceOnMonthDay.getMonthValue();
        }
        return new AccountTransferStandingInstruction(accountTransferDetails, name, priority, instructionType, status, amount, validFrom,
                validTill, recurrenceType, recurrenceFrequency, recurrenceInterval, recurrenceOnDay, recurrenceOnMonth);
    }

    private AccountTransferStandingInstruction(final AccountTransferDetails accountTransferDetails, final String name,
            final Integer priority, final Integer instructionType, final Integer status, final BigDecimal amount, final LocalDate validFrom,
            final LocalDate validTill, final Integer recurrenceType, final Integer recurrenceFrequency, final Integer recurrenceInterval,
            final Integer recurrenceOnDay, final Integer recurrenceOnMonth) {
        this.accountTransferDetails = accountTransferDetails;
        this.name = name;
        this.priority = priority;
        this.instructionType = instructionType;
        this.status = status;
        this.amount = amount;
        this.validFrom = validFrom;
        this.validTill = validTill;
        this.recurrenceType = recurrenceType;
        this.recurrenceFrequency = recurrenceFrequency;
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceOnDay = recurrenceOnDay;
        this.recurrenceOnMonth = recurrenceOnMonth;
    }

    public void updateLatsRunDate(LocalDate latsRunDate) {
        this.latsRunDate = latsRunDate;
    }

    public void updateStatus(Integer status) {
        this.status = status;
    }

    /**
     * delete the standing instruction by setting the status to 3 and appending "_deleted_" and the id to the name
     **/
    public void delete() {
        this.status = StandingInstructionStatus.DELETED.getValue();
        this.name = this.name + "_deleted_" + this.getId();
    }
}
