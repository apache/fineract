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
package org.apache.fineract.integrationtests.common;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import org.apache.fineract.client.models.TaxGroupCreateRequest;
import org.apache.fineract.client.models.TaxGroupCreateResponse;
import org.apache.fineract.client.models.TaxGroupData;

public final class TaxGroupHelper {

    private TaxGroupHelper() {}

    public static TaxGroupCreateResponse createTaxGroup(TaxGroupCreateRequest request) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().taxGroup().createTaxGroup(request));
    }

    public static TaxGroupData retrieveTaxGroup(Long taxGroupId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().taxGroup().retrieveOneTaxGroup(taxGroupId, false));
    }

    public static List<TaxGroupData> retrieveAllTaxGroups() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().taxGroup().retrieveAllTaxGroups());
    }
}
