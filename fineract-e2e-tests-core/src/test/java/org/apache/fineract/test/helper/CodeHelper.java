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
package org.apache.fineract.test.helper;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetCodesResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeHelper {

    private static final String COUNTRY_CODE_NAME = "COUNTRY";
    private static final String STATE_CODE_NAME = "STATE";
    private static final String ADDRESS_TYPE_CODE_NAME = "ADDRESS_TYPE";

    private final FineractFeignClient fineractClient;

    public PostCodeValueDataResponse createAddressTypeCodeValue(String addressTypeName) {
        Long codeId = retrieveCodeByName(ADDRESS_TYPE_CODE_NAME).getId();
        return ok(
                () -> fineractClient.codeValues().createCodeValue(codeId, new PostCodeValuesDataRequest().name(addressTypeName), Map.of()));
    }

    public PostCodeValueDataResponse createCountryCodeValue(String countryName) {
        Long codeId = retrieveCodeByName(COUNTRY_CODE_NAME).getId();
        return ok(() -> fineractClient.codeValues().createCodeValue(codeId, new PostCodeValuesDataRequest().name(countryName), Map.of()));
    }

    public PostCodeValueDataResponse createStateCodeValue(String stateName) {
        Long codeId = retrieveCodeByName(STATE_CODE_NAME).getId();
        return ok(() -> fineractClient.codeValues().createCodeValue(codeId, new PostCodeValuesDataRequest().name(stateName), Map.of()));
    }

    public GetCodesResponse retrieveCodeByName(String name) {
        return ok(() -> fineractClient.codes().retrieveCodes(Map.of())).stream().filter(r -> name.equals(r.getName())).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Code with name " + name + " has not been found"));
    }

    public PostCodeValueDataResponse createCodeValue(Long codeId, PostCodeValuesDataRequest request) {
        return ok(() -> fineractClient.codeValues().createCodeValue(codeId, request, Map.of()));
    }
}
