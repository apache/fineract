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
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAnyAuthority;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * The standing instruction PUT endpoint serves both {@code command=update} and {@code command=delete}. A single
 * {@code hasAnyAuthority(UPDATE, DELETE)} matcher would let either permission perform both operations, so the endpoint
 * is authorized per command instead. These tests pin that behaviour down.
 */
public class StandingInstructionCommandAuthorizationTest {

    private static final String UPDATE_PERMISSION = "UPDATE_STANDINGINSTRUCTION";
    private static final String DELETE_PERMISSION = "DELETE_STANDINGINSTRUCTION";

    /**
     * Mirrors the manager wired up for PUT /standinginstructions/* in {@link SecurityConfig}.
     */
    private AuthorizationManager<RequestAuthorizationContext> managerUnderTest() {
        final Map<String, AuthorizationManager<RequestAuthorizationContext>> byCommand = Map.of(CommandParameterUtil.UPDATE_COMMAND_VALUE,
                hasAnyAuthority(UPDATE_PERMISSION), CommandParameterUtil.DELETE_COMMAND_VALUE, hasAnyAuthority(DELETE_PERMISSION));
        final AuthorizationManager<RequestAuthorizationContext> unknown = hasAnyAuthority(UPDATE_PERMISSION, DELETE_PERMISSION);

        return (authentication, context) -> {
            final String commandParam = context.getRequest().getParameter("command");
            final AuthorizationManager<RequestAuthorizationContext> delegate = byCommand.entrySet().stream()
                    .filter(entry -> CommandParameterUtil.is(commandParam, entry.getKey())).map(Map.Entry::getValue).findFirst()
                    .orElse(unknown);
            final var result = delegate.authorize(authentication, context);
            return result == null ? null : new AuthorizationDecision(result.isGranted());
        };
    }

    private boolean isGranted(final String command, final String... authorities) {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("command")).thenReturn(command);

        final Supplier<Authentication> authentication = () -> new UsernamePasswordAuthenticationToken("user", "password",
                AuthorityUtils.createAuthorityList(authorities));

        final AuthorizationDecision decision = managerUnderTest().check(authentication, new RequestAuthorizationContext(request));
        return decision != null && decision.isGranted();
    }

    @Test
    public void updatePermissionAllowsUpdateCommand() {
        assertThat(isGranted("update", UPDATE_PERMISSION)).isTrue();
    }

    @Test
    public void deletePermissionAllowsDeleteCommand() {
        assertThat(isGranted("delete", DELETE_PERMISSION)).isTrue();
    }

    @Test
    public void deletePermissionAloneCannotUpdate() {
        assertThat(isGranted("update", DELETE_PERMISSION)).isFalse();
    }

    @Test
    public void updatePermissionAloneCannotDelete() {
        assertThat(isGranted("delete", UPDATE_PERMISSION)).isFalse();
    }

    @Test
    public void commandMatchingIsCaseInsensitive() {
        assertThat(isGranted("UPDATE", UPDATE_PERMISSION)).isTrue();
        assertThat(isGranted("UPDATE", DELETE_PERMISSION)).isFalse();
    }

    /**
     * An unrecognised command still needs one of the two permissions, and is then rejected by the resource itself with
     * the usual "unrecognized query parameter" error rather than as an authorization failure.
     */
    @Test
    public void unknownCommandFallsBackToEitherPermission() {
        assertThat(isGranted("bogus", UPDATE_PERMISSION)).isTrue();
        assertThat(isGranted("bogus", DELETE_PERMISSION)).isTrue();
        assertThat(isGranted("bogus", "READ_STANDINGINSTRUCTION")).isFalse();
    }

    @Test
    public void unrelatedPermissionIsRejected() {
        assertThat(isGranted("update", "READ_STANDINGINSTRUCTION")).isFalse();
    }
}
