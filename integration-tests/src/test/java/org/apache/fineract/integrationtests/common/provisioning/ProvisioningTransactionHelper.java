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
package org.apache.fineract.integrationtests.common.provisioning;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.DeleteProvisioningCriteriaResponse;
import org.apache.fineract.client.models.GetProvisioningCriteriaCriteriaIdResponse;
import org.apache.fineract.client.models.PageLoanProductProvisioningEntryData;
import org.apache.fineract.client.models.PageProvisioningEntryData;
import org.apache.fineract.client.models.PostProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PostProvisioningCriteriaResponse;
import org.apache.fineract.client.models.PostProvisioningEntriesResponse;
import org.apache.fineract.client.models.ProvisionEntryRequest;
import org.apache.fineract.client.models.ProvisioningCategoryData;
import org.apache.fineract.client.models.ProvisioningEntryData;
import org.apache.fineract.client.models.PutProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PutProvisioningCriteriaResponse;
import org.apache.fineract.client.models.PutProvisioningEntriesRequest;
import org.apache.fineract.client.models.PutProvisioningEntriesResponse;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

public class ProvisioningTransactionHelper {

    public ProvisioningTransactionHelper() {}

    public List<ProvisioningCategoryData> retrieveAllProvisioningCategories() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCategory().retrieveAll8());
    }

    public PostProvisioningCriteriaResponse createProvisioningCriteria(final PostProvisioningCriteriaRequest request) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCriteria().createProvisioningCriteria(request));
    }

    public GetProvisioningCriteriaCriteriaIdResponse retrieveProvisioningCriteria(final Long criteriaId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCriteria()
                .retrieveOneProvisioningCriteria(criteriaId));
    }

    public PutProvisioningCriteriaResponse updateProvisioningCriteria(final Long criteriaId, final PutProvisioningCriteriaRequest request) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCriteria().updateProvisioningCriteria(criteriaId,
                request));
    }

    public CommandProcessingResult createProvisioningCategory(final String categoryJson) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCategory().createProvisioningCategory(categoryJson));
    }

    public CommandProcessingResult deleteProvisioningCategory(final Long categoryId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCategory().deleteProvisioningCategory(categoryId));
    }

    public CallFailedRuntimeException deleteProvisioningCategoryExpectingError(final Long categoryId) {
        try {
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCategory().deleteProvisioningCategory(categoryId));
            return null;
        } catch (CallFailedRuntimeException e) {
            return e;
        }
    }

    public DeleteProvisioningCriteriaResponse deleteProvisioningCriteria(final Long criteriaId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCriteria().deleteProvisioningCriteria(criteriaId));
    }

    public PostProvisioningEntriesResponse createProvisioningEntries(final ProvisionEntryRequest request) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningEntries().createProvisioningEntries(request));
    }

    public PutProvisioningEntriesResponse updateProvisioningEntry(final String command, final Long entryId,
            PutProvisioningEntriesRequest request) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningEntries().modifyProvisioningEntry(entryId, command,
                request));
    }

    public ProvisioningEntryData retrieveProvisioningEntry(final Long provisioningEntry) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningEntries()
                .retrieveOneProvisioningEntry(provisioningEntry));
    }

    public PageLoanProductProvisioningEntryData retrieveProvisioningEntries(final Long provisioningEntry) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningEntries()
                .retrieveProvisioningEntriesLoanProducts(provisioningEntry, null, null, null, null, null));
    }

    public PageProvisioningEntryData retrieveAllProvisioningEntries() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningEntries()
                .retrieveAllProvisioningEntries((Integer) null, (Integer) null));
    }
}
