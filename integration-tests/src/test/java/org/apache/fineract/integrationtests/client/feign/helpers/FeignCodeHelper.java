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

import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetCodesResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignCodeHelper {

    private static final String CHARGE_OFF_REASONS_CODE_NAME = "ChargeOffReasons";
    private static final String WRITE_OFF_REASONS_CODE_NAME = "WriteOffReasons";

    private final FineractFeignClient fineractClient;

    public FeignCodeHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createChargeOffCodeValue(String value, Integer position) {
        GetCodesResponse code = ok(() -> fineractClient.codes().retrieveOneCodeByName(CHARGE_OFF_REASONS_CODE_NAME));
        PostCodeValueDataResponse response = ok(() -> fineractClient.codeValues().createCodeValue(code.getId(),
                new PostCodeValuesDataRequest().name(value).position(position).description(value).isActive(true)));
        return response.getSubResourceId();
    }

    public Long createWriteOffCodeValue(String value, Integer position) {
        GetCodesResponse code = ok(() -> fineractClient.codes().retrieveOneCodeByName(WRITE_OFF_REASONS_CODE_NAME));
        PostCodeValueDataResponse response = ok(() -> fineractClient.codeValues().createCodeValue(code.getId(),
                new PostCodeValuesDataRequest().name(value).position(position).description(value).isActive(true)));
        return response.getSubResourceId();
    }

    public GetCodesResponse retrieveCodeByName(String codeName) {
        return ok(() -> fineractClient.codes().retrieveOneCodeByName(codeName));
    }

    public PostCodeValueDataResponse createCodeValue(Long codeId, PostCodeValuesDataRequest request) {
        return ok(() -> fineractClient.codeValues().createCodeValue(codeId, request));
    }

    public List<GetCodeValuesDataResponse> retrieveAllCodeValues(Long codeId) {
        return ok(() -> fineractClient.codeValues().retrieveAllCodeValues(codeId));
    }

    /**
     * Returns the id of the first code value of the named code, creating one when the code has none yet. System codes
     * such as {@code LoanRescheduleReason} ship without values, so a test that needs a reason id has to seed one.
     */
    public Long retrieveOrCreateCodeValueId(String codeName) {
        GetCodesResponse code = retrieveCodeByName(codeName);
        List<GetCodeValuesDataResponse> codeValues = retrieveAllCodeValues(code.getId());
        if (!codeValues.isEmpty()) {
            return codeValues.get(0).getId();
        }
        String value = Utils.randomStringGenerator("", 3);
        return createCodeValue(code.getId(), new PostCodeValuesDataRequest().name(value).position(0).description(value).isActive(true))
                .getSubResourceId();
    }
}
