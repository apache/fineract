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

public class LoanRepaymentReminderData {

    private final Long loanId;
    private final Long clientId;
    private final Long groupId;
    private final String dueDate;
    private final Integer installmentNumber;
    private final BigDecimal principalAmount;
    private final BigDecimal interestAmount;
    private final BigDecimal feeChargesAmount;
    private final BigDecimal penaltyChargeAmount;
    private final BigDecimal totalAmount;
    private final String productName;
    private final String clientName;
    private final String groupName;

    public LoanRepaymentReminderData(final Long loanId, final Long clientId, final Long groupId, final String dueDate,
            final Integer installmentNumber, final BigDecimal principalAmount, final BigDecimal interestAmount,
            final BigDecimal feeChargesAmount, final BigDecimal penaltyChargeAmount, final BigDecimal totalAmount, final String productName,
            final String clientName, final String groupName) {
        this.loanId = loanId;
        this.clientId = clientId;
        this.groupId = groupId;
        this.dueDate = dueDate;
        this.installmentNumber = installmentNumber;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.feeChargesAmount = feeChargesAmount;
        this.penaltyChargeAmount = penaltyChargeAmount;
        this.totalAmount = totalAmount;
        this.productName = productName;
        this.clientName = clientName;
        this.groupName = groupName;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public BigDecimal getFeeChargesAmount() {
        return feeChargesAmount;
    }

    public BigDecimal getPenaltyChargeAmount() {
        return penaltyChargeAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getProductName() {
        return productName;
    }

    public String getClientName() {
        return clientName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return "LoanRepaymentReminderData{" + "loanId=" + loanId + ", clientId=" + clientId + ", groupId=" + groupId + ", dueDate='"
                + dueDate + '\'' + ", installmentNumber=" + installmentNumber + ", principalAmount=" + principalAmount + ", interestAmount="
                + interestAmount + ", feeChargesAmount=" + feeChargesAmount + ", penaltyChargeAmount=" + penaltyChargeAmount
                + ", totalAmount=" + totalAmount + ", productName='" + productName + '\'' + ", clientName='" + clientName + '\''
                + ", groupName='" + groupName + '\'' + '}';
    }
}
