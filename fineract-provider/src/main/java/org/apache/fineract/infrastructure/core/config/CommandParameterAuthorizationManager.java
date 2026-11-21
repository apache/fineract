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

import java.util.Map;
import java.util.function.Supplier;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Authorizes an endpoint where a single HTTP method serves several commands selected by the {@code command} query
 * parameter, against the authority that belongs to the requested command, so holding one command's permission does not
 * also grant the others.
 * <p>
 * An unrecognised (or missing) command falls through to {@code unknownCommand}, which should be the union of the
 * per-command authorities: the request is then rejected by the resource itself with the usual "unrecognized query
 * parameter" error rather than as an authorization failure.
 */
final class CommandParameterAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    static final String COMMAND_QUERY_PARAM = "command";

    private final Map<String, AuthorizationManager<RequestAuthorizationContext>> byCommand;
    private final AuthorizationManager<RequestAuthorizationContext> unknownCommand;

    CommandParameterAuthorizationManager(final Map<String, AuthorizationManager<RequestAuthorizationContext>> byCommand,
            final AuthorizationManager<RequestAuthorizationContext> unknownCommand) {
        this.byCommand = Map.copyOf(byCommand);
        this.unknownCommand = unknownCommand;
    }

    @Override
    public AuthorizationResult authorize(final Supplier<? extends Authentication> authentication,
            final RequestAuthorizationContext context) {
        final String commandParam = context.getRequest().getParameter(COMMAND_QUERY_PARAM);
        final AuthorizationManager<RequestAuthorizationContext> delegate = byCommand.entrySet().stream()
                .filter(entry -> CommandParameterUtil.is(commandParam, entry.getKey())).map(Map.Entry::getValue).findFirst()
                .orElse(unknownCommand);
        return delegate.authorize(authentication, context);
    }
}
