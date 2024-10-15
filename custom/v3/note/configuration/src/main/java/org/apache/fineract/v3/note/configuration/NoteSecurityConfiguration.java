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
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class NoteSecurityConfiguration {

    @Bean
    @Order(2)
    public SecurityFilterChain notesSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher(antMatcher("/v3/**/notes/**")).authorizeHttpRequests(auth -> auth

                // Read Requests (GET)
                .requestMatchers(HttpMethod.GET, "/v3/clients/*/notes").hasAuthority("READ_CLIENTNOTE")
                .requestMatchers(HttpMethod.GET, "/v3/groups/*/notes").hasAuthority("READ_GROUPNOTE")
                .requestMatchers(HttpMethod.GET, "/v3/loans/*/notes").hasAuthority("READ_LOANNOTE")
                .requestMatchers(HttpMethod.GET, "/v3/loanTransactions/*/notes").hasAuthority("READ_LOANTRANSACTIONNOTE")
                .requestMatchers(HttpMethod.GET, "/v3/savings/*/notes").hasAuthority("READ_SAVINGNOTE")

                // Write Requests (POST)
                .requestMatchers(HttpMethod.POST, "/v3/clients/*/notes").hasAuthority("CREATE_CLIENTNOTE")
                .requestMatchers(HttpMethod.POST, "/v3/groups/*/notes").hasAuthority("CREATE_GROUPNOTE")
                .requestMatchers(HttpMethod.POST, "/v3/loans/*/notes").hasAuthority("CREATE_LOANNOTE")
                .requestMatchers(HttpMethod.POST, "/v3/loanTransactions/*/notes").hasAuthority("CREATE_LOANTRANSACTIONNOTE")
                .requestMatchers(HttpMethod.POST, "/v3/savings/*/notes").hasAuthority("CREATE_SAVINGNOTE")

                // Update Requests (PUT)
                .requestMatchers(HttpMethod.PUT, "/v3/clients/*/notes/*").hasAuthority("UPDATE_CLIENTNOTE")
                .requestMatchers(HttpMethod.PUT, "/v3/groups/*/notes/*").hasAuthority("UPDATE_GROUPNOTE")
                .requestMatchers(HttpMethod.PUT, "/v3/loans/*/notes/*").hasAuthority("UPDATE_LOANNOTE")
                .requestMatchers(HttpMethod.PUT, "/v3/loanTransactions/*/notes/*").hasAuthority("UPDATE_LOANTRANSACTIONNOTE")
                .requestMatchers(HttpMethod.PUT, "/v3/savings/*/notes/*").hasAuthority("UPDATE_SAVINGNOTE")

                // Delete Requests (DELETE)
                .requestMatchers(HttpMethod.DELETE, "/v3/clients/*/notes/*").hasAuthority("DELETE_CLIENTNOTE")
                .requestMatchers(HttpMethod.DELETE, "/v3/groups/*/notes/*").hasAuthority("DELETE_GROUPNOTE")
                .requestMatchers(HttpMethod.DELETE, "/v3/loans/*/notes/*").hasAuthority("DELETE_LOANNOTE")
                .requestMatchers(HttpMethod.DELETE, "/v3/loanTransactions/*/notes/*").hasAuthority("DELETE_LOANTRANSACTIONNOTE")
                .requestMatchers(HttpMethod.DELETE, "/v3/savings/*/notes/*").hasAuthority("DELETE_SAVINGNOTE")

                // Catch-all rule
                .anyRequest().hasAuthority("ALL_FUNCTIONS")).build();
    }
}
