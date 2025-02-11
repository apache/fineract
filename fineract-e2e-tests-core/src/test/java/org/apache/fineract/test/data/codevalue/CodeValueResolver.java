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
package org.apache.fineract.test.data.codevalue;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.services.CodeValuesApi;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Component
@RequiredArgsConstructor
@Slf4j
public class CodeValueResolver {

    private final CodeValuesApi codeValuesApi;

    @Cacheable(key = "#codeValue.getName()", value = "codeValuesByName")
    public long resolve(Long codeId, CodeValue codeValue) {
        try {
            String codeValueName = codeValue.getName();

            log.debug("Resolving code value by code id and name [{}]", codeValue);
            Response<List<GetCodeValuesDataResponse>> response = codeValuesApi.retrieveAllCodeValues(codeId).execute();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Unable to get payment types. Status code was HTTP " + response.code());
            }

            List<GetCodeValuesDataResponse> codeValuesResponses = response.body();
            GetCodeValuesDataResponse foundPtr = codeValuesResponses.stream().filter(ptr -> codeValueName.equals(ptr.getName())).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Payment type [%s] not found".formatted(codeValueName)));

            return foundPtr.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
