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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;

public class OverdueLoanScheduleData {

    private final Long loanId;
    private final Long chargeId;
    private final String locale;
    private final BigDecimal amount;
    private final String dateFormat;
    private final String dueDate;
    private final BigDecimal principalOverdue;
    private final BigDecimal interestOverdue;
    private final Integer periodNumber;

    public OverdueLoanScheduleData(final Long loanId, final Long chargeId, final String dueDate, final BigDecimal amount,
            final String dateFormat, final String locale, final BigDecimal principalOverdue, final BigDecimal interestOverdue,
            final Integer periodNumber) {
        this.loanId = loanId;
        this.chargeId = chargeId;
        this.dueDate = dueDate;
        this.amount = amount;
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.principalOverdue = principalOverdue;
        this.interestOverdue = interestOverdue;
        this.periodNumber = periodNumber;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public Long getChargeId() {
        return this.chargeId;
    }

    public String getDueDate() {
        return this.dueDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    public String getLocale() {
        return this.locale;
    }

    public Integer getPeriodNumber() {
        return this.periodNumber;
    }

    @Override
    public String toString() {
        return "{" + "chargeId:" + this.chargeId + ", locale:'" + this.locale + '\'' + ", amount:" + this.amount + ", dateFormat:'"
                + this.dateFormat + '\'' + ", dueDate:'" + this.dueDate + '\'' + ", principal:'" + this.principalOverdue + '\''
                + ", interest:'" + this.interestOverdue + '\'' + '}';
    }

}
