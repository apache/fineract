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
package org.apache.fineract.infrastructure.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * The standing instruction PUT endpoint serves both {@code command=update} and {@code command=delete}. A single
 * {@code hasAnyAuthority(UPDATE, DELETE)} matcher would let either permission perform both operations, so
 * {@link SecurityConfig} authorizes it per command. These tests run against the manager {@link SecurityConfig} wires up
 * for that endpoint.
 */
class StandingInstructionCommandAuthorizationTest {

    private static final String UPDATE_PERMISSION = "UPDATE_STANDINGINSTRUCTION";
    private static final String DELETE_PERMISSION = "DELETE_STANDINGINSTRUCTION";

    private boolean isGranted(final String command, final String... authorities) {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("command")).thenReturn(command);

        final Supplier<Authentication> authentication = () -> new UsernamePasswordAuthenticationToken("user", "password",
                AuthorityUtils.createAuthorityList(authorities));

        final AuthorizationResult result = SecurityConfig.standingInstructionUpdateOrDeleteAuthorization().authorize(authentication,
                new RequestAuthorizationContext(request));
        return result != null && result.isGranted();
    }

    @Test
    void updatePermissionAllowsUpdateCommand() {
        assertThat(isGranted("update", UPDATE_PERMISSION)).isTrue();
    }

    @Test
    void deletePermissionAllowsDeleteCommand() {
        assertThat(isGranted("delete", DELETE_PERMISSION)).isTrue();
    }

    @Test
    void deletePermissionAloneCannotUpdate() {
        assertThat(isGranted("update", DELETE_PERMISSION)).isFalse();
    }

    @Test
    void updatePermissionAloneCannotDelete() {
        assertThat(isGranted("delete", UPDATE_PERMISSION)).isFalse();
    }

    @Test
    void commandMatchingIsCaseInsensitive() {
        assertThat(isGranted("UPDATE", UPDATE_PERMISSION)).isTrue();
        assertThat(isGranted("DELETE", UPDATE_PERMISSION)).isFalse();
    }

    @Test
    void allFunctionsAndAllFunctionsWriteAllowBothCommands() {
        assertThat(isGranted("update", "ALL_FUNCTIONS")).isTrue();
        assertThat(isGranted("delete", "ALL_FUNCTIONS")).isTrue();
        assertThat(isGranted("update", "ALL_FUNCTIONS_WRITE")).isTrue();
        assertThat(isGranted("delete", "ALL_FUNCTIONS_WRITE")).isTrue();
        assertThat(isGranted("delete", "ALL_FUNCTIONS_READ")).isFalse();
    }

    /**
     * An unrecognised command still needs one of the two permissions, and is then rejected by the resource itself with
     * the usual "unrecognized query parameter" error rather than as an authorization failure.
     */
    @Test
    void unknownCommandFallsBackToEitherPermission() {
        assertThat(isGranted("bogus", UPDATE_PERMISSION)).isTrue();
        assertThat(isGranted("bogus", DELETE_PERMISSION)).isTrue();
        assertThat(isGranted(null, UPDATE_PERMISSION)).isTrue();
        assertThat(isGranted("bogus", "READ_STANDINGINSTRUCTION")).isFalse();
    }

    @Test
    void unrelatedPermissionIsRejected() {
        assertThat(isGranted("update", "READ_STANDINGINSTRUCTION")).isFalse();
        assertThat(isGranted("delete", "READ_STANDINGINSTRUCTION")).isFalse();
    }
}
