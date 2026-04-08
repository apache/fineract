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
package org.apache.fineract.infrastructure.core.diagnostics.security;

import static java.lang.System.lineSeparator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@Component
@Profile(FineractProfiles.DIAGNOSTICS)
@Slf4j
@RequiredArgsConstructor
public class SecurityFilterChainDiagnostics implements InitializingBean {

    private final List<SecurityFilterChain> filterChains;

    @Override
    public void afterPropertiesSet() throws Exception {
        filterChains.forEach(this::printFilterChain);
    }

    private void printFilterChain(SecurityFilterChain filterChain) {
        if (filterChain instanceof DefaultSecurityFilterChain) {
            printDefaultFilterChain((DefaultSecurityFilterChain) filterChain);
        } else {
            printUnknownFilterChain(filterChain);
        }
    }

    private void printDefaultFilterChain(DefaultSecurityFilterChain filterChain) {

        log.info("""

                Filter chain matcher: {}
                Filters in order:
                {}
                """, filterChain.getRequestMatcher(), getFormattedFilters(filterChain));
    }

    private String getFormattedFilters(DefaultSecurityFilterChain filterChain) {
        StringBuilder result = new StringBuilder();
        filterChain.getFilters().forEach(f -> {
            result.append("- ");
            result.append(f.getClass().getName());
            result.append(lineSeparator());
        });
        return result.toString();
    }

    private void printUnknownFilterChain(SecurityFilterChain filterChain) {
        log.info("""
                Filter chain:
                {}
                """, filterChain);
    }
}
