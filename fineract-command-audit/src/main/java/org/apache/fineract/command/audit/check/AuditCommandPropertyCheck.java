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
package org.apache.fineract.command.audit.check;

import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_AUDIT_PROPERTY_HOOK_ERROR_ENABLED;
import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_AUDIT_PROPERTY_HOOK_POST_ENABLED;
import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_AUDIT_PROPERTY_HOOK_PRE_ENABLED;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
final class AuditCommandPropertyCheck {

    private final Environment environment;

    @PostConstruct
    public void check() {
        if (environment.containsProperty(COMMAND_AUDIT_PROPERTY_HOOK_PRE_ENABLED)) {
            log.info("Audit pre command hook enabled.");
        } else {
            log.warn("Audit pre command hook disabled! IT IS RECOMMENDED ENABLE THIS FEATURE IN PRODUCTION ENVIRONMENTS!");
        }
        if (environment.containsProperty(COMMAND_AUDIT_PROPERTY_HOOK_POST_ENABLED)) {
            log.info("Audit post command hook enabled.");
        } else {
            log.warn("Audit post command hook disabled! IT IS RECOMMENDED ENABLE THIS FEATURE IN PRODUCTION ENVIRONMENTS!");
        }
        if (environment.containsProperty(COMMAND_AUDIT_PROPERTY_HOOK_ERROR_ENABLED)) {
            log.info("Audit error command hook enabled.");
        } else {
            log.warn("Audit error command hook disabled! IT IS RECOMMENDED ENABLE THIS FEATURE IN PRODUCTION ENVIRONMENTS!");
        }
    }
}
