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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.FundData;
import org.apache.fineract.client.models.FundRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FundGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String FUNDS_LENDER_A = "Lender A";
    public static final String FUNDS_LENDER_B = "Lender B";

    private final FineractFeignClient fineractClient;

    @Override
    public void initialize() {
        List<FundData> existingFunds = new ArrayList<>();
        try {
            existingFunds = fineractClient.funds().retrieveFunds(Map.of());
        } catch (Exception e) {
            log.debug("Could not retrieve existing funds, will create them", e);
        }

        final List<FundData> funds = existingFunds;
        List<String> fundNames = new ArrayList<>();
        fundNames.add(FUNDS_LENDER_A);
        fundNames.add(FUNDS_LENDER_B);
        fundNames.forEach(name -> {
            boolean fundExists = funds.stream().anyMatch(f -> name.equals(f.getName()));
            if (fundExists) {
                return;
            }

            FundRequest postFundsRequest = new FundRequest();
            postFundsRequest.name(name);
            executeVoid(() -> fineractClient.funds().createFund(postFundsRequest, Map.of()));
        });

    }
}
