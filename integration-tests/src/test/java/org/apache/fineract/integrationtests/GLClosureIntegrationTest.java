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

import static org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants.ISO_DATE_PATTERN;
import static org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants.LOCALE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.DeleteGlClosuresResponse;
import org.apache.fineract.client.models.GetGlClosureResponse;
import org.apache.fineract.client.models.PostGlClosuresRequest;
import org.apache.fineract.client.models.PostGlClosuresResponse;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.PutGlClosuresRequest;
import org.apache.fineract.integrationtests.ConfigProperties.Backend;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GLClosureIntegrationTest extends FeignIntegrationTest {

    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final LocalDate CLOSING_DATE = OFFICE_OPENING_DATE.plusDays(1);
    private static final LocalDate BUSINESS_DATE = CLOSING_DATE.plusDays(2);
    private static final String USER_PASSWORD = "GlClosureTest!2468";

    private final FeignBusinessDateHelper businessDateHelper = new FeignBusinessDateHelper(fineractClient());
    private final List<Long> createdClosureIds = new ArrayList<>();

    @AfterEach
    void deleteClosures() {
        try {
            for (final Long closureId : createdClosureIds.reversed()) {
                executeVoid(() -> fineractClient().accountingClosure().deleteGLClosure(closureId));
            }
        } finally {
            createdClosureIds.clear();
        }
    }

    @Test
    void listsClosuresAcrossOfficesWhenOfficeFilterIsOmittedOrZero() {
        businessDateHelper.runAt(BUSINESS_DATE.toString(), () -> {
            final Long firstOfficeId = createOffice().getOfficeId();
            assertNotNull(firstOfficeId);
            final Long secondOfficeId = createOffice().getOfficeId();
            assertNotNull(secondOfficeId);
            final LocalDate latestDate = CLOSING_DATE.plusDays(1);
            final Long earlierClosureId = createClosure(firstOfficeId, CLOSING_DATE, null).getResourceId();
            assertNotNull(earlierClosureId);
            final Long laterClosureId = createClosure(firstOfficeId, latestDate, null).getResourceId();
            assertNotNull(laterClosureId);
            final Long sameDateClosureId = createClosure(secondOfficeId, latestDate, null).getResourceId();
            assertNotNull(sameDateClosureId);

            final List<Long> officeIds = List.of(firstOfficeId, secondOfficeId);
            final List<GetGlClosureResponse> closuresWithoutFilter = ok(
                    () -> fineractClient().accountingClosure().retrieveAllGLClosuresUniversal(Map.of()));
            final List<GetGlClosureResponse> closuresWithZeroOfficeId = retrieveClosures(0L);
            assertUnfilteredClosures(closuresWithoutFilter, officeIds, earlierClosureId, laterClosureId, sameDateClosureId);
            assertUnfilteredClosures(closuresWithZeroOfficeId, officeIds, earlierClosureId, laterClosureId, sameDateClosureId);
            Assertions.assertThat(closuresWithZeroOfficeId).extracting(GetGlClosureResponse::getId)
                    .containsExactlyInAnyOrderElementsOf(closuresWithoutFilter.stream().map(GetGlClosureResponse::getId).toList());
        });
    }

    @Test
    void filtersClosuresByOfficeAndReturnsAnEmptyListWhenNoneMatch() {
        businessDateHelper.runAt(BUSINESS_DATE.toString(), () -> {
            final Long officeId = createOffice().getOfficeId();
            assertNotNull(officeId);
            final Long otherOfficeId = createOffice().getOfficeId();
            assertNotNull(otherOfficeId);
            final Long emptyOfficeId = createOffice().getOfficeId();
            assertNotNull(emptyOfficeId);
            final Long earlierClosureId = createClosure(officeId, CLOSING_DATE, null).getResourceId();
            assertNotNull(earlierClosureId);
            final Long laterClosureId = createClosure(officeId, CLOSING_DATE.plusDays(1), null).getResourceId();
            assertNotNull(laterClosureId);
            createClosure(otherOfficeId, BUSINESS_DATE, null);

            Assertions.assertThat(retrieveClosures(officeId)).extracting(GetGlClosureResponse::getId).containsExactly(laterClosureId,
                    earlierClosureId);
            for (final Long officeWithoutClosures : List.of(emptyOfficeId, Long.MAX_VALUE, -1L)) {
                final ApiResponse<List<GetGlClosureResponse>> response = ok(
                        () -> fineractClient().accountingClosure().retrieveAllGLClosuresWithHttpInfo(officeWithoutClosures, Map.of()));
                Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
                Assertions.assertThat(response.getData()).isEmpty();
            }
        });
    }

    @Test
    void returnsClosureFieldsWithDistinctCreatingAndUpdatingUsers() {
        businessDateHelper.runAt(BUSINESS_DATE.toString(), () -> {
            final Long officeId = createOffice().getOfficeId();
            assertNotNull(officeId);
            final String officeName = ok(() -> fineractClient().offices().retrieveOneOffice(officeId)).getName();
            final Long creatorId = FeignUserHelper.getUserIdByUsername(Backend.USERNAME);
            assertNotNull(creatorId);

            final Long closureWithoutComment = createClosure(officeId, CLOSING_DATE, null).getResourceId();
            assertNotNull(closureWithoutComment);
            final LocalDate closingDate = CLOSING_DATE.plusDays(1);
            final Long closureId = createClosure(officeId, closingDate, "Before review").getResourceId();
            assertNotNull(closureId);
            final String updaterName = Utils.uniqueRandomStringGenerator("ClosureUpdater", 8);
            final Long roleId = FeignRoleHelper.createRole();
            assertNotNull(roleId);
            try {
                FeignRoleHelper.addPermissionsToRole(roleId, Map.of("UPDATE_GLCLOSURE", true));
                final Long updaterId = FeignUserHelper.createUser(roleId, null, updaterName, USER_PASSWORD).getResourceId();
                assertNotNull(updaterId);
                try {
                    final FineractFeignClient updater = FineractFeignClientHelper.createNewFineractFeignClient(updaterName, USER_PASSWORD);
                    final String comments = "Reviewed closure";
                    ok(() -> updater.accountingClosure().updateGLClosure(closureId, new PutGlClosuresRequest().comments(comments)));

                    final GetGlClosureResponse details = ok(() -> fineractClient().accountingClosure().retrieveOneGLClosure(closureId));
                    Assertions.assertThat(details.getCreatedDate()).isNotNull();
                    Assertions.assertThat(details.getLastUpdatedDate()).isNotNull();
                    final GetGlClosureResponse expectedClosure = new GetGlClosureResponse().id(closureId).officeId(officeId)
                            .officeName(officeName).closingDate(closingDate).deleted(false).createdDate(details.getCreatedDate())
                            .lastUpdatedDate(details.getLastUpdatedDate()).createdByUserId(creatorId).createdByUsername(Backend.USERNAME)
                            .lastUpdatedByUserId(updaterId).lastUpdatedByUsername(updaterName).comments(comments);

                    final List<GetGlClosureResponse> closures = retrieveClosures(officeId);
                    Assertions.assertThat(closures).hasSize(2);
                    Assertions.assertThat(closures.getFirst()).isEqualTo(expectedClosure);
                    Assertions.assertThat(closures.getLast().getId()).isEqualTo(closureWithoutComment);
                    Assertions.assertThat(closures.getLast().getComments()).isNull();
                } finally {
                    FeignUserHelper.deleteUser(updaterId);
                }
            } finally {
                FeignRoleHelper.deleteRole(roleId);
            }
        });
    }

    @Test
    void removesTheDeletedClosureFromTheOfficeList() {
        businessDateHelper.runAt(BUSINESS_DATE.toString(), () -> {
            final Long officeId = createOffice().getOfficeId();
            assertNotNull(officeId);
            final Long earlierClosureId = createClosure(officeId, CLOSING_DATE, null).getResourceId();
            assertNotNull(earlierClosureId);
            final Long laterClosureId = createClosure(officeId, CLOSING_DATE.plusDays(1), null).getResourceId();
            assertNotNull(laterClosureId);
            Assertions.assertThat(retrieveClosures(officeId)).extracting(GetGlClosureResponse::getId).containsExactly(laterClosureId,
                    earlierClosureId);

            final DeleteGlClosuresResponse response = ok(() -> fineractClient().accountingClosure().deleteGLClosure(laterClosureId));
            createdClosureIds.remove(laterClosureId);

            Assertions.assertThat(response.getResourceId()).isEqualTo(laterClosureId);
            Assertions.assertThat(retrieveClosures(officeId)).extracting(GetGlClosureResponse::getId).containsExactly(earlierClosureId);
        });
    }

    private PostOfficesResponse createOffice() {
        return new FeignOfficeHelper(fineractClient()).createOffice(OFFICE_OPENING_DATE);
    }

    private PostGlClosuresResponse createClosure(final Long officeId, final LocalDate closingDate, final String comments) {
        final PostGlClosuresRequest request = new PostGlClosuresRequest().officeId(officeId).closingDate(closingDate)
                .dateFormat(ISO_DATE_PATTERN).locale(LOCALE).comments(comments);
        final PostGlClosuresResponse closure = ok(() -> fineractClient().accountingClosure().createGLClosure(request));
        final Long closureId = closure.getResourceId();
        assertNotNull(closureId);
        createdClosureIds.add(closureId);
        return closure;
    }

    private List<GetGlClosureResponse> retrieveClosures(final Long officeId) {
        return ok(() -> fineractClient().accountingClosure().retrieveAllGLClosures(officeId));
    }

    private static void assertUnfilteredClosures(final List<GetGlClosureResponse> closures, final List<Long> officeIds,
            final Long... expectedClosureIds) {
        Assertions.assertThat(closures).extracting(GetGlClosureResponse::getClosingDate).isSortedAccordingTo(Comparator.reverseOrder());
        Assertions.assertThat(closures).filteredOn(closure -> {
            final Long officeId = closure.getOfficeId();
            assertNotNull(officeId);
            return officeIds.contains(officeId);
        }).extracting(GetGlClosureResponse::getId).containsExactlyInAnyOrder(expectedClosureIds);
    }
}
