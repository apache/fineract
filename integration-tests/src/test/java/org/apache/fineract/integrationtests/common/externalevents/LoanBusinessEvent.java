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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoanBusinessEvent extends BusinessEvent {

    private Integer statusId;
    private Double principalDisbursed;
    private Double principalOutstanding;
    private List<String> loanTermVariationType;

    public LoanBusinessEvent(String type, String businessDate, Integer statusId, Double principalDisbursed, Double principalOutstanding) {
        super(type, businessDate);
        this.statusId = statusId;
        this.principalDisbursed = principalDisbursed;
        this.principalOutstanding = principalOutstanding;
    }

    public LoanBusinessEvent(String type, String businessDate, Integer statusId, Double principalDisbursed, Double principalOutstanding,
            List<String> loanTermVariationType) {
        super(type, businessDate);
        this.statusId = statusId;
        this.principalDisbursed = principalDisbursed;
        this.principalOutstanding = principalOutstanding;
        this.loanTermVariationType = loanTermVariationType;
    }

    @Override
    public boolean verify(ExternalEventResponse externalEvent, DateTimeFormatter formatter) {
        Object summaryRes = externalEvent.getPayLoad().get("summary");
        Object statusRes = externalEvent.getPayLoad().get("status");
        Map<String, Object> summary = summaryRes instanceof Map ? (Map<String, Object>) summaryRes : Map.of();
        Map<String, Object> status = statusRes instanceof Map ? (Map<String, Object>) statusRes : Map.of();
        var principalDisbursed = summary.get("principalDisbursed");

        var principalOutstanding = summary.get("principalOutstanding");
        Double statusId = (Double) status.get("id");
        return super.verify(externalEvent, formatter) && Objects.equals(statusId, getStatusId().doubleValue())
                && Objects.equals(principalDisbursed, getPrincipalDisbursed())
                && Objects.equals(principalOutstanding, getPrincipalOutstanding()) && loanTermVariationsMatch(
                        (List<Map<String, Object>>) externalEvent.getPayLoad().get("loanTermVariations"), loanTermVariationType);
    }

    private boolean loanTermVariationsMatch(final List<Map<String, Object>> loanTermVariations, final List<String> expectedTypes) {
        if (CollectionUtils.isEmpty(expectedTypes)) {
            return true;
        }
        final long numberOfMatches = expectedTypes
                .stream().filter(
                        expectedType -> loanTermVariations.stream()
                                .anyMatch(variation -> StringUtils
                                        .equals((String) ((Map<String, Object>) variation.get("termType")).get("value"), expectedType)))
                .count();

        return numberOfMatches == expectedTypes.size();
    }
}
