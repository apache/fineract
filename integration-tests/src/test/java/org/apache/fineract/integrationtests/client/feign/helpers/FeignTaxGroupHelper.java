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
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetTaxesGroupResponse;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupResponse;

public class FeignTaxGroupHelper {

    private final FineractFeignClient fineractClient;

    public FeignTaxGroupHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostTaxesGroupResponse createTaxGroup(PostTaxesGroupRequest request) {
        return ok(() -> fineractClient.taxGroup().createTaxGroup(request));
    }

    public GetTaxesGroupResponse retrieveTaxGroup(Long taxGroupId) {
        return ok(() -> fineractClient.taxGroup().retrieveOneTaxGroup(taxGroupId));
    }

    public List<GetTaxesGroupResponse> retrieveAllTaxGroups() {
        return ok(() -> fineractClient.taxGroup().retrieveAllTaxGroups());
    }

    public CallFailedRuntimeException retrieveTaxGroupExpectingError(Long taxGroupId) {
        return fail(() -> fineractClient.taxGroup().retrieveOneTaxGroup(taxGroupId));
    }

    public CallFailedRuntimeException createTaxGroupExpectingError(PostTaxesGroupRequest request) {
        return fail(() -> fineractClient.taxGroup().createTaxGroup(request));
    }
}
