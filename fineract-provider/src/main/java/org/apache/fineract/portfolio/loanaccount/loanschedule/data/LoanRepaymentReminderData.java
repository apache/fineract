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
import java.time.LocalDate;

public class LoanRepaymentReminderData {

    private final Long loanId;
    private final Long clientId;
    private final Long groupId;
    private final Long loanProductId;
    private final Long loanScheduleId;
    private final LocalDate dueDate;
    private final Integer installmentNumber;
    private final BigDecimal principalAmountOutStanding;
    private final BigDecimal interestAmountOutStanding;
    private final BigDecimal feesChargeAmountOutStanding;
    private final BigDecimal penaltyChargeAmountOutStanding;
    private final BigDecimal totalAmountOutStanding;
    private final String productName;
    private final String clientName;
    private final String groupName;
    private BigDecimal totalOverdueAmount;

    public LoanRepaymentReminderData(Long loanId, Long clientId, Long groupId, Long loanProductId, Long loanScheduleId, LocalDate dueDate,
            Integer installmentNumber, BigDecimal principalAmountOutStanding, BigDecimal interestAmountOutStanding,
            BigDecimal feesChargeAmountOutStanding, BigDecimal penaltyChargeAmountOutStanding, BigDecimal totalAmountOutStanding,
            String productName, String clientName, String groupName, BigDecimal totalOverdueAmount) {
        this.loanId = loanId;
        this.clientId = clientId > 0 ? clientId : null;
        this.groupId = groupId > 0 ? groupId : null;
        this.loanProductId = loanProductId;
        this.loanScheduleId = loanScheduleId;
        this.dueDate = dueDate;
        this.installmentNumber = installmentNumber;
        this.principalAmountOutStanding = principalAmountOutStanding;
        this.interestAmountOutStanding = interestAmountOutStanding;
        this.feesChargeAmountOutStanding = feesChargeAmountOutStanding;
        this.penaltyChargeAmountOutStanding = penaltyChargeAmountOutStanding;
        this.totalAmountOutStanding = totalAmountOutStanding;
        this.productName = productName;
        this.clientName = clientName;
        this.groupName = groupName;
        this.totalOverdueAmount = totalOverdueAmount;
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

    public Long getLoanProductId() {
        return loanProductId;
    }

    public Long getLoanScheduleId() {
        return loanScheduleId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public BigDecimal getPrincipalAmountOutStanding() {
        return principalAmountOutStanding;
    }

    public BigDecimal getInterestAmountOutStanding() {
        return interestAmountOutStanding;
    }

    public BigDecimal getFeesChargeAmountOutStanding() {
        return feesChargeAmountOutStanding;
    }

    public BigDecimal getPenaltyChargeAmountOutStanding() {
        return penaltyChargeAmountOutStanding;
    }

    public BigDecimal getTotalAmountOutStanding() {
        return totalAmountOutStanding;
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

    public BigDecimal getTotalOverdueAmount() {
        return totalOverdueAmount;
    }

    @Override
    public String toString() {
        return "LoanRepaymentReminderData{" + "loanId=" + loanId + ", clientId=" + clientId + ", groupId=" + groupId + ", loanProductId="
                + loanProductId + ", loanScheduleId=" + loanScheduleId + ", dueDate='" + dueDate + '\'' + ", installmentNumber="
                + installmentNumber + ", principalAmountOutStanding=" + principalAmountOutStanding + ", interestAmountOutStanding="
                + interestAmountOutStanding + ", feesChargeAmountOutStanding=" + feesChargeAmountOutStanding
                + ", penaltyChargeAmountOutStanding=" + penaltyChargeAmountOutStanding + ", totalAmountOutStanding="
                + totalAmountOutStanding + ", productName='" + productName + '\'' + ", clientName='" + clientName + '\'' + ", groupName='"
                + groupName + '\'' + ", totalOverdueAmount='" + totalOverdueAmount + '\'' + '}';
    }
}
