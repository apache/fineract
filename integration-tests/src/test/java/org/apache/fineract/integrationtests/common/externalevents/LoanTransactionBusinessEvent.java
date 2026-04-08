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
package org.apache.fineract.integrationtests.common.externalevents;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoanTransactionBusinessEvent extends BusinessEvent {

    private Double amount;
    private Double outstandingLoanBalance;
    private Double principalPortion;
    private Double interestPortion;
    private Double feeChargesPortion;
    private Double penaltyChargesPortion;

    public LoanTransactionBusinessEvent(String type, String businessDate, Double amount, Double outstandingLoanBalance,
            Double principalPortion, Double interestPortion, Double feeChargesPortion, Double penaltyChargesPortion) {
        super(type, businessDate);
        this.amount = amount;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
    }

    @Override
    public boolean verify(ExternalEventResponse externalEvent, DateTimeFormatter formatter) {
        Object amount = externalEvent.getPayLoad().get("amount");
        Object outstandingLoanBalance = externalEvent.getPayLoad().get("outstandingLoanBalance");
        Object principalPortion = externalEvent.getPayLoad().get("principalPortion");
        Object interestPortion = externalEvent.getPayLoad().get("interestPortion");
        Object feePortion = externalEvent.getPayLoad().get("feeChargesPortion");
        Object penaltyPortion = externalEvent.getPayLoad().get("penaltyChargesPortion");

        return super.verify(externalEvent, formatter) && Objects.equals(amount, getAmount())
                && Objects.equals(outstandingLoanBalance, getOutstandingLoanBalance())
                && Objects.equals(principalPortion, getPrincipalPortion()) && Objects.equals(interestPortion, getInterestPortion())
                && Objects.equals(feePortion, getFeeChargesPortion()) && Objects.equals(penaltyPortion, getPenaltyChargesPortion());
    }
}
