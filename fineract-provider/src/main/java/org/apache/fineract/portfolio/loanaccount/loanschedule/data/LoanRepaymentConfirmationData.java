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
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LoanRepaymentConfirmationData {

    private final Long transactionId;
    private final Long loanId;
    private final Long transactionTypeEnum;
    private final String transactionDate;
    private final BigDecimal amount;
    private final BigDecimal principalPortionDerived;
    private final BigDecimal interestPortionDerived;
    private final BigDecimal feeChargesPortionDerived;
    private final BigDecimal penaltyChargePortionDerived;
    private final BigDecimal outstandingLoanBalanceDerived;
    private final Long clientId;
    private final Long groupId;
    private final Long loanProductId;
    private final String productName;
    private final String clientName;
    private final String groupName;
    private final BigDecimal totalOverdueAmount;
    List<LoanRepaymentScheduleData> scheduleDataList = new ArrayList<>();

    public LoanRepaymentConfirmationData(Long transactionId, Long loanId, Long transactionTypeEnum, String transactionDate,
            BigDecimal amount, BigDecimal principalPortionDerived, BigDecimal interestPortionDerived, BigDecimal feeChargesPortionDerived,
            BigDecimal penaltyChargePortionDerived, BigDecimal outstandingLoanBalanceDerived, Long clientId, Long groupId,
            Long loanProductId, String productName, String clientName, String groupName, BigDecimal totalOverdueAmount) {
        this.transactionId = transactionId;
        this.loanId = loanId;
        this.transactionTypeEnum = transactionTypeEnum;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.principalPortionDerived = principalPortionDerived;
        this.interestPortionDerived = interestPortionDerived;
        this.feeChargesPortionDerived = feeChargesPortionDerived;
        this.penaltyChargePortionDerived = penaltyChargePortionDerived;
        this.outstandingLoanBalanceDerived = outstandingLoanBalanceDerived;
        this.clientId = clientId;
        this.groupId = groupId;
        this.loanProductId = loanProductId;
        this.productName = productName;
        this.clientName = clientName;
        this.groupName = groupName;
        this.totalOverdueAmount = totalOverdueAmount;
    }
}
