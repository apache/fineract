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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.audit.AuditCommandProperties;
import org.springframework.stereotype.Component;

/**
 * Reports at startup whether command auditing is actually switched on. Reads the bound property values rather than
 * testing for their presence, so that an explicit {@code false} is reported as disabled.
 */
@Slf4j
@RequiredArgsConstructor
@Component
final class AuditCommandPropertyCheck {

    private static final String RECOMMENDATION = "IT IS RECOMMENDED TO ENABLE THIS FEATURE IN PRODUCTION ENVIRONMENTS!";

    private final AuditCommandProperties properties;

    @PostConstruct
    void check() {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            log.warn("Command audit is disabled! {}", RECOMMENDATION);

            // the individual hooks are not created at all while the module is disabled
            return;
        }

        report("pre", properties.getHookPreEnabled());
        report("post", properties.getHookPostEnabled());
        report("error", properties.getHookErrorEnabled());
    }

    private void report(String hook, Boolean enabled) {
        if (Boolean.TRUE.equals(enabled)) {
            log.info("Audit {} command hook enabled.", hook);
        } else {
            log.warn("Audit {} command hook disabled! {}", hook, RECOMMENDATION);
        }
    }
}
