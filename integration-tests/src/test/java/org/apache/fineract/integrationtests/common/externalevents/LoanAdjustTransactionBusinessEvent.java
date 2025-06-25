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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoanAdjustTransactionBusinessEvent extends BusinessEvent {

    private String transactionTypeCode;
    private String transactionDate;
    private Double oldAmount;
    private Double newAmount;
    private Double oldPrincipalPortion;
    private Double newPrincipalPortion;
    private Double oldInterestPortion;
    private Double newInterestPortion;
    private Double oldFeePortion;
    private Double newFeePortion;
    private Double oldPenaltyPortion;
    private Double newPenaltyPortion;

    // minimum data for checking if transaction was reversed
    public LoanAdjustTransactionBusinessEvent(String type, String businessDate, String transactionTypeCode, String transactionDate) {
        super(type, businessDate);
        this.transactionTypeCode = transactionTypeCode;
        this.transactionDate = transactionDate;
    }

    // minimum data for checking if transaction was adjusted
    public LoanAdjustTransactionBusinessEvent(String type, String businessDate, String transactionTypeCode, String transactionDate,
            Double oldAmount, Double newAmount) {
        super(type, businessDate);
        this.transactionTypeCode = transactionTypeCode;
        this.transactionDate = transactionDate;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
    }

    public LoanAdjustTransactionBusinessEvent(String type, String businessDate, String transactionTypeCode, String transactionDate,
            Double oldAmount, Double newAmount, Double oldPrincipalPortion, Double newPrincipalPortion, Double oldInterestPortion,
            Double newInterestPortion, Double oldFeePortion, Double newFeePortion, Double oldPenaltyPortion, Double newPenaltyPortion) {
        super(type, businessDate);
        this.transactionTypeCode = transactionTypeCode;
        this.transactionDate = transactionDate;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
        this.oldPrincipalPortion = oldPrincipalPortion;
        this.newPrincipalPortion = newPrincipalPortion;
        this.oldInterestPortion = oldInterestPortion;
        this.newInterestPortion = newInterestPortion;
        this.oldFeePortion = oldFeePortion;
        this.newFeePortion = newFeePortion;
        this.oldPenaltyPortion = oldPenaltyPortion;
        this.newPenaltyPortion = newPenaltyPortion;
    }

    @Override
    public boolean verify(ExternalEventResponse externalEvent, DateTimeFormatter formatter) {
        final Object transactionToAdjust = externalEvent.getPayLoad().get("transactionToAdjust");
        final Map<?, Object> transActionToAdjustMap = transactionToAdjust instanceof Map ? (Map<String, Object>) transactionToAdjust
                : Collections.emptyMap();

        Object actualOldAmount = transActionToAdjustMap.get("amount");
        Object actualOldPrincipalPortion = transActionToAdjustMap.get("principalPortion");
        Object actualOldInterestPortion = transActionToAdjustMap.get("interestPortion");
        Object actualOldFeePortion = transActionToAdjustMap.get("feeChargesPortion");
        Object actualOldPenaltyPortion = transActionToAdjustMap.get("penaltyChargesPortion");

        final Object newTransactionDetail = externalEvent.getPayLoad().get("newTransactionDetail");
        final Map<?, Object> newTransactionDetailMap = newTransactionDetail instanceof Map ? (Map<String, Object>) newTransactionDetail
                : Collections.emptyMap();

        Object actualNewAmount = newTransactionDetailMap.get("amount");
        Object actualNewPrincipalPortion = newTransactionDetailMap.get("principalPortion");
        Object actualNewInterestPortion = newTransactionDetailMap.get("interestPortion");
        Object actualNewFeePortion = newTransactionDetailMap.get("feeChargesPortion");
        Object actualNewPenaltyPortion = newTransactionDetailMap.get("penaltyChargesPortion");

        final Object actualTransactionDate = transActionToAdjustMap.get("date");
        final Object transactionType = transActionToAdjustMap.get("type");
        final Map<?, Object> transactionTypeMap = transactionType instanceof Map ? (Map<String, Object>) transactionType
                : Collections.emptyMap();
        final Object actualTransactionTypeCode = transactionTypeMap.get("code");

        return super.verify(externalEvent, formatter)//
                && Objects.equals(actualTransactionTypeCode, transactionTypeCode) && Objects.equals(actualTransactionDate, transactionDate)//
                && (oldAmount == null || Objects.equals(actualOldAmount, oldAmount))//
                && (newAmount == null || Objects.equals(actualNewAmount, newAmount))//
                && (oldPrincipalPortion == null || Objects.equals(actualOldPrincipalPortion, oldPrincipalPortion))//
                && (newPrincipalPortion == null || Objects.equals(actualNewPrincipalPortion, newPrincipalPortion))//
                && (oldInterestPortion == null || Objects.equals(actualOldInterestPortion, oldInterestPortion))//
                && (newInterestPortion == null || Objects.equals(actualNewInterestPortion, newInterestPortion))//
                && (oldFeePortion == null || Objects.equals(actualOldFeePortion, oldFeePortion))//
                && (newFeePortion == null || Objects.equals(actualNewFeePortion, newFeePortion))//
                && (oldPenaltyPortion == null || Objects.equals(actualOldPenaltyPortion, oldPenaltyPortion))//
                && (newPenaltyPortion == null || Objects.equals(actualNewPenaltyPortion, newPenaltyPortion));
    }
}
