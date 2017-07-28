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
package org.apache.fineract.portfolio.savings.data;

/**
 * Immutable data object represent savings account status enumerations.
 */
public class SavingsAccountStatusEnumData {

    private final Long id;
    @SuppressWarnings("unused")
    private final String code;
    @SuppressWarnings("unused")
    private final String value;
    @SuppressWarnings("unused")
    private final boolean submittedAndPendingApproval;
    @SuppressWarnings("unused")
    private final boolean approved;
    @SuppressWarnings("unused")
    private final boolean rejected;
    @SuppressWarnings("unused")
    private final boolean withdrawnByApplicant;
    @SuppressWarnings("unused")
    private final boolean active;
    @SuppressWarnings("unused")
    private final boolean closed;
    @SuppressWarnings("unused")
    private final boolean prematureClosed;
    @SuppressWarnings("unused")
    private final boolean transferInProgress;
    @SuppressWarnings("unused")
    private final boolean transferOnHold;
    @SuppressWarnings("unused")
    private final boolean matured;

    public SavingsAccountStatusEnumData(final Long id, final String code, final String value, final boolean submittedAndPendingApproval,
            final boolean approved, final boolean rejected, final boolean withdrawnByApplicant, final boolean active, final boolean closed,
            final boolean prematureClosed, final boolean transferInProgress, final boolean transferOnHold, final boolean matured) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.submittedAndPendingApproval = submittedAndPendingApproval;
        this.approved = approved;
        this.rejected = rejected;
        this.withdrawnByApplicant = withdrawnByApplicant;
        this.active = active;
        this.closed = closed;
        this.prematureClosed = prematureClosed;
        this.transferInProgress = transferInProgress;
        this.transferOnHold = transferOnHold;
        this.matured = matured;
    }

    public Long id() {
        return this.id;
    }
}