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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fineract.test.helper;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.services.AuditsApi.RetrieveAuditEntriesQueryParams;
import org.apache.fineract.client.models.AuditData;
import org.springframework.stereotype.Component;

/**
 * Queries the {@code /audits} API and parses the paged response. The endpoint returns raw JSON ({@code pageItems}
 * wrapper when {@code paged=true}), so the result is deserialized with the same ObjectMapper the feign client uses.
 */
@Component
@RequiredArgsConstructor
public class AuditHelper {

    private static final ObjectMapper MAPPER = ObjectMapperFactory.getShared();
    private static final TypeReference<List<AuditData>> AUDIT_DATA_LIST_TYPE = new TypeReference<>() {};

    private final FineractFeignClient fineractClient;

    public List<AuditData> searchAudits(RetrieveAuditEntriesQueryParams queryParams) {
        String response = ok(() -> fineractClient.audits().retrieveAuditEntries(queryParams.orderBy("id").sortOrder("DESC").paged(true)));
        try {
            JsonNode pageItemsNode = MAPPER.readTree(response).get("pageItems");
            return MAPPER.convertValue(pageItemsNode, AUDIT_DATA_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to parse audits list response: " + response, e);
        }
    }
}
