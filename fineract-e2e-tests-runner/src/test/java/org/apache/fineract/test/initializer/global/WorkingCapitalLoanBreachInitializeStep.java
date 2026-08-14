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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachData;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanNearBreachData;
import org.apache.fineract.client.models.WorkingCapitalLoanNearBreachRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkingCapitalLoanBreachInitializeStep implements FineractGlobalInitializerStep {

    public static final String DEFAULT_WC_BREACH_NAME = "Default Working Capital breach";
    public static final Integer DEFAULT_WC_BREACH_FREQUENCY = 2;
    public static final String DEFAULT_WC_BREACH_FREQUENCY_TYPE = "MONTHS";
    public static final String DEFAULT_WC_BREACH_AMOUNT_CALCULATION_TYPE = "PERCENTAGE";
    public static final BigDecimal DEFAULT_WC_BREACH_AMOUNT = new BigDecimal("1.23");

    public static final String DEFAULT_WC_NEAR_BREACH_NAME = "Default Working Capital near breach";
    public static final Integer DEFAULT_WC_NEAR_BREACH_FREQUENCY = 1;
    public static final String DEFAULT_WC_NEAR_BREACH_FREQUENCY_TYPE = "MONTHS";
    public static final BigDecimal DEFAULT_WC_NEAR_BREACH_THRESHOLD = new BigDecimal("72.15");

    private final FineractFeignClient fineractClient;

    @Override
    public void initialize() {
        setDefaultWCBreach();
        setDefaultWCNearBreach();
    }

    public void setDefaultWCBreach() {
        try {
            List<WorkingCapitalLoanBreachData> existingBuckets = fineractClient.workingCapitalBreaches()
                    .retrieveAllWorkingCapitalLoanBreaches(Map.of());
            boolean bucketExists = existingBuckets.stream().anyMatch(b -> DEFAULT_WC_BREACH_NAME.equals(b.getName()));

            if (bucketExists) {
                return;
            }
        } catch (Exception e) {
            log.debug("Could not retrieve existing working capital breaches, will create default breach", e);
        }

        WorkingCapitalLoanBreachRequest defaultWorkingCapitalLoanBreachRequest = new WorkingCapitalLoanBreachRequest()//
                .name(DEFAULT_WC_BREACH_NAME) //
                .breachFrequency(DEFAULT_WC_BREACH_FREQUENCY) //
                .breachFrequencyType(DEFAULT_WC_BREACH_FREQUENCY_TYPE) //
                .breachAmountCalculationType(DEFAULT_WC_BREACH_AMOUNT_CALCULATION_TYPE) //
                .breachAmount(DEFAULT_WC_BREACH_AMOUNT);

        executeVoid(() -> fineractClient.workingCapitalBreaches().createWorkingCapitalLoanBreach(defaultWorkingCapitalLoanBreachRequest,
                Map.of()));
    }

    public void setDefaultWCNearBreach() {
        try {
            List<WorkingCapitalLoanNearBreachData> existingBuckets = fineractClient.workingCapitalNearBreaches()
                    .retrieveAllWorkingCapitalLoanNearBreaches(Map.of());
            boolean bucketExists = existingBuckets.stream().anyMatch(b -> DEFAULT_WC_NEAR_BREACH_NAME.equals(b.getName()));

            if (bucketExists) {
                return;
            }
        } catch (Exception e) {
            log.debug("Could not retrieve existing working capital breaches, will create default breach", e);
        }

        WorkingCapitalLoanNearBreachRequest defaultWorkingCapitalLoanNearBreachRequest = new WorkingCapitalLoanNearBreachRequest()//
                .nearBreachName(DEFAULT_WC_NEAR_BREACH_NAME) //
                .nearBreachFrequency(DEFAULT_WC_NEAR_BREACH_FREQUENCY) //
                .nearBreachFrequencyType(DEFAULT_WC_NEAR_BREACH_FREQUENCY_TYPE) //
                .nearBreachThreshold(DEFAULT_WC_NEAR_BREACH_THRESHOLD); //

        executeVoid(() -> fineractClient.workingCapitalNearBreaches()
                .createWorkingCapitalLoanNearBreach(defaultWorkingCapitalLoanNearBreachRequest, Map.of()));
    }
}
