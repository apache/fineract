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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.FundData;
import org.apache.fineract.client.models.FundRequest;
import org.apache.fineract.client.models.PostFundsResponse;
import org.apache.fineract.client.models.PutFundsFundIdResponse;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignFundHelper {

    private final FineractFeignClient fineractClient;

    public FeignFundHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostFundsResponse createFund(FundRequest request) {
        return ok(() -> fineractClient.funds().createFund(request));
    }

    public PostFundsResponse createFund() {
        return createFund(new FundRequest().name(Utils.uniqueRandomStringGenerator("", 10)).externalId(UUID.randomUUID().toString()));
    }

    public CallFailedRuntimeException createFundExpectingError(FundRequest request) {
        return fail(() -> fineractClient.funds().createFund(request));
    }

    public FundData retrieveFund(Long fundId) {
        return ok(() -> fineractClient.funds().retrieveFund(fundId));
    }

    public CallFailedRuntimeException retrieveFundExpectingError(Long fundId) {
        return fail(() -> fineractClient.funds().retrieveFund(fundId));
    }

    public List<FundData> retrieveAllFunds() {
        return ok(() -> fineractClient.funds().retrieveFunds());
    }

    public PutFundsFundIdResponse updateFund(Long fundId, FundRequest request) {
        return ok(() -> fineractClient.funds().updateFund(fundId, request));
    }

    public CallFailedRuntimeException updateFundExpectingError(Long fundId, FundRequest request) {
        return fail(() -> fineractClient.funds().updateFund(fundId, request));
    }
}
