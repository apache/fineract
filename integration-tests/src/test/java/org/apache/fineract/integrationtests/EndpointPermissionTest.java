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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CurrencyUpdateRequest;
import org.apache.fineract.client.models.HookCreateRequest;
import org.apache.fineract.client.models.MeetingCreateRequest;
import org.apache.fineract.client.models.NoteUpdateRequest;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PaymentTypeUpdateRequest;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.ProductMixCreateRequest;
import org.apache.fineract.client.models.ProductMixUpdateRequest;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.client.models.StaffCreateRequest;
import org.apache.fineract.client.models.TemplateCreateRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that a user whose role holds no permissions is rejected with 403 on collection and item routes of resources
 * whose authorization is configured through request matchers.
 */
public class EndpointPermissionTest extends FeignIntegrationTest {

    private static final String TEST_USER_PASSWORD = "AKleRbDhK421$";
    private static final Long HEAD_OFFICE_ID = 1L;
    private static final Long ANY_ID = 1L;

    private Long roleId;
    private FineractFeignClient userClient;

    @BeforeEach
    public void createUserWithoutPermissions() {
        final FineractFeignClient adminClient = fineractClient();

        final String roleName = Utils.uniqueRandomStringGenerator("TEST_ROLE_", 10);
        roleId = ok(() -> adminClient.roles().createRole(new PostRolesRequest().name(roleName).description("Role without permissions")))
                .getResourceId();

        final String firstname = Utils.randomFirstNameGenerator();
        final String lastname = Utils.randomLastNameGenerator();
        final String userName = Utils.uniqueRandomStringGenerator("testUserName", 4);
        ok(() -> adminClient.users()
                .createUser(new PostUsersRequest().addRolesItem(roleId).email(firstname + "." + lastname + "@whatever.mifos.org")
                        .firstname(firstname).lastname(lastname).repeatPassword(TEST_USER_PASSWORD).sendPasswordToEmail(false)
                        .officeId(HEAD_OFFICE_ID).username(userName).password(TEST_USER_PASSWORD)));

        userClient = FineractFeignClientHelper.createNewFineractFeignClient(userName, TEST_USER_PASSWORD);
    }

    @Test
    public void testHookEndpointsRequirePermission() {
        assertForbidden(c -> c.hooks().retrieveAllHooks());
        assertForbidden(c -> c.hooks().createHook(new HookCreateRequest()));
        assertForbidden(c -> c.hooks().retrieveHook(ANY_ID, (Boolean) null));

        // granting the read permission lets the same user list hooks, so the 403s above are authorization failures
        ok(() -> fineractClient().roles().updateRolePermissions(roleId,
                new PutRolesRoleIdPermissionsRequest().putPermissionsItem("READ_HOOK", true)));
        ok(() -> userClient.hooks().retrieveAllHooks());
    }

    @Test
    public void testStaffEndpointsRequirePermission() {
        assertForbidden(c -> c.staff().retrieveAllStaff(null, null, null, null));
        assertForbidden(c -> c.staff().createStaff(new StaffCreateRequest()));
    }

    @Test
    public void testMeetingEndpointsRequirePermission() {
        assertForbidden(c -> c.meetings().retrieveAllMeetings("groups", ANY_ID, (Integer) null));
        assertForbidden(c -> c.meetings().retrieveMeeting(ANY_ID, "groups", ANY_ID));
        assertForbidden(c -> c.meetings().createMeeting("groups", ANY_ID, new MeetingCreateRequest()));
    }

    @Test
    public void testBusinessDateAndCurrencyEndpointsRequirePermission() {
        assertForbidden(c -> c.businessDateManagement().getBusinessDates());
        assertForbidden(c -> c.currency().updateCurrencies(new CurrencyUpdateRequest()));
    }

    @Test
    public void testInterestRateChartEndpointsRequirePermission() {
        assertForbidden(c -> c.interestRateChart().retrieveAllInterestRateCharts((Long) null));
    }

    @Test
    public void testPaymentTypeEndpointsRequirePermission() {
        assertForbidden(c -> c.paymentType().getAllPaymentTypes((Boolean) null));
        assertForbidden(c -> c.paymentType().createPaymentType(new PaymentTypeCreateRequest()));
        assertForbidden(c -> c.paymentType().updatePaymentType(ANY_ID, new PaymentTypeUpdateRequest()));
        assertForbidden(c -> c.paymentType().deleteCodePaymentType(ANY_ID));
    }

    @Test
    public void testProductMixEndpointsRequirePermission() {
        assertForbidden(c -> c.productMix().retrieveTemplateProductMix(ANY_ID));
        assertForbidden(c -> c.productMix().createProductMix(ANY_ID, new ProductMixCreateRequest()));
        assertForbidden(c -> c.productMix().updateProductMix(ANY_ID, new ProductMixUpdateRequest()));
        assertForbidden(c -> c.productMix().deleteProductMix(ANY_ID));
    }

    @Test
    public void testTemplateEndpointsRequirePermission() {
        assertForbidden(c -> c.templates().retrieveAllTemplates((Integer) null, (Integer) null));
        assertForbidden(c -> c.templates().createTemplate(new TemplateCreateRequest()));
    }

    @Test
    public void testNoteAndDocumentItemEndpointsRequirePermission() {
        assertForbidden(c -> c.notes().retrieveNote("clients", ANY_ID, ANY_ID));
        assertForbidden(c -> c.notes().updateNote("clients", ANY_ID, ANY_ID, new NoteUpdateRequest()));
        assertForbidden(c -> c.notes().deleteNote("clients", ANY_ID, ANY_ID));
        assertForbidden(c -> c.documents().getDocument("clients", ANY_ID, ANY_ID));
        assertForbidden(c -> c.documents().deleteDocument("clients", ANY_ID, ANY_ID));
    }

    private void assertForbidden(final Function<FineractFeignClient, ?> call) {
        final CallFailedRuntimeException exception = fail(() -> call.apply(userClient));
        assertEquals(403, exception.getStatus());
    }
}
