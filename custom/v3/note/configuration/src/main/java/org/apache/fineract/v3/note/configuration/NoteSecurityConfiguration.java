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
package org.apache.fineract.v3.note.configuration;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class NoteSecurityConfiguration {

    @Bean
    @Order(2)
    public SecurityFilterChain notesSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher(antMatcher("/v3/*/*/notes")).authorizeHttpRequests((auth) -> { //
            auth.requestMatchers("/v3/clients/*/notes").hasAuthority("READ_CLIENTNOTE") //
                    .requestMatchers("/v3/groups/*/notes").hasAuthority("READ_GROUPNOTE") //
                    .requestMatchers("/v3/loans/*/notes").hasAuthority("READ_LOANNOTE") //
                    .requestMatchers("/v3/loanTransactions/*/notes").hasAuthority("READ_LOANTRANSACTIONNOTE") //
                    .requestMatchers("/v3/savings/*/notes").hasAuthority("READ_SAVINGNOTE") //
                    //
                    //
                    .anyRequest().hasAnyAuthority("ALL_FUNCTIONS", "ALL_FUNCTIONS_READ");
        }).build();
    }
}
