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

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Permission rules for API endpoints whose authorization is enforced through request matchers rather than in the
 * resource or service code. Every security filter chain that serves {@code /api/**} must apply these rules before its
 * catch-all rule.
 */
public final class ApiAuthorizationRules {

    private static final PathPatternRequestMatcher.Builder API_MATCHER = PathPatternRequestMatcher.withDefaults();
    private static final String ALL_FUNCTIONS = "ALL_FUNCTIONS";
    private static final String ALL_FUNCTIONS_READ = "ALL_FUNCTIONS_READ";
    private static final String ALL_FUNCTIONS_WRITE = "ALL_FUNCTIONS_WRITE";

    private ApiAuthorizationRules() {}

    public static void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                // businessdate
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/businessdate/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_BUSINESS_DATE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/businessdate"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_BUSINESS_DATE")
                // external
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/externalevents/configuration"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_EXTERNAL_EVENT_CONFIGURATION")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/externalevents/configuration"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_EXTERNAL_EVENT_CONFIGURATION")
                // cache
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/caches"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CACHE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/caches"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CACHE")
                // currency
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/currencies"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CURRENCY")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/currencies"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CURRENCY")
                // notes: read
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/clients/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENTNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/loans/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOANNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/loanTransactions/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOANTRANSACTIONNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/savings/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SAVINGNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/groups/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_GROUPNOTE")
                // notes: create
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/clients/*/notes"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CLIENTNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/loans/*/notes"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_LOANNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/loanTransactions/*/notes"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_LOANTRANSACTIONNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/savings/*/notes"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_SAVINGNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/groups/*/notes"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_GROUPNOTE")
                // notes: update
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/clients/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CLIENTNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/loans/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_LOANNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/loanTransactions/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_LOANTRANSACTIONNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/savings/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_SAVINGNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/groups/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_GROUPNOTE")
                // notes: delete
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/clients/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CLIENTNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/loans/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_LOANNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/loanTransactions/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_LOANTRANSACTIONNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/savings/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_SAVINGNOTE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/groups/*/notes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_GROUPNOTE")
                // document: clients
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/clients/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/clients/*/documents"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/clients/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/clients/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                // document: client_identifiers
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/client_identifiers/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/client_identifiers/*/documents"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/client_identifiers/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/client_identifiers/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                // document: staff
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/staff/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/staff/*/documents"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/staff/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/staff/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                // document: loans
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/loans/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/loans/*/documents"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/loans/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/loans/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                // document: savings
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/savings/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/savings/*/documents"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/savings/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/savings/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                // document: groups
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/groups/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/groups/*/documents"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/groups/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/groups/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                // document: import
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/import/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/import/*/documents"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/import/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/import/*/documents/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                // image: clients
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/clients/*/images"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/clients/*/images"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CLIENTIMAGE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/clients/*/images"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CLIENTIMAGE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/clients/*/images"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CLIENTIMAGE")
                // collateral: clients
                // NOTE: the "_CLIENT_COLLATERAL_PRODUCT" suffix is the legacy permission naming kept for
                // backwards compatibility (entity_name = CLIENT_COLLATERAL_PRODUCT in the permission table)
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/clients/*/collaterals"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENT_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/clients/*/collaterals/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENT_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/clients/*/collaterals"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CLIENT_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/clients/*/collaterals/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CLIENT_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/clients/*/collaterals/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CLIENT_COLLATERAL_PRODUCT")
                // image: staff
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/staff/*/images"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/staff/*/images"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_STAFFIMAGE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/staff/*/images"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_STAFFIMAGE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/staff/*/images"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_STAFFIMAGE")
                // collateral: products
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/collateral-management"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/collateral-management/template"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/collateral-management/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/collateral-management"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/collateral-management/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_COLLATERAL_PRODUCT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/collateral-management/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_COLLATERAL_PRODUCT")
                // bulk import
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/import"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_IMPORT")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/import/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_IMPORT")
                // payment type
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/paymenttypes/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_PAYMENTTYPE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/paymenttypes"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_PAYMENTTYPE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/paymenttypes/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_PAYMENTTYPE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/paymenttypes/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_PAYMENTTYPE")
                // product mix
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/loanproducts/*/productmix"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_PRODUCTMIX")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/loanproducts/*/productmix"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_PRODUCTMIX")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/loanproducts/*/productmix"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_PRODUCTMIX")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/loanproducts/*/productmix"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_PRODUCTMIX")

                // working days
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/workingdays"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_WORKING_DAYS")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/workingdays/template"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_WORKING_DAYS")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/workingdays"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_WORKING_DAYS")
                // interest rate chart slabs (before charts for specificity)
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/interestratecharts/*/chartslabs"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CHARTSLAB")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/interestratecharts/*/chartslabs/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CHARTSLAB")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/interestratecharts/*/chartslabs"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CHARTSLAB")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/interestratecharts/*/chartslabs/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CHARTSLAB")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/interestratecharts/*/chartslabs/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CHARTSLAB")
                // interest rate chart
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/interestratecharts/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_INTERESTRATECHART")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/interestratecharts"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_INTERESTRATECHART")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/interestratecharts/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_INTERESTRATECHART")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/interestratecharts/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_INTERESTRATECHART")
                // staff
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/staff/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_STAFF")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/staff/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_STAFF")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/staff/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_STAFF")
                // meeting
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/*/*/meetings/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_MEETING")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/*/*/meetings"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_MEETING")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/*/*/meetings/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "SAVEORUPDATEATTENDANCE_MEETING")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/*/*/meetings/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_MEETING")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/*/*/meetings/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_MEETING")
                // hook
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/hooks/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_HOOK")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/hooks/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_HOOK")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/hooks/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_HOOK")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/hooks/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_HOOK")
                // template
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/templates/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_TEMPLATE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/templates/**"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_TEMPLATE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/templates/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_TEMPLATE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/templates/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_TEMPLATE")

                .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/loan-collateral-management/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_LOAN_COLLATERAL_PRODUCT")

                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/standinginstructionrunhistory"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_STANDINGINSTRUCTION")
                // floatingrate
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/floatingrates"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_FLOATINGRATE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/floatingrates/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_FLOATINGRATE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/floatingrates"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_FLOATINGRATE")
                .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/floatingrates/*"))
                .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_FLOATINGRATE");
    }
}
