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
import org.apache.fineract.client.models.GetLoanOriginatorTemplateResponse;
import org.apache.fineract.client.models.GetLoanOriginatorsResponse;
import org.apache.fineract.client.models.PostLoanOriginatorsRequest;
import org.apache.fineract.client.models.PostLoanOriginatorsResponse;
import org.apache.fineract.client.models.PutLoanOriginatorsRequest;
import org.apache.fineract.client.models.PutLoanOriginatorsResponse;

public class FeignLoanOriginatorHelper {

    private final FineractFeignClient fineractClient;

    public FeignLoanOriginatorHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createOriginator(String externalId) {
        return createOriginator(new PostLoanOriginatorsRequest().externalId(externalId).name(externalId));
    }

    public Long createOriginator(String externalId, String name, String status) {
        return createOriginator(new PostLoanOriginatorsRequest().externalId(externalId).name(name).status(status));
    }

    public Long createOriginator(PostLoanOriginatorsRequest request) {
        PostLoanOriginatorsResponse response = ok(() -> fineractClient.loanOriginators().create11(request));
        return response.getResourceId();
    }

    public CallFailedRuntimeException createOriginatorExpectingError(PostLoanOriginatorsRequest request) {
        return fail(() -> fineractClient.loanOriginators().create11(request));
    }

    public List<GetLoanOriginatorsResponse> getAllOriginators() {
        return ok(() -> fineractClient.loanOriginators().retrieveAll28());
    }

    public GetLoanOriginatorsResponse getOriginatorById(Long originatorId) {
        return ok(() -> fineractClient.loanOriginators().retrieveOne18(originatorId));
    }

    public CallFailedRuntimeException getOriginatorByIdExpectingError(Long originatorId) {
        return fail(() -> fineractClient.loanOriginators().retrieveOne18(originatorId));
    }

    public GetLoanOriginatorsResponse getOriginatorByExternalId(String externalId) {
        return ok(() -> fineractClient.loanOriginators().retrieveByExternalId(externalId));
    }

    public CallFailedRuntimeException getOriginatorByExternalIdExpectingError(String externalId) {
        return fail(() -> fineractClient.loanOriginators().retrieveByExternalId(externalId));
    }

    public PutLoanOriginatorsResponse updateOriginator(Long originatorId, PutLoanOriginatorsRequest request) {
        return ok(() -> fineractClient.loanOriginators().update16(originatorId, request));
    }

    public PutLoanOriginatorsResponse updateOriginatorByExternalId(String externalId, PutLoanOriginatorsRequest request) {
        return ok(() -> fineractClient.loanOriginators().updateByExternalId(externalId, request));
    }

    public CallFailedRuntimeException updateOriginatorExpectingError(Long originatorId, PutLoanOriginatorsRequest request) {
        return fail(() -> fineractClient.loanOriginators().update16(originatorId, request));
    }

    public Long deleteOriginator(Long originatorId) {
        var response = ok(() -> fineractClient.loanOriginators().delete14(originatorId));
        return response.getResourceId();
    }

    public Long deleteOriginatorByExternalId(String externalId) {
        var response = ok(() -> fineractClient.loanOriginators().deleteByExternalId(externalId));
        return response.getResourceId();
    }

    public CallFailedRuntimeException deleteOriginatorExpectingError(Long originatorId) {
        return fail(() -> fineractClient.loanOriginators().delete14(originatorId));
    }

    public static String generateUniqueExternalId() {
        return "EXT-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public void attachOriginatorToLoan(Long loanId, Long originatorId) {
        ok(() -> {
            fineractClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId);
            return null;
        });
    }

    public CallFailedRuntimeException attachOriginatorToLoanExpectingError(Long loanId, Long originatorId) {
        return fail(() -> {
            fineractClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId);
            return null;
        });
    }

    public void detachOriginatorFromLoan(Long loanId, Long originatorId) {
        ok(() -> {
            fineractClient.loanOriginators().detachOriginatorFromLoan(loanId, originatorId);
            return null;
        });
    }

    public CallFailedRuntimeException detachOriginatorFromLoanExpectingError(Long loanId, Long originatorId) {
        return fail(() -> {
            fineractClient.loanOriginators().detachOriginatorFromLoan(loanId, originatorId);
            return null;
        });
    }

    public GetLoanOriginatorTemplateResponse retrieveLoanOriginatorTemplate() {
        return ok(() -> fineractClient.loanOriginators().retrieveLoanOriginatorTemplate());
    }
}
