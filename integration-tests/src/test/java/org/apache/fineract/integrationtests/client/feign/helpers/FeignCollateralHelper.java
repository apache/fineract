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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ClientCollateralCreateRequest;
import org.apache.fineract.client.models.ClientCollateralCreateResponse;
import org.apache.fineract.client.models.CollateralProductCreateRequest;
import org.apache.fineract.client.models.CollateralProductCreateResponse;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignCollateralHelper {

    private static final String LOCALE = "en";
    private static final BigDecimal DEFAULT_PCT_TO_BASE = BigDecimal.valueOf(40);
    private static final BigDecimal DEFAULT_BASE_PRICE = BigDecimal.valueOf(100000000);
    private static final BigDecimal DEFAULT_QUANTITY = BigDecimal.valueOf(100);

    private final FineractFeignClient fineractClient;

    public FeignCollateralHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    /**
     * Creates a collateral product with the same defaults as the legacy
     * {@code CollateralManagementHelper.createCollateralProduct}.
     */
    public CollateralProductCreateResponse createCollateralProduct() {
        return ok(() -> fineractClient.collateralManagement()
                .createCollateral1(new CollateralProductCreateRequest().name(Utils.randomStringGenerator("COLLATERAL_PRODUCT", 5))
                        .currency("USD").unitType("acre").quality("agriculture").pctToBase(DEFAULT_PCT_TO_BASE)
                        .basePrice(DEFAULT_BASE_PRICE).locale(LOCALE)));
    }

    public ClientCollateralCreateResponse createClientCollateral(Long clientId, Long collateralId) {
        return ok(() -> fineractClient.clientCollateralManagement().addClientCollateral(clientId,
                new ClientCollateralCreateRequest().collateralId(collateralId).quantity(DEFAULT_QUANTITY).locale(LOCALE)));
    }
}
