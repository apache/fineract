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
package org.apache.fineract.portfolio.loanaccount.data;

/**
 * Immutable data object represent loan status enumerations.
 */
@SuppressWarnings("unused")
public class LoanStatusEnumData {

    private final Long id;
    private final String code;
    private final String value;
    private final boolean pendingApproval;
    private final boolean waitingForDisbursal;
    private final boolean active;
    private final boolean closedObligationsMet;
    private final boolean closedWrittenOff;
    private final boolean closedRescheduled;
    private final boolean closed;
    private final boolean overpaid;

    public LoanStatusEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.pendingApproval = Long.valueOf(100).equals(this.id);
        this.waitingForDisbursal = Long.valueOf(200).equals(this.id);
        this.active = Long.valueOf(300).equals(this.id);
        this.closedObligationsMet = Long.valueOf(600).equals(this.id);
        this.closedWrittenOff = Long.valueOf(601).equals(this.id);
        this.closedRescheduled = Long.valueOf(602).equals(this.id);
        this.closed = this.closedObligationsMet || this.closedWrittenOff || this.closedRescheduled;
        this.overpaid = Long.valueOf(700).equals(this.id);
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
}