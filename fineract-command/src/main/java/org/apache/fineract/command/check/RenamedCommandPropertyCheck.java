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
package org.apache.fineract.command.check;

import static org.apache.fineract.command.core.CommandConstants.COMMAND_PROPERTIES_PREFIX;
import static org.apache.fineract.command.core.CommandConstants.COMMAND_PROPERTY_HOOKS_SERVLET_HEADERS_ENABLED;
import static org.apache.fineract.command.core.CommandConstants.COMMAND_PROPERTY_HOOKS_TIMESTAMP_ENABLED;
import static org.apache.fineract.command.core.CommandConstants.COMMAND_PROPERTY_HOOKS_USERNAME_ENABLED;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Warns at startup about hook switches that are still set under the names they had up to 1.16.x. Those names are no
 * longer read, and every hook is on unless switched off, so a hook switched off under its old name would otherwise come
 * back on without a word.
 *
 * <p>
 * The environment variables that fed the old names are still read as a fallback (see {@code application.properties} in
 * {@code fineract-provider}); this check is about the property names themselves.
 */
@Slf4j
@RequiredArgsConstructor
@Component
final class RenamedCommandPropertyCheck {

    private static final String LEGACY_HOOKS_PREFIX = COMMAND_PROPERTIES_PREFIX + ".hooks.";

    // the audit names are spelled out: the audit module depends on this one, not the other way round
    static final Map<String, String> RENAMED = Map.ofEntries(
            Map.entry(LEGACY_HOOKS_PREFIX + "audit-pre", COMMAND_PROPERTIES_PREFIX + ".audit.hook-pre-enabled"),
            Map.entry(LEGACY_HOOKS_PREFIX + "audit-post", COMMAND_PROPERTIES_PREFIX + ".audit.hook-post-enabled"),
            Map.entry(LEGACY_HOOKS_PREFIX + "audit-error", COMMAND_PROPERTIES_PREFIX + ".audit.hook-error-enabled"),
            Map.entry(LEGACY_HOOKS_PREFIX + "servlet-header-pre", COMMAND_PROPERTY_HOOKS_SERVLET_HEADERS_ENABLED),
            Map.entry(LEGACY_HOOKS_PREFIX + "timestamp-pre", COMMAND_PROPERTY_HOOKS_TIMESTAMP_ENABLED),
            Map.entry(LEGACY_HOOKS_PREFIX + "username-pre", COMMAND_PROPERTY_HOOKS_USERNAME_ENABLED));

    private final Environment environment;

    @PostConstruct
    void check() {
        RENAMED.forEach((oldName, newName) -> {
            if (environment.containsProperty(oldName)) {
                log.warn("Property '{}' is set but no longer has any effect. Use '{}' instead.", oldName, newName);
            }
        });
    }
}
