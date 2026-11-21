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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetRolesRoleIdResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RolesTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(RolesTest.class);

    private FeignStaffHelper staffHelper;

    @BeforeAll
    public void setup() {
        staffHelper = new FeignStaffHelper(fineractClient());
    }

    @Test
    public void testRoleFieldsThroughJerseyContextInjection() throws Exception {
        final Long roleId = FeignRoleHelper.createRole();
        try {
            final GetRolesRoleIdResponse createdRole = FeignRoleHelper.retrieveRole(roleId);
            final var roles = ok(() -> fineractClient().roles().retrieveAllRolesUniversal(Map.of("fields", "name")));
            Assertions.assertFalse(roles.isEmpty());
            Assertions.assertTrue(roles.stream().anyMatch(role -> createdRole.getName().equals(role.getName())));
            for (final var role : roles) {
                Assertions.assertNotNull(role.getName());
                Assertions.assertNull(role.getId());
                Assertions.assertNull(role.getDescription());
                // Gson's legacy filter leaves disabled present even when only name is requested.
                Assertions.assertNotNull(role.getDisabled());
            }

            final CallFailedRuntimeException exception = FeignCalls
                    .fail(() -> fineractClient().roles().retrieveAllRolesUniversal(Map.of("fields", "foo")));
            assertEquals(400, exception.getStatus());
            final JsonNode errors = new ObjectMapper().readTree(exception.getResponseBody()).path("errors");
            assertEquals(1, errors.size());
            assertEquals("error.msg.parameter.unsupported", errors.get(0).path("userMessageGlobalisationCode").asText());
            assertEquals("foo", errors.get(0).path("parameterName").asText());
        } finally {
            FeignRoleHelper.deleteRole(roleId);
        }
    }

    @Test
    public void testUserStaffDateThroughJerseyWriter() throws Exception {
        final Long roleId = FeignRoleHelper.createRole();
        Long userId = null;
        try {
            final Long staffId = staffHelper.createStaff(FeignStaffHelper.DEFAULT_OFFICE_ID, "20 September 2011").getResourceId();
            final Long createdUserId = FeignUserHelper.createUser(roleId, staffId, Utils.uniqueRandomStringGenerator("User_Date_", 5))
                    .getResourceId();
            userId = createdUserId;
            final RawUserApi users = fineractClient().create(RawUserApi.class);
            try (Response response = ok(() -> users.retrieveUser(createdUserId))) {
                assertEquals(200, response.status());
                final JsonNode body = new ObjectMapper().readTree(response.body().asReader(StandardCharsets.UTF_8));
                final JsonNode joiningDate = body.path("staff").path("joiningDate");
                Assertions.assertTrue(joiningDate.isArray(), body.toString());
                assertEquals(new ObjectMapper().readTree("[2011,9,20]"), joiningDate);
            }
        } finally {
            if (userId != null) {
                FeignUserHelper.deleteUser(userId);
            }
            FeignRoleHelper.deleteRole(roleId);
        }
    }

    public interface RawUserApi {

        @RequestLine("GET /v1/users/{userId}")
        Response retrieveUser(@Param("userId") Long userId);
    }

    @Test
    public void testCreateRolesStatus() {

        LOG.info("---------------------------------CREATING A ROLE---------------------------------------------");
        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        GetRolesRoleIdResponse role = FeignRoleHelper.retrieveRole(roleId);
        assertEquals(role.getId(), roleId);

    }

    @Test
    public void testDisableRolesStatus() {

        LOG.info("---------------------------------CREATING A ROLE---------------------------------------------");
        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        GetRolesRoleIdResponse role = FeignRoleHelper.retrieveRole(roleId);
        assertEquals(role.getId(), roleId);

        LOG.info("--------------------------------- DISABLING ROLE -------------------------------");
        final Long disableRoleId = FeignRoleHelper.disableRole(roleId).getResourceId();
        assertEquals(disableRoleId, roleId);
        role = FeignRoleHelper.retrieveRole(roleId);
        assertEquals(role.getId(), roleId);
        assertEquals(true, role.getDisabled());

    }

    @Test
    public void testEnableRolesStatus() {

        LOG.info("---------------------------------CREATING A ROLE---------------------------------------------");
        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        GetRolesRoleIdResponse role = FeignRoleHelper.retrieveRole(roleId);
        assertEquals(role.getId(), roleId);

        LOG.info("--------------------------------- DISABLING ROLE -------------------------------");
        final Long disableRoleId = FeignRoleHelper.disableRole(roleId).getResourceId();
        assertEquals(disableRoleId, roleId);
        role = FeignRoleHelper.retrieveRole(roleId);
        assertEquals(role.getId(), roleId);
        assertEquals(true, role.getDisabled());

        LOG.info("--------------------------------- ENABLING ROLE -------------------------------");
        final Long enableRoleId = FeignRoleHelper.enableRole(roleId).getResourceId();
        assertEquals(enableRoleId, roleId);
        role = FeignRoleHelper.retrieveRole(roleId);
        assertEquals(role.getId(), roleId);
        assertEquals(false, role.getDisabled());

    }

    @Test
    public void testDeleteRoleStatus() {

        LOG.info("-------------------------------- CREATING A ROLE---------------------------------------------");
        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        GetRolesRoleIdResponse role = FeignRoleHelper.retrieveRole(roleId);
        assertEquals(role.getId(), roleId);

        LOG.info("--------------------------------- DELETE ROLE -------------------------------");
        final Long deleteRoleId = FeignRoleHelper.deleteRole(roleId).getResourceId();
        assertEquals(deleteRoleId, roleId);
    }

    @Test
    public void testRoleShouldGetDeletedIfNoActiveUserExists() {
        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        final Long staffId = staffHelper.createStaff().getResourceId();
        Assertions.assertNotNull(staffId);

        final Long userId = FeignUserHelper.createUser(roleId, staffId, Utils.uniqueRandomStringGenerator("User_Name_", 3)).getResourceId();
        Assertions.assertNotNull(userId);

        final Long deletedUserId = FeignUserHelper.deleteUser(userId).getResourceId();
        Assertions.assertEquals(deletedUserId, userId);

        final Long deletedRoleId = FeignRoleHelper.deleteRole(roleId).getResourceId();
        assertEquals(deletedRoleId, roleId);
    }

    @Test
    public void testRoleShouldNotGetDeletedIfActiveUserExists() {
        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        final Long staffId = staffHelper.createStaff().getResourceId();
        Assertions.assertNotNull(staffId);

        final Long userId = FeignUserHelper.createUser(roleId, staffId, Utils.uniqueRandomStringGenerator("User_Name_", 3)).getResourceId();
        Assertions.assertNotNull(userId);

        CallFailedRuntimeException exception = FeignRoleHelper.deleteRoleExpectingError(roleId);
        assertEquals(403, exception.getStatus());
    }

}
