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
package org.apache.fineract.portfolio.collectionsheet.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for extracting flat data for joint liability group's
 * collection sheet.
 */
public class JLGCollectionSheetFlatData {

    private final String groupName;
    private final Long groupId;
    private final Long staffId;
    private final String staffName;
    private final Long levelId;
    private final String levelName;
    private final String clientName;
    private final Long clientId;
    private final Long loanId;
    private final String accountId;
    private final Integer accountStatusId;
    private final String productShortName;
    private final Long productId;
    private final CurrencyData currency;
    private BigDecimal disbursementAmount = BigDecimal.ZERO;
    private BigDecimal principalDue = BigDecimal.ZERO;
    private BigDecimal principalPaid = BigDecimal.ZERO;
    private BigDecimal interestDue = BigDecimal.ZERO;
    private BigDecimal interestPaid = BigDecimal.ZERO;
    private BigDecimal chargesDue = BigDecimal.ZERO;
    private final EnumOptionData attendanceType;
    private BigDecimal feeDue = BigDecimal.ZERO;
    private BigDecimal feePaid = BigDecimal.ZERO;

    public JLGCollectionSheetFlatData(final String groupName, final Long groupId, final Long staffId, final String staffName,
            final Long levelId, final String levelName, final String clientName, final Long clientId, final Long loanId,
            final String accountId, final Integer accountStatusId, final String productShortName, final Long productId,
            final CurrencyData currency, final BigDecimal disbursementAmount, final BigDecimal principalDue,
            final BigDecimal principalPaid, final BigDecimal interestDue, final BigDecimal interestPaid, final BigDecimal chargesDue,
            final EnumOptionData attendanceType, final BigDecimal feeDue, final BigDecimal feePaid) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.staffId = staffId;
        this.staffName = staffName;
        this.levelId = levelId;
        this.levelName = levelName;
        this.clientName = clientName;
        this.clientId = clientId;
        this.loanId = loanId;
        this.accountId = accountId;
        this.accountStatusId = accountStatusId;
        this.productShortName = productShortName;
        this.productId = productId;
        this.currency = currency;
        this.disbursementAmount = disbursementAmount;
        this.principalDue = principalDue;
        this.principalPaid = principalPaid;
        this.interestDue = interestDue;
        this.interestPaid = interestPaid;
        this.chargesDue = chargesDue;
        this.attendanceType = attendanceType;
        this.feeDue = feeDue;
        this.feePaid = feePaid;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public String getStaffName() {
        return this.staffName;
    }

    public Long getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public String getClientName() {
        return this.clientName;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public Integer getAccountStatusId() {
        return this.accountStatusId;
    }

    public String getProductShortName() {
        return this.productShortName;
    }

    public Long getProductId() {
        return this.productId;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public BigDecimal getDisbursementAmount() {
        return this.disbursementAmount;
    }

    public BigDecimal getPrincipalDue() {
        return this.principalDue;
    }

    public BigDecimal getPrincipalPaid() {
        return this.principalPaid;
    }

    public BigDecimal getInterestDue() {
        return this.interestDue;
    }

    public BigDecimal getInterestPaid() {
        return this.interestPaid;
    }

    public BigDecimal getChargesDue() {
        return this.chargesDue;
    }

    public LoanDueData getLoanDueData() {
        return new LoanDueData(this.loanId, this.accountId, this.accountStatusId, this.productShortName, this.productId, this.currency,
                this.disbursementAmount, this.principalDue, this.principalPaid, this.interestDue, this.interestPaid, this.chargesDue,
                this.feeDue, this.feePaid);
    }

    public JLGClientData getClientData() {
        return JLGClientData.withAttendance(this.clientId, this.clientName, this.attendanceType);
    }

    public JLGGroupData getJLGGroupData() {

        return JLGGroupData.instance(this.groupId, this.groupName, this.staffId, this.staffName, this.levelId, this.levelName);
    }

    public BigDecimal getFeeDue() {
        return this.feeDue;
    }

    public BigDecimal getFeePaid() {
        return this.feePaid;
    }
}