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
package org.apache.fineract.integrationtests.savings;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.FeignException;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetStandingInstructionsStandingInstructionIdResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.StandingInstructionUpdateRequest;
import org.apache.fineract.client.models.StandingInstructionUpdateResponse;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.StandingInstructionCommandsApi;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.StandingInstructionsHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * {@code PUT /standinginstructions/{id}} with {@code command=update} and {@code command=delete}, through the typed
 * command handlers, including the per-command authorization that replaced the permission check of the legacy command
 * pipeline.
 */
class StandingInstructionUpdateDeleteTest extends FeignSavingsTestBase {

    private static final Integer SAVINGS_ACCOUNT_TYPE = 2;
    private static final Integer STATUS_DELETED = 3;
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final Long HEAD_OFFICE_ID = 1L;

    private final StandingInstructionsHelper helper = new StandingInstructionsHelper();

    @Test
    void updateAppliesAndReportsTheChangedValues() {
        final Long instructionId = createInstruction();

        final StandingInstructionUpdateResponse response = helper.updateStandingInstruction(instructionId,
                new StandingInstructionUpdateRequest().recurrenceInterval(3).priority(1).allowPartialTransfer(true));

        assertEquals(instructionId, response.getResourceId());
        // priority 1 is what the instruction was created with, so it is not a change
        assertEquals(Map.of("recurrenceInterval", 3, "allowPartialTransfer", true), response.getChanges());
        assertEquals(3, helper.getStandingInstructionById(instructionId).getRecurrenceInterval());
    }

    @Test
    void updateWithAMalformedDateIsRejected() {
        final Long instructionId = createInstruction();

        final CallFailedRuntimeException failure = fail(
                () -> FineractFeignClientHelper.getFineractFeignClient().standingInstructions().updateStandingInstruction(instructionId,
                        "update", new StandingInstructionUpdateRequest().validFrom("31 Foo 2024").dateFormat("dd MMMM yyyy").locale("en")));

        assertEquals(400, failure.getStatus());
        assertTrue(failure.getResponseBody().contains("validFrom"), failure.getResponseBody());
    }

    @Test
    void createWithoutABodyIsRejected() {
        final CallFailedRuntimeException failure = fail(() -> FineractFeignClientHelper.getFineractFeignClient()
                .create(StandingInstructionCommandsApi.class).createStandingInstructionWithoutBody());

        assertEquals(400, failure.getStatus());
    }

    @Test
    void updateWithoutABodyIsRejected() {
        final Long instructionId = createInstruction();

        final CallFailedRuntimeException failure = fail(() -> FineractFeignClientHelper.getFineractFeignClient()
                .create(StandingInstructionCommandsApi.class).updateStandingInstructionWithoutBody(instructionId, "update"));

        assertEquals(400, failure.getStatus());
    }

    @Test
    void deleteMarksTheInstructionDeletedAndBlocksFurtherUpdates() {
        final Long instructionId = createInstruction();

        final StandingInstructionUpdateResponse deleteResponse = helper.deleteStandingInstruction(instructionId);
        assertEquals(instructionId, deleteResponse.getResourceId());
        assertEquals(Map.of("status", STATUS_DELETED), deleteResponse.getChanges());

        final GetStandingInstructionsStandingInstructionIdResponse deleted = helper.getStandingInstructionById(instructionId);
        assertEquals(STATUS_DELETED, deleted.getStatus().getId());
        final CallFailedRuntimeException failure = fail(() -> FineractFeignClientHelper.getFineractFeignClient().standingInstructions()
                .updateStandingInstruction(instructionId, "update", new StandingInstructionUpdateRequest().recurrenceInterval(2)));
        assertEquals(400, failure.getStatus());
    }

    @Test
    void updatePermissionDoesNotAllowDelete() {
        final Long instructionId = createInstruction();
        final FineractFeignClient user = createUserWithPermission("UPDATE_STANDINGINSTRUCTION");

        ok(() -> user.standingInstructions().updateStandingInstruction(instructionId, "update",
                new StandingInstructionUpdateRequest().recurrenceInterval(2)));
        // Spring Security rejects it before the API, so the 403 carries no Fineract error body
        assertThrows(FeignException.Forbidden.class, () -> user.standingInstructions().updateStandingInstruction(instructionId, "delete",
                new StandingInstructionUpdateRequest()));
        assertEquals(2, helper.getStandingInstructionById(instructionId).getRecurrenceInterval());
    }

    @Test
    void deletePermissionDoesNotAllowUpdate() {
        final Long instructionId = createInstruction();
        final FineractFeignClient user = createUserWithPermission("DELETE_STANDINGINSTRUCTION");

        assertThrows(FeignException.Forbidden.class, () -> user.standingInstructions().updateStandingInstruction(instructionId, "update",
                new StandingInstructionUpdateRequest().recurrenceInterval(2)));
        assertEquals(1, helper.getStandingInstructionById(instructionId).getRecurrenceInterval());

        ok(() -> user.standingInstructions().updateStandingInstruction(instructionId, "delete", new StandingInstructionUpdateRequest()));
        assertEquals(STATUS_DELETED, helper.getStandingInstructionById(instructionId).getStatus().getId());
    }

    private Long createInstruction() {
        final Long clientId = createClient();
        final Long productId = createDefaultSavingsProduct().getResourceId();
        final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        final Long fromSavingsId = createApproveActivateSavings(clientId, productId, today);
        final Long toSavingsId = createApproveActivateSavings(clientId, productId, today);
        return helper.createStandingInstruction(clientId, fromSavingsId, toSavingsId, SAVINGS_ACCOUNT_TYPE, SAVINGS_ACCOUNT_TYPE, today,
                null, null);
    }

    private FineractFeignClient createUserWithPermission(final String permission) {
        final String username = Utils.uniqueRandomStringGenerator("usr", 8);
        final Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of(permission, true));
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(HEAD_OFFICE_ID).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }
}
