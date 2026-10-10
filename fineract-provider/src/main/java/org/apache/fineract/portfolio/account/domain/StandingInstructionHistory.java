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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;

/**
 * A single execution outcome of a standing instruction, persisted to
 * {@code m_account_transfer_standing_instructions_history}.
 *
 * <p>
 * This replaces the previous raw string-concatenated SQL insert in the execution tasklet. Each row is written in its
 * own committed transaction (see {@code StandingInstructionExecutionService}) so a rolled-back transfer still leaves a
 * durable {@code failed} record. The {@code amount} is the amount actually transferred (0 on failure), not the
 * attempted amount.
 * </p>
 */
@Entity
@Table(name = "m_account_transfer_standing_instructions_history")
public class StandingInstructionHistory extends AbstractPersistableCustom<Long> {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";

    @Column(name = "standing_instruction_id", nullable = false)
    private Long standingInstructionId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "execution_time", nullable = false)
    private LocalDateTime executionTime;

    @Column(name = "amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal amount;

    @Column(name = "error_log", length = 500)
    private String errorLog;

    protected StandingInstructionHistory() {
        //
    }

    private StandingInstructionHistory(final Long standingInstructionId, final String status, final BigDecimal amount,
            final String errorLog) {
        this.standingInstructionId = standingInstructionId;
        this.status = status;
        this.amount = amount;
        this.errorLog = errorLog;
        this.executionTime = DateUtils.getLocalDateTimeOfTenant();
    }

    public static StandingInstructionHistory success(final Long standingInstructionId, final BigDecimal transferredAmount) {
        return new StandingInstructionHistory(standingInstructionId, STATUS_SUCCESS, transferredAmount, null);
    }

    public static StandingInstructionHistory failed(final Long standingInstructionId, final String errorLog) {
        // Nothing moved on a failed run, so the recorded amount is zero (not the attempted amount).
        return new StandingInstructionHistory(standingInstructionId, STATUS_FAILED, BigDecimal.ZERO, trimToColumn(errorLog));
    }

    private static String trimToColumn(final String errorLog) {
        if (errorLog == null) {
            return null;
        }
        return errorLog.length() > 500 ? errorLog.substring(0, 500) : errorLog;
    }

    public Long getStandingInstructionId() {
        return this.standingInstructionId;
    }

    public String getStatus() {
        return this.status;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public String getErrorLog() {
        return this.errorLog;
    }
}
