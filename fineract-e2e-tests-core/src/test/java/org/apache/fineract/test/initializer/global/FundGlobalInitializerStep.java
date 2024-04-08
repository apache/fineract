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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostFundsRequest;
import org.apache.fineract.client.services.FundsApi;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FundGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String FUNDS_LENDER_A = "Lender A";
    public static final String FUNDS_LENDER_B = "Lender B";

    private final FundsApi fundsApi;

    @Override
    public void initialize() throws Exception {
        List<String> fundNames = new ArrayList<>();
        fundNames.add(FUNDS_LENDER_A);
        fundNames.add(FUNDS_LENDER_B);
        fundNames.forEach(name -> {
            PostFundsRequest postFundsRequest = new PostFundsRequest();
            postFundsRequest.name(name);
            try {
                fundsApi.createFund(postFundsRequest).execute();
            } catch (IOException e) {
                throw new RuntimeException("Error while creating fund", e);
            }
        });

    }
}
