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
package org.apache.fineract.integrationtests.common.funds;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.UUID;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.FundRequest;
import org.apache.fineract.client.models.PostFundsResponse;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;

public final class FundsResourceHandler {

    private FundsResourceHandler() {

    }

    private static final String FUNDS_URL = "/fineract-provider/api/v1/funds";
    private static final String CREATE_FUNDS_URL = FUNDS_URL + "?" + Utils.TENANT_IDENTIFIER;

    public static PostFundsResponse createFund() {
        FundRequest request = new FundRequest();
        request.setName(Utils.uniqueRandomStringGenerator("", 10));
        request.setExternalId(UUID.randomUUID().toString());
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().funds().createFund(request));
    }

    public static Integer createFund(final String fundJSON, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_FUNDS_URL, fundJSON, "resourceId");
    }

    public static Integer createFund(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        FundsHelper fh = FundsHelper.create(Utils.uniqueRandomStringGenerator("", 10)).externalId(UUID.randomUUID().toString()).build();
        return createFund(fh.toJSON(), requestSpec, responseSpec);
    }

}
