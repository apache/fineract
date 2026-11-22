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
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.fineract.portfolio.group.api.CenterCommand;
import org.apache.fineract.portfolio.group.api.GroupCommand;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * {@code POST /groups/{id}} and {@code POST /centers/{id}} serve several commands selected by {@code ?command=}.
 * {@link ApiAuthorizationRules} builds their authorization from {@link GroupCommand} and {@link CenterCommand}, so
 * these tests go over every value of both enums: each command is granted by its own permission and refused with any
 * other one.
 */
class GroupCenterCommandAuthorizationTest {

    /** Stands in for the rule every other API request gets (fully authenticated). */
    private static final AuthorizationManager<RequestAuthorizationContext> AUTHENTICATED = (authentication,
            context) -> new AuthorizationDecision(authentication.get() != null);

    private static AuthorizationManager<RequestAuthorizationContext> authorization(final Map<String, String> permissionByCommand) {
        // the rules are registered as allOf(base, rule), see ApiAuthorizationRules.configure
        return AuthorizationManagers.allOf(AUTHENTICATED, ApiAuthorizationRules.commandAuthorization(permissionByCommand));
    }

    private static Optional<ApiAuthorizationRules.Rule> firstRule(final String method, final String path) {
        return ApiAuthorizationRules.RULES.stream().filter(rule -> rule.matcher().matches(method, path)).findFirst();
    }

    @ParameterizedTest
    @CsvSource({ "groups, CLOSE_GROUP", "centers, CLOSE_CENTER" })
    void commandRuleCoversTheIdWithAndWithoutTrailingSlash(final String grouping, final String closePermission) {
        assertThat(firstRule("POST", "/api/v1/" + grouping + "/1"))
                .hasValueSatisfying(rule -> assertThat(rule.authorities()).contains(closePermission));
        assertThat(firstRule("POST", "/api/v1/" + grouping + "/1/"))
                .hasValueSatisfying(rule -> assertThat(rule.authorities()).contains(closePermission));
    }

    /** The bulk import upload is authorized per row by its import handler (see ApiAuthorizationCoverageTest). */
    @ParameterizedTest
    @ValueSource(strings = { "groups", "centers" })
    void commandRuleDoesNotCoverTheUploadTemplate(final String grouping) {
        assertThat(firstRule("POST", "/api/v1/" + grouping + "/uploadtemplate")).isEmpty();
        assertThat(firstRule("POST", "/api/v1/" + grouping + "/uploadtemplate/")).isEmpty();
    }

    private static boolean isGranted(final AuthorizationManager<RequestAuthorizationContext> manager, final String command,
            final String... authorities) {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("command")).thenReturn(command);
        final Supplier<Authentication> authentication = () -> new UsernamePasswordAuthenticationToken("user", "password",
                AuthorityUtils.createAuthorityList(authorities));
        final AuthorizationResult result = manager.authorize(authentication, new RequestAuthorizationContext(request));
        return result != null && result.isGranted();
    }

    @ParameterizedTest
    @EnumSource(GroupCommand.class)
    void groupCommandNeedsItsOwnPermission(final GroupCommand command) {
        final AuthorizationManager<RequestAuthorizationContext> manager = authorization(ApiAuthorizationRules.groupCommandPermissions());
        final String[] otherPermissions = Arrays.stream(GroupCommand.values()).filter(other -> other != command)
                .map(GroupCommand::getPermission).filter(Objects::nonNull).toArray(String[]::new);

        assertGrantedOnlyByItsOwnPermission(manager, command.getValue(), command.getPermission(), "READ_GROUP", otherPermissions);
    }

    @ParameterizedTest
    @EnumSource(CenterCommand.class)
    void centerCommandNeedsItsOwnPermission(final CenterCommand command) {
        final AuthorizationManager<RequestAuthorizationContext> manager = authorization(ApiAuthorizationRules.centerCommandPermissions());
        final String[] otherPermissions = Arrays.stream(CenterCommand.values()).filter(other -> other != command)
                .map(CenterCommand::getPermission).filter(Objects::nonNull).toArray(String[]::new);

        assertGrantedOnlyByItsOwnPermission(manager, command.getValue(), command.getPermission(), "READ_CENTER", otherPermissions);
    }

    private static void assertGrantedOnlyByItsOwnPermission(final AuthorizationManager<RequestAuthorizationContext> manager,
            final String command, final String permission, final String readPermission, final String[] otherPermissions) {
        if (permission == null) {
            // a read-only command needs no more than any other API request
            assertThat(isGranted(manager, command, readPermission)).isTrue();
            assertThat(isGranted(manager, command)).isTrue();
            return;
        }
        assertThat(isGranted(manager, command, permission)).isTrue();
        assertThat(isGranted(manager, command.toUpperCase(Locale.ROOT), permission)).isTrue();
        assertThat(isGranted(manager, command, "ALL_FUNCTIONS")).isTrue();
        assertThat(isGranted(manager, command, "ALL_FUNCTIONS_WRITE")).isTrue();
        assertThat(isGranted(manager, command, readPermission)).isFalse();
        assertThat(isGranted(manager, command, "ALL_FUNCTIONS_READ")).isFalse();
        assertThat(isGranted(manager, command, otherPermissions)).isFalse();
    }

    /**
     * An unrecognised command needs one of the write permissions and is then rejected by the resource as an
     * unrecognized query parameter, as for standing instructions.
     */
    @ParameterizedTest
    @EnumSource(GroupCommand.class)
    void unknownGroupCommandFallsBackToAnyGroupPermission(final GroupCommand command) {
        final AuthorizationManager<RequestAuthorizationContext> manager = authorization(ApiAuthorizationRules.groupCommandPermissions());
        if (command.getPermission() != null) {
            assertThat(isGranted(manager, "bogus", command.getPermission())).isTrue();
            assertThat(isGranted(manager, null, command.getPermission())).isTrue();
        }
        assertThat(isGranted(manager, "bogus", "READ_GROUP")).isFalse();
    }

    @ParameterizedTest
    @EnumSource(CenterCommand.class)
    void unknownCenterCommandFallsBackToAnyCenterPermission(final CenterCommand command) {
        final AuthorizationManager<RequestAuthorizationContext> manager = authorization(ApiAuthorizationRules.centerCommandPermissions());
        if (command.getPermission() != null) {
            assertThat(isGranted(manager, "bogus", command.getPermission())).isTrue();
        }
        assertThat(isGranted(manager, "bogus", "READ_CENTER")).isFalse();
        assertThat(Stream.of(GroupCommand.values()).map(GroupCommand::getPermission).filter(Objects::nonNull)
                .noneMatch(groupPermission -> isGranted(manager, "bogus", groupPermission))).isTrue();
    }
}
