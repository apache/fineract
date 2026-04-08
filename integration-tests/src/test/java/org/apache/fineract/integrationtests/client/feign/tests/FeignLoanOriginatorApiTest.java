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
package org.apache.fineract.integrationtests.client.feign.tests;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetLoanOriginatorTemplateResponse;
import org.apache.fineract.client.models.GetLoanOriginatorsResponse;
import org.apache.fineract.client.models.PostLoanOriginatorsRequest;
import org.apache.fineract.client.models.PutLoanOriginatorsRequest;
import org.apache.fineract.client.models.PutLoanOriginatorsResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanOriginatorHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Slf4j
@Order(1)
public class FeignLoanOriginatorApiTest extends FeignIntegrationTest {

    private static FeignLoanOriginatorHelper originatorHelper;
    private static FeignClientHelper clientHelper;
    private static FeignLoanHelper loanHelper;

    @BeforeAll
    public static void setup() {
        FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        originatorHelper = new FeignLoanOriginatorHelper(fineractClient);
        clientHelper = new FeignClientHelper(fineractClient);
        loanHelper = new FeignLoanHelper(fineractClient);
    }

    @Test
    public void testCreateOriginatorWithMinimalData() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();

        final Long originatorId = originatorHelper.createOriginator(externalId);

        assertThat(originatorId).isNotNull();

        final GetLoanOriginatorsResponse originator = originatorHelper.getOriginatorById(originatorId);

        assertThat(originator.getExternalId()).isEqualTo(externalId);
        assertThat(originator.getStatus()).isEqualTo("ACTIVE");

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateOriginatorWithAllFields() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final String name = "Test Originator";
        final String status = "PENDING";

        final Long originatorId = originatorHelper.createOriginator(externalId, name, status);

        assertThat(originatorId).isNotNull();

        final GetLoanOriginatorsResponse originator = originatorHelper.getOriginatorById(originatorId);

        assertThat(originator.getExternalId()).isEqualTo(externalId);
        assertThat(originator.getName()).isEqualTo(name);
        assertThat(originator.getStatus()).isEqualTo(status);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateOriginatorWithAllFieldsUsingTemplate() {
        final String name = Utils.randomStringGenerator("Originator ", 30);

        final GetLoanOriginatorTemplateResponse originatorTemplate = originatorHelper.retrieveLoanOriginatorTemplate();
        assertThat(originatorTemplate).isNotNull();

        final String status = originatorTemplate.getStatusOptions().iterator().next();
        final GetCodeValuesDataResponse originatorTypeCode = originatorTemplate.getOriginatorTypeOptions().get(0);
        final GetCodeValuesDataResponse channelTypeCode = originatorTemplate.getChannelTypeOptions().get(0);

        final Long originatorId = originatorHelper
                .createOriginator(new PostLoanOriginatorsRequest().externalId(originatorTemplate.getExternalId()).name(name).status(status)
                        .originatorTypeId(originatorTypeCode.getId()).channelTypeId(channelTypeCode.getId()));

        final GetLoanOriginatorsResponse originator = originatorHelper.getOriginatorById(originatorId);

        assertThat(originator.getExternalId()).isEqualTo(originatorTemplate.getExternalId());
        assertThat(originator.getName()).isEqualTo(name);
        assertThat(originator.getStatus()).isEqualTo(status);
        assertThat(originator.getOriginatorType().getName()).isEqualTo(originatorTypeCode.getName());
        assertThat(originator.getChannelType().getName()).isEqualTo(channelTypeCode.getName());

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testRetrieveOriginatorById() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(externalId);

        final GetLoanOriginatorsResponse originator = originatorHelper.getOriginatorById(originatorId);

        assertThat(originator).isNotNull();
        assertThat(originator.getId()).isEqualTo(originatorId);
        assertThat(originator.getExternalId()).isEqualTo(externalId);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testRetrieveOriginatorByExternalId() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(externalId);

        final GetLoanOriginatorsResponse originator = originatorHelper.getOriginatorByExternalId(externalId);

        assertThat(originator).isNotNull();
        assertThat(originator.getId()).isEqualTo(originatorId);
        assertThat(originator.getExternalId()).isEqualTo(externalId);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testRetrieveAllOriginators() {
        final String externalId1 = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final String externalId2 = FeignLoanOriginatorHelper.generateUniqueExternalId();

        final Long originatorId1 = originatorHelper.createOriginator(externalId1);
        final Long originatorId2 = originatorHelper.createOriginator(externalId2);

        final List<GetLoanOriginatorsResponse> originators = originatorHelper.getAllOriginators();

        assertThat(originators).isNotNull();
        assertThat(originators).hasSizeGreaterThanOrEqualTo(2);

        originatorHelper.deleteOriginator(originatorId1);
        originatorHelper.deleteOriginator(originatorId2);
    }

    @Test
    public void testUpdateOriginatorPartially() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(externalId, "Original Name", "ACTIVE");

        final PutLoanOriginatorsResponse updateResult = originatorHelper.updateOriginator(originatorId,
                new PutLoanOriginatorsRequest().name("Updated Name"));

        assertThat(updateResult).isNotNull();

        final GetLoanOriginatorsResponse originator = originatorHelper.getOriginatorById(originatorId);
        assertThat(originator.getName()).isEqualTo("Updated Name");
        assertThat(originator.getStatus()).isEqualTo("ACTIVE");

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testUpdateOriginatorByExternalId() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        originatorHelper.createOriginator(externalId, "Original Name", "ACTIVE");

        final PutLoanOriginatorsResponse updateResult = originatorHelper.updateOriginatorByExternalId(externalId,
                new PutLoanOriginatorsRequest().name("Updated via ExternalId"));

        assertThat(updateResult).isNotNull();

        final GetLoanOriginatorsResponse originator = originatorHelper.getOriginatorByExternalId(externalId);
        assertThat(originator.getName()).isEqualTo("Updated via ExternalId");

        originatorHelper.deleteOriginatorByExternalId(externalId);
    }

    @Test
    public void testDeleteOriginator() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(externalId);

        final Long deletedId = originatorHelper.deleteOriginator(originatorId);
        assertThat(deletedId).isEqualTo(originatorId);

        final CallFailedRuntimeException exception = originatorHelper.getOriginatorByIdExpectingError(originatorId);
        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteOriginatorByExternalId() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(externalId);

        final Long deletedId = originatorHelper.deleteOriginatorByExternalId(externalId);
        assertThat(deletedId).isEqualTo(originatorId);

        final CallFailedRuntimeException exception = originatorHelper.getOriginatorByExternalIdExpectingError(externalId);
        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Test
    public void testCreateOriginatorWithOptionalName() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest().externalId(externalId);

        final Long originatorId = originatorHelper.createOriginator(request);
        assertThat(originatorId).isNotNull();

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateOriginatorWithDuplicateExternalId() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();

        final Long originatorId = originatorHelper.createOriginator(externalId);

        final CallFailedRuntimeException exception = originatorHelper
                .createOriginatorExpectingError(new PostLoanOriginatorsRequest().externalId(externalId).name("Duplicate"));
        assertThat(exception.getStatus()).isEqualTo(403);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateOriginatorWithInvalidStatus() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest().externalId(externalId).name("Test").status("INVALID");

        final CallFailedRuntimeException exception = originatorHelper.createOriginatorExpectingError(request);
        assertThat(exception.getStatus()).isEqualTo(403);
    }

    @Test
    public void testGetOriginatorByNonExistentId() {
        final CallFailedRuntimeException exception = originatorHelper.getOriginatorByIdExpectingError(999999L);
        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Test
    public void testGetOriginatorByNonExistentExternalId() {
        final CallFailedRuntimeException exception = originatorHelper.getOriginatorByExternalIdExpectingError("NON-EXISTENT-EXTERNAL-ID");
        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Test
    public void testUpdateNonExistentOriginator() {
        final CallFailedRuntimeException exception = originatorHelper.updateOriginatorExpectingError(999999L,
                new PutLoanOriginatorsRequest().name("Updated"));
        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteNonExistentOriginator() {
        final CallFailedRuntimeException exception = originatorHelper.deleteOriginatorExpectingError(999999L);
        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Test
    public void testCreateOriginatorWithInvalidCodeValueId() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest().externalId(externalId).name("Test")
                .originatorTypeId(999999L);

        final CallFailedRuntimeException exception = originatorHelper.createOriginatorExpectingError(request);
        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Test
    public void testAttachOriginatorToSubmittedLoan() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);

        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        originatorHelper.attachOriginatorToLoan(loanId, originatorId);

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testDetachOriginatorFromLoan() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);

        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        originatorHelper.attachOriginatorToLoan(loanId, originatorId);
        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testAttachInactiveOriginatorReturns403() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Inactive Test", "INACTIVE");

        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        final CallFailedRuntimeException exception = originatorHelper.attachOriginatorToLoanExpectingError(loanId, originatorId);
        assertThat(exception.getStatus()).isEqualTo(403);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testAttachSameOriginatorTwiceReturns403() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);

        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        originatorHelper.attachOriginatorToLoan(loanId, originatorId);

        final CallFailedRuntimeException exception = originatorHelper.attachOriginatorToLoanExpectingError(loanId, originatorId);
        assertThat(exception.getStatus()).isEqualTo(403);

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testDetachNonAttachedOriginatorReturns404() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);

        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        final CallFailedRuntimeException exception = originatorHelper.detachOriginatorFromLoanExpectingError(loanId, originatorId);
        assertThat(exception.getStatus()).isEqualTo(404);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testAttachOriginatorToApprovedLoanReturns403() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);

        final Long clientId = clientHelper.createClient();
        final Long loanProductId = loanHelper.createSimpleLoanProduct();
        final String todayDate = org.apache.fineract.integrationtests.common.Utils.dateFormatter
                .format(org.apache.fineract.integrationtests.common.Utils.getLocalDateOfTenant());
        final Long loanId = loanHelper.applyAndApproveLoan(clientId, loanProductId, todayDate, 10000.0, 12);

        final CallFailedRuntimeException exception = originatorHelper.attachOriginatorToLoanExpectingError(loanId, originatorId);
        assertThat(exception.getStatus()).isEqualTo(403);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testRetrieveLoanWithOriginatorsAssociation() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);

        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        originatorHelper.attachOriginatorToLoan(loanId, originatorId);

        final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");

        assertThat(loanDetails.getOriginators()).isNotNull();
        assertThat(loanDetails.getOriginators()).hasSize(1);
        assertThat(loanDetails.getOriginators().get(0).getId()).isEqualTo(originatorId);
        assertThat(loanDetails.getOriginators().get(0).getExternalId()).isEqualTo(originatorExternalId);

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testRetrieveLoanWithAllAssociationsIncludesOriginators() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);

        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        originatorHelper.attachOriginatorToLoan(loanId, originatorId);

        final var loanDetails = loanHelper.getLoanDetailsWithAssociationsAndExclude(loanId, "all", "guarantors,futureSchedule");

        assertThat(loanDetails.getOriginators()).isNotNull();
        assertThat(loanDetails.getOriginators()).isNotEmpty();
        assertThat(loanDetails.getOriginators().get(0).getId()).isEqualTo(originatorId);

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testRetrieveLoanWithNoOriginatorsReturnsEmptyList() {
        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");

        assertThat(loanDetails.getOriginators()).isNotNull();
        assertThat(loanDetails.getOriginators()).isEmpty();
    }

    @Test
    public void testRetrieveLoanExcludeOriginatorsFromAll() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);

        final Long clientId = clientHelper.createClient();
        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        originatorHelper.attachOriginatorToLoan(loanId, originatorId);

        final var loanDetails = loanHelper.getLoanDetailsWithAssociationsAndExclude(loanId, "all", "originators,guarantors,futureSchedule");

        assertThat(loanDetails.getOriginators()).isNull();

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
        originatorHelper.deleteOriginator(originatorId);
    }
}
