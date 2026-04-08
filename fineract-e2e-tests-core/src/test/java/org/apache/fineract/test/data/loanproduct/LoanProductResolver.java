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
package org.apache.fineract.test.data.loanproduct;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanProductResolver {

    private final FineractFeignClient fineractClient;

    public long resolve(LoanProduct loanProduct) {
        String loanProductName = loanProduct.getName();
        log.debug("Resolving loan product by name [{}]", loanProductName);
        List<GetLoanProductsResponse> loanProductsResponses = ok(() -> fineractClient.loanProducts().retrieveAllLoanProducts(Map.of()));

        GetLoanProductsResponse foundLpr = loanProductsResponses.stream().filter(lpr -> loanProductName.equals(lpr.getName())).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Loan product [%s] not found".formatted(loanProductName)));
        return foundLpr.getId();
    }
}
