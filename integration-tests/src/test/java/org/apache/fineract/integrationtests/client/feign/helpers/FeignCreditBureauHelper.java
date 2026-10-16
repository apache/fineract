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
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.CreditBureauConfigurationData;
import org.apache.fineract.client.models.OrganisationCreditBureauData;
import org.apache.fineract.client.models.PostCreditBureauConfigurationRequest;
import org.apache.fineract.client.models.PostCreditBureauLoanProductMappingRequest;
import org.apache.fineract.client.models.PostOrganisationCreditBureauRequest;
import org.apache.fineract.client.models.PutCreditBureauConfigurationRequest;
import org.apache.fineract.client.models.PutOrganisationCreditBureauRequest;

public class FeignCreditBureauHelper {

    private final FineractFeignClient fineractClient;
    private final CreditBureauIntegrationCommandsApi creditBureauIntegrationApi;

    public FeignCreditBureauHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.creditBureauIntegrationApi = fineractClient.create(CreditBureauIntegrationCommandsApi.class);
    }

    public List<OrganisationCreditBureauData> retrieveOrganisationCreditBureaus() {
        return ok(() -> fineractClient.creditBureauConfiguration().getOrganisationCreditBureau());
    }

    public CommandProcessingResult addOrganisationCreditBureau(Long creditBureauId, String alias, boolean isActive) {
        return addOrganisationCreditBureau(creditBureauId, new PostOrganisationCreditBureauRequest().alias(alias).isActive(isActive));
    }

    public CommandProcessingResult addOrganisationCreditBureau(Long creditBureauId, PostOrganisationCreditBureauRequest request) {
        return ok(() -> fineractClient.creditBureauConfiguration().addOrganisationCreditBureau(creditBureauId, request));
    }

    public CallFailedRuntimeException addOrganisationCreditBureauExpectingError(Long creditBureauId,
            PostOrganisationCreditBureauRequest request) {
        return fail(() -> fineractClient.creditBureauConfiguration().addOrganisationCreditBureau(creditBureauId, request));
    }

    public CommandProcessingResult updateOrganisationCreditBureau(Long organisationCreditBureauId, boolean isActive) {
        PutOrganisationCreditBureauRequest request = new PutOrganisationCreditBureauRequest().creditBureauId(organisationCreditBureauId)
                .isActive(isActive);
        return ok(() -> fineractClient.creditBureauConfiguration().updateCreditBureau(request));
    }

    public List<CreditBureauConfigurationData> retrieveConfigurations(Long organisationCreditBureauId) {
        return ok(() -> fineractClient.creditBureauConfiguration().getConfiguration(organisationCreditBureauId));
    }

    public CommandProcessingResult createConfiguration(Long creditBureauId, PostCreditBureauConfigurationRequest request) {
        return ok(() -> fineractClient.creditBureauConfiguration().createCreditBureauConfiguration(creditBureauId, request));
    }

    public CallFailedRuntimeException createConfigurationExpectingError(Long creditBureauId, PostCreditBureauConfigurationRequest request) {
        return fail(() -> fineractClient.creditBureauConfiguration().createCreditBureauConfiguration(creditBureauId, request));
    }

    public CommandProcessingResult updateConfiguration(Long configurationId, String configKey, String value) {
        PutCreditBureauConfigurationRequest request = new PutCreditBureauConfigurationRequest().configkey(configKey).value(value);
        return ok(() -> fineractClient.creditBureauConfiguration().updateCreditBureauConfiguration(configurationId, request));
    }

    public CallFailedRuntimeException createLoanProductMappingExpectingError(Long organisationCreditBureauId,
            PostCreditBureauLoanProductMappingRequest request) {
        return fail(
                () -> fineractClient.creditBureauConfiguration().createCreditBureauLoanProductMapping(organisationCreditBureauId, request));
    }

    public CommandProcessingResult fetchCreditReport(String creditBureauId, String nrc) {
        return ok(() -> creditBureauIntegrationApi
                .fetchCreditReport(new CreditBureauIntegrationCommandsApi.CreditReportRequest(creditBureauId, nrc)));
    }
}
