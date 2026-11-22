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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.CalendarCreateRequest;
import org.apache.fineract.client.models.PostCentersCenterIdRequest;
import org.apache.fineract.client.models.PostGroupsGroupIdRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCenterHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.group.api.CenterCommand;
import org.apache.fineract.portfolio.group.api.GroupCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** The group and center write commands that go through the typed command pipeline. */
public class GroupCenterCommandIntegrationTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final Long HEAD_OFFICE_ID = 1L;
    private static final String MEETING_DATE = "07 March 2011";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignGroupHelper groupHelper = new FeignGroupHelper(client);
    private final FeignCenterHelper centerHelper = new FeignCenterHelper(client);
    private final FeignClientHelper clientHelper = new FeignClientHelper(client);
    private final FeignCodeHelper codeHelper = new FeignCodeHelper(client);

    /**
     * Every group command goes through the real security filter chain: a user with only READ_GROUP is refused every
     * write command, and only the read-only generateCollectionSheet gets past it (to fail on its missing body).
     */
    @Test
    public void everyGroupWriteCommandNeedsItsOwnPermission() {
        final Long groupId = groupHelper.createActiveGroup().getResourceId();
        final FineractFeignClient readOnlyUser = createUserWithPermissions(Map.of("READ_GROUP", true));

        for (final GroupCommand command : GroupCommand.values()) {
            final int status = statusOf(() -> readOnlyUser.groups().handleCommandsGroup(groupId, new PostGroupsGroupIdRequest(),
                    Map.of("command", command.getValue(), "roleId", 1L)));
            assertEquals(command.getPermission() == null ? 400 : 403, status, command.getValue());
        }
    }

    @Test
    public void everyCenterWriteCommandNeedsItsOwnPermission() {
        final Long centerId = centerHelper.createActiveCenter().getResourceId();
        final FineractFeignClient readOnlyUser = createUserWithPermissions(Map.of("READ_CENTER", true));

        for (final CenterCommand command : CenterCommand.values()) {
            final int status = statusOf(
                    () -> readOnlyUser.centers().handleCommandsCenter(centerId, new PostCentersCenterIdRequest(), command.getValue()));
            assertEquals(command.getPermission() == null ? 400 : 403, status, command.getValue());
        }
    }

    /** The permission of one command does not grant another one on the same endpoint. */
    @Test
    public void activatePermissionAllowsActivateOnly() {
        final Long groupId = groupHelper.createGroup().getResourceId();
        final FineractFeignClient user = createUserWithPermissions(Map.of("READ_GROUP", true, "ACTIVATE_GROUP", true));

        ok(() -> user.groups().handleCommandsGroup(groupId,
                new PostGroupsGroupIdRequest().activationDate("04 March 2011").dateFormat(DATE_FORMAT).locale("en"),
                Map.of("command", "activate")));

        assertTrue(groupHelper.isGroupActive(groupId));
        assertThrows(FeignException.Forbidden.class,
                () -> user.groups().handleCommandsGroup(groupId, new PostGroupsGroupIdRequest(), Map.of("command", "close")));
    }

    /**
     * Jersey serves {@code /groups/1/} as {@code /groups/1}, so the trailing slash must not skip the permission check.
     */
    @Test
    public void trailingSlashDoesNotBypassPermissions() {
        final Long groupId = groupHelper.createGroup().getResourceId();
        final Long centerId = centerHelper.createCenter(Utils.randomStringGenerator("Center_", 5), HEAD_OFFICE_ID).getResourceId();
        final String username = createUsernameWithPermissions(Map.of("READ_GROUP", true, "READ_CENTER", true));
        final String activate = "{\"activationDate\": \"04 March 2011\", \"dateFormat\": \"" + DATE_FORMAT + "\", \"locale\": \"en\"}";

        final RuntimeException group = assertThrows(RuntimeException.class,
                () -> FeignRawHttpHelper.post("/groups/" + groupId + "/?command=activate", activate, username, PASSWORD));
        final RuntimeException center = assertThrows(RuntimeException.class,
                () -> FeignRawHttpHelper.post("/centers/" + centerId + "/?command=activate", activate, username, PASSWORD));

        assertTrue(group.getMessage().startsWith("HTTP 403"), group.getMessage());
        assertTrue(center.getMessage().startsWith("HTTP 403"), center.getMessage());
        assertFalse(groupHelper.isGroupActive(groupId));
    }

    @Test
    public void unknownCommandIsRejectedAsBadRequest() {
        final Long groupId = groupHelper.createActiveGroup().getResourceId();

        assertEquals(400,
                statusOf(() -> client.groups().handleCommandsGroup(groupId, new PostGroupsGroupIdRequest(), Map.of("command", "bogus"))));
    }

    @Test
    public void assignUpdateAndUnassignRole() {
        final Long groupId = groupHelper.createActiveGroup().getResourceId();
        final Long clientId = clientHelper.createClient();
        groupHelper.associateClient(groupId, clientId);
        final Long leader = codeHelper.createCodeValue("GROUPROLE", Utils.uniqueRandomStringGenerator("Leader_", 4), 0);
        final Long secretary = codeHelper.createCodeValue("GROUPROLE", Utils.uniqueRandomStringGenerator("Secretary_", 4), 1);

        final JsonNode assigned = post("/groups/" + groupId + "?command=assignRole",
                "{\"clientId\": " + clientId + ", \"role\": " + leader + "}");
        final long roleId = assigned.get("resourceId").asLong();

        JsonNode roles = groupRoles(groupId);
        assertEquals(1, roles.size());
        assertEquals(leader, roles.get(0).get("role").get("id").asLong());
        assertEquals(clientId, roles.get(0).get("clientId").asLong());

        post("/groups/" + groupId + "?command=updateRole&roleId=" + roleId, "{\"role\": " + secretary + "}");
        roles = groupRoles(groupId);
        assertEquals(secretary, roles.get(0).get("role").get("id").asLong());

        // the role to unassign is identified by the query parameter alone, no request body is needed
        post("/groups/" + groupId + "?command=unassignRole&roleId=" + roleId, null);
        assertTrue(groupRoles(groupId).isEmpty());
    }

    @Test
    public void transferClientsBetweenGroups() {
        final Long sourceGroupId = groupHelper.createActiveGroup().getResourceId();
        final Long destinationGroupId = groupHelper.createActiveGroup().getResourceId();
        final Long clientId = clientHelper.createClient();
        groupHelper.associateClient(sourceGroupId, clientId);

        post("/groups/" + sourceGroupId + "?command=transferClients", "{\"destinationGroupId\": " + destinationGroupId
                + ", \"clients\": [{\"id\": " + clientId + "}], \"inheritDestinationGroupLoanOfficer\": true}");

        assertFalse(groupHelper.retrieveGroupMemberIds(sourceGroupId).contains(clientId));
        assertTrue(groupHelper.retrieveGroupMemberIds(destinationGroupId).contains(clientId));
    }

    @Test
    public void saveGroupCollectionSheet() {
        final Long groupId = groupHelper.createActiveGroup().getResourceId();
        final Long clientId = clientHelper.createClient();
        groupHelper.associateClient(groupId, clientId);
        final Long calendarId = ok(() -> client.calendar().createCalendar("groups", groupId, weeklyMeeting())).getResourceId();

        final JsonNode result = post("/groups/" + groupId + "?command=saveCollectionSheet",
                collectionSheet(calendarId, "\"clientsAttendance\": [{\"clientId\": " + clientId + ", \"attendanceType\": 1}]"));

        assertEquals(groupId, result.get("groupId").asLong());
        assertEquals(groupId, result.get("resourceId").asLong());
    }

    @Test
    public void saveCenterCollectionSheet() {
        final Long centerId = centerHelper.createActiveCenter().getResourceId();
        final Long calendarId = ok(
                () -> client.calendar().createCalendar("centers", centerId, weeklyMeeting().startDate("29 December 2014"))).getResourceId();

        final JsonNode result = post("/centers/" + centerId + "?command=saveCollectionSheet", "{\"calendarId\": " + calendarId
                + ", \"transactionDate\": \"05 January 2015\", \"dateFormat\": \"" + DATE_FORMAT + "\", \"locale\": \"en\"}");

        assertEquals(centerId, result.get("groupId").asLong());
    }

    @Test
    public void disassociateGroupsAndCloseCenter() {
        final Long centerId = centerHelper.createActiveCenter().getResourceId();
        final Long groupId = groupHelper.createGroup().getResourceId();
        centerHelper.associateGroups(centerId, List.of(groupId));

        post("/centers/" + centerId + "?command=disassociateGroups", "{\"groupMembers\": [" + groupId + "]}");
        final JsonNode groupMembers = get("/centers/" + centerId + "?associations=groupMembers").get("groupMembers");
        assertTrue(groupMembers == null || groupMembers.isEmpty(), String.valueOf(groupMembers));

        final Long closureReasonId = codeHelper.retrieveOrCreateCodeValueId("CenterClosureReason");
        post("/centers/" + centerId + "?command=close", "{\"closureDate\": \"30 December 2014\", \"closureReasonId\": " + closureReasonId
                + ", \"dateFormat\": \"" + DATE_FORMAT + "\", \"locale\": \"en\"}");

        assertFalse(Boolean.TRUE.equals(centerHelper.retrieveCenter(centerId).getActive()));
    }

    @Test
    public void closeGroup() {
        final Long groupId = groupHelper.createActiveGroup().getResourceId();
        final Long closureReasonId = codeHelper.retrieveOrCreateCodeValueId("GroupClosureReason");

        post("/groups/" + groupId + "?command=close", "{\"closureDate\": \"05 March 2011\", \"closureReasonId\": " + closureReasonId
                + ", \"dateFormat\": \"" + DATE_FORMAT + "\", \"locale\": \"en\"}");

        assertFalse(groupHelper.isGroupActive(groupId));
    }

    /**
     * The status of a failed call: a 403 from the security filter chain arrives as {@link FeignException}, an error
     * raised by Fineract itself as the client's own exception.
     */
    private static int statusOf(final Executable call) {
        final RuntimeException failure = assertThrows(RuntimeException.class, call);
        if (failure instanceof FeignException feignException) {
            return feignException.status();
        }
        if (failure instanceof org.apache.fineract.client.feign.FeignException fineractException) {
            return fineractException.status();
        }
        throw failure;
    }

    private static JsonNode post(final String path, final String body) {
        try {
            return MAPPER.readTree(FeignRawHttpHelper.post(path, body));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JsonNode get(final String path) {
        try {
            return MAPPER.readTree(FeignRawHttpHelper.get(path));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JsonNode groupRoles(final Long groupId) {
        final JsonNode roles = get("/groups/" + groupId + "?associations=groupRoles").get("groupRoles");
        return roles == null ? MAPPER.createArrayNode() : roles;
    }

    private static String collectionSheet(final Long calendarId, final String extra) {
        final List<String> fields = new ArrayList<>(List.of("\"calendarId\": " + calendarId,
                "\"transactionDate\": \"" + MEETING_DATE + "\"", "\"dateFormat\": \"" + DATE_FORMAT + "\"", "\"locale\": \"en\""));
        fields.add(extra);
        return "{" + String.join(", ", fields) + "}";
    }

    private static CalendarCreateRequest weeklyMeeting() {
        return new CalendarCreateRequest().title(Utils.randomStringGenerator("Meeting_", 4)).typeId(1).startDate("07 March 2011")
                .repeating(true).frequency(2).interval(1).repeatsOnDay(1).dateFormat(DATE_FORMAT).locale("en");
    }

    private static FineractFeignClient createUserWithPermissions(final Map<String, Boolean> permissions) {
        return FineractFeignClientHelper.createNewFineractFeignClient(createUsernameWithPermissions(permissions), PASSWORD);
    }

    private static String createUsernameWithPermissions(final Map<String, Boolean> permissions) {
        final String username = Utils.uniqueRandomStringGenerator("usr", 8);
        final Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, permissions);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(HEAD_OFFICE_ID).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return username;
    }
}
