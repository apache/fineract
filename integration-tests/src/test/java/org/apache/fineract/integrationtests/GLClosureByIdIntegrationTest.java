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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants.ISO_DATE_PATTERN;
import static org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants.LOCALE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateResponse.TypeEnum;
import org.apache.fineract.client.models.GetGlClosureResponse;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.OfficeData;
import org.apache.fineract.client.models.PostGlClosuresRequest;
import org.apache.fineract.client.models.PostGlClosuresResponse;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PutGlClosuresRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GLClosureByIdIntegrationTest extends FeignIntegrationTest {

    private static final Long MISSING_CLOSURE_ID = -1L;
    private static final String USER_PASSWORD = "QwE!5rTy#9uP0";
    private static final Map<String, Object> TEMPLATE_QUERY = Map.of("template", true);

    private final List<Long> closureIds = new ArrayList<>();
    private final List<Executable> userAndRoleCleanup = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        final List<Executable> cleanup = new ArrayList<>();
        for (final Long closureId : closureIds) {
            cleanup.add(() -> ok(() -> fineractClient().accountingClosure().deleteGLClosure(closureId)));
        }
        cleanup.addAll(userAndRoleCleanup);
        try {
            assertAll(cleanup);
        } finally {
            closureIds.clear();
            userAndRoleCleanup.clear();
        }
    }

    @Test
    void retrievesTheRequestedClosureWithItsOfficeAndAuditDetails() {
        final LocalDate otherClosingDate = currentBusinessDate().minusDays(1);
        final LocalDate closingDate = otherClosingDate.minusDays(1);
        final GetOfficesResponse office = createOffice(closingDate.minusDays(1));
        final Long officeId = office.getId();
        final String officeName = office.getName();
        assertNotNull(officeId);
        assertNotNull(officeName);

        final Long roleId = FeignRoleHelper.createRole();
        assertNotNull(roleId);
        userAndRoleCleanup.addFirst(() -> FeignRoleHelper.deleteRole(roleId));
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of("CREATE_GLCLOSURE", true, "UPDATE_GLCLOSURE", true));

        final String creatorName = Utils.uniqueRandomStringGenerator("ClosureCreator", 8);
        final Long creatorId = createUser(roleId, creatorName).getResourceId();
        assertNotNull(creatorId);
        final FineractFeignClient creatorClient = FineractFeignClientHelper.createNewFineractFeignClient(creatorName, USER_PASSWORD);
        final String updaterName = Utils.uniqueRandomStringGenerator("ClosureUpdater", 8);
        final Long updaterId = createUser(roleId, updaterName).getResourceId();
        assertNotNull(updaterId);
        final FineractFeignClient updaterClient = FineractFeignClientHelper.createNewFineractFeignClient(updaterName, USER_PASSWORD);

        final Long closureId = createClosure(creatorClient, officeId, closingDate, "Initial comment").getResourceId();
        assertNotNull(closureId);
        final Long otherClosureId = createClosure(creatorClient, officeId, otherClosingDate, null).getResourceId();
        assertNotNull(otherClosureId);

        final String updatedComment = "Updated comment";
        ok(() -> updaterClient.accountingClosure().updateGLClosure(closureId, new PutGlClosuresRequest().comments(updatedComment)));

        final GetGlClosureResponse listedClosure = ok(() -> fineractClient().accountingClosure().retrieveAllGLClosures(officeId)).stream()
                .filter(candidate -> closureId.equals(candidate.getId())).findFirst().orElseThrow();
        final LocalDate expectedCreatedDate = listedClosure.getCreatedDate();
        final LocalDate expectedLastUpdatedDate = listedClosure.getLastUpdatedDate();
        assertNotNull(expectedCreatedDate);
        assertNotNull(expectedLastUpdatedDate);

        final ApiResponse<GetGlClosureResponse> response = ok(
                () -> fineractClient().accountingClosure().retrieveOneGLClosureWithHttpInfo(closureId, Map.of()));
        assertEquals(200, response.getStatusCode());
        final GetGlClosureResponse closure = response.getData();
        assertNotNull(closure);
        final GetGlClosureResponse expectedClosure = new GetGlClosureResponse().id(closureId).officeId(officeId).officeName(officeName)
                .closingDate(closingDate).deleted(false).createdByUserId(creatorId).createdByUsername(creatorName)
                .createdDate(expectedCreatedDate).lastUpdatedByUserId(updaterId).lastUpdatedByUsername(updaterName)
                .lastUpdatedDate(expectedLastUpdatedDate).comments(updatedComment);
        assertThat(closure).usingRecursiveComparison().ignoringFields("allowedOffices").isEqualTo(expectedClosure);

        final GetGlClosureResponse otherClosure = ok(() -> fineractClient().accountingClosure().retrieveOneGLClosure(otherClosureId));
        assertNotNull(otherClosure);
        final Long retrievedOtherClosureId = otherClosure.getId();
        final LocalDate retrievedOtherClosingDate = otherClosure.getClosingDate();
        assertNotNull(retrievedOtherClosureId);
        assertNotNull(retrievedOtherClosingDate);
        assertEquals(otherClosureId, retrievedOtherClosureId);
        assertEquals(otherClosingDate, retrievedOtherClosingDate);
        assertNull(otherClosure.getComments());
    }

    @Test
    void includesOfficeOptionsWhenTheTemplateIsRequested() {
        final LocalDate closingDate = currentBusinessDate().minusDays(1);
        final GetOfficesResponse office = createOffice(closingDate.minusDays(1));
        final Long officeId = office.getId();
        final String officeName = office.getName();
        assertNotNull(officeId);
        assertNotNull(officeName);

        final Long closureId = createClosure(fineractClient(), officeId, closingDate, "For the template").getResourceId();
        assertNotNull(closureId);

        final GetGlClosureResponse template = ok(
                () -> fineractClient().accountingClosure().retrieveOneGLClosureUniversal(closureId, TEMPLATE_QUERY));
        final GetGlClosureResponse closure = ok(() -> fineractClient().accountingClosure().retrieveOneGLClosure(closureId));
        assertNotNull(template);
        assertNotNull(closure);

        final Long templateClosureId = template.getId();
        assertNotNull(templateClosureId);
        assertEquals(closureId, templateClosureId);
        assertThat(template).usingRecursiveComparison().ignoringFields("allowedOffices").isEqualTo(closure);
        final List<OfficeData> allowedOffices = template.getAllowedOffices();
        assertNotNull(allowedOffices);
        final List<OfficeData> matchingOffices = allowedOffices.stream().filter(option -> officeId.equals(option.getId())).toList();
        assertThat(matchingOffices).hasSize(1);
        final String matchingOfficeName = matchingOffices.getFirst().getName();
        assertNotNull(matchingOfficeName);
        assertEquals(officeName, matchingOfficeName);

        final List<OfficeData> plainClosureOffices = closure.getAllowedOffices();
        assertNotNull(plainClosureOffices);
        assertThat(plainClosureOffices).isEmpty();
    }

    @Test
    void returnsNotFoundForMissingAndDeletedClosures() {
        assertClosureNotFound(fail(() -> fineractClient().accountingClosure().retrieveOneGLClosure(MISSING_CLOSURE_ID)));
        assertClosureNotFound(
                fail(() -> fineractClient().accountingClosure().retrieveOneGLClosureUniversal(MISSING_CLOSURE_ID, TEMPLATE_QUERY)));

        final LocalDate closingDate = currentBusinessDate().minusDays(1);
        final Long officeId = createOffice(closingDate.minusDays(1)).getId();
        assertNotNull(officeId);
        final Long closureId = createClosure(fineractClient(), officeId, closingDate, null).getResourceId();
        assertNotNull(closureId);
        ok(() -> fineractClient().accountingClosure().deleteGLClosure(closureId));
        closureIds.remove(closureId);

        assertClosureNotFound(fail(() -> fineractClient().accountingClosure().retrieveOneGLClosure(closureId)));
    }

    private LocalDate currentBusinessDate() {
        final GlobalConfigurationPropertyData configuration = new FeignGlobalConfigurationHelper(fineractClient())
                .retrieveConfigurationByName("enable-business-date");
        assertNotNull(configuration);
        final Boolean businessDateEnabled = configuration.getEnabled();
        assertNotNull(businessDateEnabled);
        if (!businessDateEnabled) {
            return Utils.getLocalDateOfTenant();
        }
        final List<BusinessDateResponse> dates = ok(() -> fineractClient().businessDateManagement().getBusinessDates());
        final Optional<BusinessDateResponse> businessDate = dates.stream().filter(date -> TypeEnum.BUSINESS_DATE == date.getType())
                .findFirst();
        return businessDate.map(response -> {
            final LocalDate date = response.getDate();
            assertNotNull(date);
            return date;
        }).orElseGet(Utils::getLocalDateOfTenant);
    }

    private GetOfficesResponse createOffice(final LocalDate openingDate) {
        final FeignOfficeHelper officeHelper = new FeignOfficeHelper(fineractClient());
        final Long officeId = officeHelper.createOffice(openingDate).getResourceId();
        assertNotNull(officeId);
        return officeHelper.retrieveOffice(officeId);
    }

    private PostUsersResponse createUser(final Long roleId, final String username) {
        final PostUsersResponse user = FeignUserHelper.createUser(roleId, null, username, USER_PASSWORD);
        final Long userId = user.getResourceId();
        assertNotNull(userId);
        userAndRoleCleanup.addFirst(() -> FeignUserHelper.deleteUser(userId));
        return user;
    }

    private PostGlClosuresResponse createClosure(final FineractFeignClient client, final Long officeId, final LocalDate closingDate,
            final String comments) {
        final PostGlClosuresRequest request = new PostGlClosuresRequest().officeId(officeId).closingDate(closingDate).comments(comments)
                .locale(LOCALE).dateFormat(ISO_DATE_PATTERN);
        final PostGlClosuresResponse closure = ok(() -> client.accountingClosure().createGLClosure(request));
        final Long closureId = closure.getResourceId();
        assertNotNull(closureId);
        closureIds.addFirst(closureId);
        return closure;
    }

    private static void assertClosureNotFound(final CallFailedRuntimeException error) {
        assertEquals(404, error.getStatus());
        assertEquals("error.msg.resource.not.found", error.getUserMessageGlobalisationCode());
        assertEquals("error.msg.glclosure.id.invalid", FeignErrors.firstError(error).userMessageGlobalisationCode());
    }
}
