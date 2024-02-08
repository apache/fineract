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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.data;

import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;

/**
 * Immutable data object represent loan reschedule request status enumerations.
 **/
public class LoanRescheduleRequestStatusEnumData {

    private final Long id;
    private final String code;
    private final String value;
    private final boolean pendingApproval;
    private final boolean approved;
    private final boolean rejected;

    /**
     * LoanRescheduleRequestStatusEnumData constructor
     *
     * Note: Same status types/states for loan accounts are used in here
     **/
    public LoanRescheduleRequestStatusEnumData(Long id, String code, String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.pendingApproval = Long.valueOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue()).equals(this.id);
        this.approved = Long.valueOf(LoanStatus.APPROVED.getValue()).equals(this.id);
        this.rejected = Long.valueOf(LoanStatus.REJECTED.getValue()).equals(this.id);
    }

    public Long id() {
        return this.id;
    }

    public String code() {
        return this.code;
    }

    public String value() {
        return this.value;
    }

    public boolean isPendingApproval() {
        return this.pendingApproval;
    }

    public boolean isApproved() {
        return this.approved;
    }

    public boolean isRejected() {
        return this.rejected;
    }
}
