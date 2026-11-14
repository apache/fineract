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

import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Permission rules for API endpoints whose authorization is enforced through request matchers rather than in the
 * resource or service code. Every security filter chain that serves {@code /api/**} must apply these rules before its
 * catch-all rule.
 */
public final class ApiAuthorizationRules {

    private static final String ALL_FUNCTIONS = "ALL_FUNCTIONS";
    private static final String ALL_FUNCTIONS_READ = "ALL_FUNCTIONS_READ";
    private static final String ALL_FUNCTIONS_WRITE = "ALL_FUNCTIONS_WRITE";

    /**
     * The rules in the order they are applied; the first rule that matches a request decides.
     */
    static final List<Rule> RULES = List.of(
            // businessdate
            rule(HttpMethod.GET, "/api/*/businessdate/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_BUSINESS_DATE"),
            rule(HttpMethod.POST, "/api/*/businessdate", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_BUSINESS_DATE"),
            // external
            rule(HttpMethod.GET, "/api/*/externalevents/configuration", ALL_FUNCTIONS, ALL_FUNCTIONS_READ,
                    "READ_EXTERNAL_EVENT_CONFIGURATION"),
            rule(HttpMethod.PUT, "/api/*/externalevents/configuration", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE,
                    "UPDATE_EXTERNAL_EVENT_CONFIGURATION"),
            // cache
            rule(HttpMethod.GET, "/api/*/caches", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CACHE"),
            rule(HttpMethod.PUT, "/api/*/caches", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CACHE"),
            // currency
            rule(HttpMethod.GET, "/api/*/currencies", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CURRENCY"),
            rule(HttpMethod.PUT, "/api/*/currencies", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CURRENCY"),
            // notes: read
            rule(HttpMethod.GET, "/api/*/clients/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENTNOTE"),
            rule(HttpMethod.GET, "/api/*/loans/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOANNOTE"),
            rule(HttpMethod.GET, "/api/*/loanTransactions/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOANTRANSACTIONNOTE"),
            rule(HttpMethod.GET, "/api/*/savings/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SAVINGNOTE"),
            rule(HttpMethod.GET, "/api/*/groups/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_GROUPNOTE"),
            // notes: create
            rule(HttpMethod.POST, "/api/*/clients/*/notes", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CLIENTNOTE"),
            rule(HttpMethod.POST, "/api/*/loans/*/notes", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_LOANNOTE"),
            rule(HttpMethod.POST, "/api/*/loanTransactions/*/notes", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_LOANTRANSACTIONNOTE"),
            rule(HttpMethod.POST, "/api/*/savings/*/notes", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_SAVINGNOTE"),
            rule(HttpMethod.POST, "/api/*/groups/*/notes", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_GROUPNOTE"),
            // notes: update
            rule(HttpMethod.PUT, "/api/*/clients/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CLIENTNOTE"),
            rule(HttpMethod.PUT, "/api/*/loans/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_LOANNOTE"),
            rule(HttpMethod.PUT, "/api/*/loanTransactions/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_LOANTRANSACTIONNOTE"),
            rule(HttpMethod.PUT, "/api/*/savings/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_SAVINGNOTE"),
            rule(HttpMethod.PUT, "/api/*/groups/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_GROUPNOTE"),
            // notes: delete
            rule(HttpMethod.DELETE, "/api/*/clients/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CLIENTNOTE"),
            rule(HttpMethod.DELETE, "/api/*/loans/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_LOANNOTE"),
            rule(HttpMethod.DELETE, "/api/*/loanTransactions/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_LOANTRANSACTIONNOTE"),
            rule(HttpMethod.DELETE, "/api/*/savings/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_SAVINGNOTE"),
            rule(HttpMethod.DELETE, "/api/*/groups/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_GROUPNOTE"),
            // notes: share accounts and savings transactions
            rule(HttpMethod.GET, "/api/*/accounts/share/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SHAREACCOUNTNOTE"),
            rule(HttpMethod.POST, "/api/*/accounts/share/*/notes", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_SHAREACCOUNTNOTE"),
            rule(HttpMethod.PUT, "/api/*/accounts/share/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_SHAREACCOUNTNOTE"),
            rule(HttpMethod.DELETE, "/api/*/accounts/share/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_SHAREACCOUNTNOTE"),
            rule(HttpMethod.GET, "/api/*/savingsTransactions/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SAVINGSTRANSACTIONNOTE"),
            rule(HttpMethod.POST, "/api/*/savingsTransactions/*/notes", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE,
                    "CREATE_SAVINGSTRANSACTIONNOTE"),
            rule(HttpMethod.PUT, "/api/*/savingsTransactions/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE,
                    "UPDATE_SAVINGSTRANSACTIONNOTE"),
            rule(HttpMethod.DELETE, "/api/*/savingsTransactions/*/notes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE,
                    "DELETE_SAVINGSTRANSACTIONNOTE"),
            // document (any entity type; the permissions are the same for all of them)
            rule(HttpMethod.GET, "/api/*/*/*/documents/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT"),
            rule(HttpMethod.POST, "/api/*/*/*/documents", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT"),
            rule(HttpMethod.PUT, "/api/*/*/*/documents/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT"),
            rule(HttpMethod.DELETE, "/api/*/*/*/documents/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT"),
            // image: clients
            rule(HttpMethod.GET, "/api/*/clients/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT"),
            rule(HttpMethod.POST, "/api/*/clients/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CLIENTIMAGE"),
            rule(HttpMethod.PUT, "/api/*/clients/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CLIENTIMAGE"),
            rule(HttpMethod.DELETE, "/api/*/clients/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CLIENTIMAGE"),
            // collateral: clients
            // NOTE: the "_CLIENT_COLLATERAL_PRODUCT" suffix is the legacy permission naming kept for
            // backwards compatibility (entity_name = CLIENT_COLLATERAL_PRODUCT in the permission table)
            rule(HttpMethod.GET, "/api/*/clients/*/collaterals", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENT_COLLATERAL_PRODUCT"),
            rule(HttpMethod.GET, "/api/*/clients/*/collaterals/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENT_COLLATERAL_PRODUCT"),
            rule(HttpMethod.POST, "/api/*/clients/*/collaterals", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CLIENT_COLLATERAL_PRODUCT"),
            rule(HttpMethod.PUT, "/api/*/clients/*/collaterals/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CLIENT_COLLATERAL_PRODUCT"),
            rule(HttpMethod.DELETE, "/api/*/clients/*/collaterals/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE,
                    "DELETE_CLIENT_COLLATERAL_PRODUCT"),
            // image: staff
            rule(HttpMethod.GET, "/api/*/staff/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT"),
            rule(HttpMethod.POST, "/api/*/staff/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_STAFFIMAGE"),
            rule(HttpMethod.PUT, "/api/*/staff/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_STAFFIMAGE"),
            rule(HttpMethod.DELETE, "/api/*/staff/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_STAFFIMAGE"),
            // image: other entity types (only clients and staff have images)
            rule(HttpMethod.GET, "/api/*/*/*/images", ALL_FUNCTIONS, ALL_FUNCTIONS_READ),
            rule(HttpMethod.POST, "/api/*/*/*/images", ALL_FUNCTIONS), rule(HttpMethod.PUT, "/api/*/*/*/images", ALL_FUNCTIONS),
            rule(HttpMethod.DELETE, "/api/*/*/*/images", ALL_FUNCTIONS),
            // collateral: products
            rule(HttpMethod.GET, "/api/*/collateral-management", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_COLLATERAL_PRODUCT"),
            rule(HttpMethod.GET, "/api/*/collateral-management/template", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_COLLATERAL_PRODUCT"),
            rule(HttpMethod.GET, "/api/*/collateral-management/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_COLLATERAL_PRODUCT"),
            rule(HttpMethod.POST, "/api/*/collateral-management", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_COLLATERAL_PRODUCT"),
            rule(HttpMethod.PUT, "/api/*/collateral-management/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_COLLATERAL_PRODUCT"),
            rule(HttpMethod.DELETE, "/api/*/collateral-management/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_COLLATERAL_PRODUCT"),
            // bulk import
            rule(HttpMethod.GET, "/api/*/import", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_IMPORT"),
            rule(HttpMethod.GET, "/api/*/import/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_IMPORT"),
            // payment type
            rule(HttpMethod.GET, "/api/*/paymenttypes/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_PAYMENTTYPE"),
            rule(HttpMethod.POST, "/api/*/paymenttypes", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_PAYMENTTYPE"),
            rule(HttpMethod.PUT, "/api/*/paymenttypes/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_PAYMENTTYPE"),
            rule(HttpMethod.DELETE, "/api/*/paymenttypes/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_PAYMENTTYPE"),
            // product mix
            rule(HttpMethod.GET, "/api/*/loanproducts/*/productmix", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_PRODUCTMIX"),
            rule(HttpMethod.POST, "/api/*/loanproducts/*/productmix", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_PRODUCTMIX"),
            rule(HttpMethod.PUT, "/api/*/loanproducts/*/productmix", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_PRODUCTMIX"),
            rule(HttpMethod.DELETE, "/api/*/loanproducts/*/productmix", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_PRODUCTMIX"),

            // working days
            rule(HttpMethod.GET, "/api/*/workingdays", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_WORKING_DAYS"),
            rule(HttpMethod.GET, "/api/*/workingdays/template", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_WORKING_DAYS"),
            rule(HttpMethod.PUT, "/api/*/workingdays", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_WORKING_DAYS"),
            // interest rate chart slabs (before charts for specificity)
            rule(HttpMethod.GET, "/api/*/interestratecharts/*/chartslabs", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CHARTSLAB"),
            rule(HttpMethod.GET, "/api/*/interestratecharts/*/chartslabs/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CHARTSLAB"),
            rule(HttpMethod.POST, "/api/*/interestratecharts/*/chartslabs", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CHARTSLAB"),
            rule(HttpMethod.PUT, "/api/*/interestratecharts/*/chartslabs/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CHARTSLAB"),
            rule(HttpMethod.DELETE, "/api/*/interestratecharts/*/chartslabs/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CHARTSLAB"),
            // interest rate chart
            rule(HttpMethod.GET, "/api/*/interestratecharts/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_INTERESTRATECHART"),
            rule(HttpMethod.POST, "/api/*/interestratecharts", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_INTERESTRATECHART"),
            rule(HttpMethod.PUT, "/api/*/interestratecharts/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_INTERESTRATECHART"),
            rule(HttpMethod.DELETE, "/api/*/interestratecharts/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_INTERESTRATECHART"),
            // staff
            rule(HttpMethod.GET, "/api/*/staff/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_STAFF"),
            rule(HttpMethod.POST, "/api/*/staff/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_STAFF"),
            rule(HttpMethod.PUT, "/api/*/staff/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_STAFF"),
            // meeting
            rule(HttpMethod.GET, "/api/*/*/*/meetings/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_MEETING"),
            rule(HttpMethod.POST, "/api/*/*/*/meetings", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_MEETING"),
            rule(HttpMethod.POST, "/api/*/*/*/meetings/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "SAVEORUPDATEATTENDANCE_MEETING"),
            rule(HttpMethod.PUT, "/api/*/*/*/meetings/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_MEETING"),
            rule(HttpMethod.DELETE, "/api/*/*/*/meetings/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_MEETING"),
            // hook
            rule(HttpMethod.GET, "/api/*/hooks/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_HOOK"),
            rule(HttpMethod.POST, "/api/*/hooks/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_HOOK"),
            rule(HttpMethod.PUT, "/api/*/hooks/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_HOOK"),
            rule(HttpMethod.DELETE, "/api/*/hooks/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_HOOK"),
            // template
            rule(HttpMethod.GET, "/api/*/templates/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_TEMPLATE"),
            rule(HttpMethod.POST, "/api/*/templates/**", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_TEMPLATE"),
            rule(HttpMethod.PUT, "/api/*/templates/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_TEMPLATE"),
            rule(HttpMethod.DELETE, "/api/*/templates/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_TEMPLATE"),

            rule(HttpMethod.DELETE, "/api/*/loan-collateral-management/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE,
                    "DELETE_LOAN_COLLATERAL_PRODUCT"),

            rule(HttpMethod.GET, "/api/*/standinginstructionrunhistory", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_STANDINGINSTRUCTION"),
            // floatingrate
            rule(HttpMethod.GET, "/api/*/floatingrates", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_FLOATINGRATE"),
            rule(HttpMethod.GET, "/api/*/floatingrates/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_FLOATINGRATE"),
            rule(HttpMethod.POST, "/api/*/floatingrates", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_FLOATINGRATE"),
            rule(HttpMethod.PUT, "/api/*/floatingrates/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_FLOATINGRATE"),
            // share accounts and share products
            rule(HttpMethod.GET, "/api/*/accounts/*/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SHAREACCOUNT"),
            rule(HttpMethod.GET, "/api/*/products/*/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SHAREPRODUCT"),
            // ad hoc query
            rule(HttpMethod.GET, "/api/*/adhocquery/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_ADHOC"),
            // bulk import: imports and their output documents
            rule(HttpMethod.GET, "/api/*/imports/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_IMPORT"),
            // client search
            rule(HttpMethod.POST, "/api/*/clients/search", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENT"),
            // codes
            rule(HttpMethod.GET, "/api/*/codes/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CODE"),
            rule(HttpMethod.GET, "/api/*/codes/name/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CODE"),
            // business step configuration
            rule(HttpMethod.GET, "/api/*/jobs/names", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SCHEDULER"),
            rule(HttpMethod.GET, "/api/*/jobs/*/steps", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SCHEDULER"),
            rule(HttpMethod.GET, "/api/*/jobs/*/available-steps", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SCHEDULER"),
            // COB catch-up and account locks
            rule(HttpMethod.POST, "/api/*/loans/catch-up", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "EXECUTEJOB_SCHEDULER"),
            rule(HttpMethod.POST, "/api/*/working-capital-loans/catch-up", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "EXECUTEJOB_SCHEDULER"),
            rule(HttpMethod.GET, "/api/*/loans/locked", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOAN"),
            // credit bureau
            rule(HttpMethod.POST, "/api/*/creditBureauIntegration/addCreditReport", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE,
                    "SAVE_CREDITREPORT"),
            rule(HttpMethod.GET, "/api/*/creditBureauIntegration/creditReport/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "GET_CREDITREPORT"),
            // datatables
            rule(HttpMethod.POST, "/api/*/datatables/deregister/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DEREGISTER_DATATABLE"),
            // email and sms messages
            rule(HttpMethod.GET, "/api/*/email/{id:[0-9]+}", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_EMAIL"),
            rule(HttpMethod.GET, "/api/*/sms/{id:[0-9]+}", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SMS"),
            // entity datatable checks
            rule(HttpMethod.GET, "/api/*/entityDatatableChecks/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_ENTITY_DATATABLE_CHECK"),
            // external asset owners
            rule(HttpMethod.GET, "/api/*/external-asset-owners/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_EXTERNAL_ASSET_OWNER"),
            rule(HttpMethod.POST, "/api/*/external-asset-owners/search", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_EXTERNAL_ASSET_OWNER"),
            // interoperation
            rule(HttpMethod.GET, "/api/*/interoperation/parties/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_INTERID"),
            rule(HttpMethod.GET, "/api/*/interoperation/accounts/*/identifiers", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_INTERID"),
            rule(HttpMethod.GET, "/api/*/interoperation/accounts/*/kyc", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENT"),
            rule(HttpMethod.GET, "/api/*/interoperation/accounts/*/transactions", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SAVINGSACCOUNT"),
            rule(HttpMethod.GET, "/api/*/interoperation/accounts/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SAVINGSACCOUNT"),
            // journal entries
            rule(HttpMethod.GET, "/api/*/journalentries/provisioning", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_JOURNALENTRY"),
            // loan collateral and post-dated checks
            rule(HttpMethod.GET, "/api/*/loan-collateral-management/*", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOAN"),
            rule(HttpMethod.GET, "/api/*/loans/*/postdatedchecks/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOAN"),
            // provisioning
            rule(HttpMethod.GET, "/api/*/provisioningcategory/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_PROVISIONCATEGORY"),
            rule(HttpMethod.GET, "/api/*/provisioningcriteria/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_PROVISIONCRITERIA"),
            rule(HttpMethod.GET, "/api/*/provisioningentries/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_PROVISIONENTRIES"),
            // ad hoc search
            rule(HttpMethod.POST, "/api/*/search/advance", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOAN"),
            rule(HttpMethod.GET, "/api/*/search/template", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOANPRODUCT"),
            // surveys: scorecards and lookup tables (before surveys for specificity)
            rule(HttpMethod.GET, "/api/*/surveys/scorecards/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SCORECARD"),
            rule(HttpMethod.POST, "/api/*/surveys/scorecards/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_SCORECARD"),
            rule(HttpMethod.GET, "/api/*/surveys/*/lookuptables/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOOKUPTABLE"),
            rule(HttpMethod.POST, "/api/*/surveys/*/lookuptables", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_LOOKUPTABLE"),
            // surveys
            rule(HttpMethod.GET, "/api/*/surveys/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SURVEY"),
            rule(HttpMethod.POST, "/api/*/surveys", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_SURVEY"),
            rule(HttpMethod.PUT, "/api/*/surveys/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_SURVEY"),
            rule(HttpMethod.POST, "/api/*/surveys/*", ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_SURVEY"),
            // teller
            rule(HttpMethod.GET, "/api/*/tellers/**", ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_TELLER"),
            // working capital loan
            rule(HttpMethod.GET, "/api/*/working-capital-loans/*/amortization-schedule", ALL_FUNCTIONS, ALL_FUNCTIONS_READ,
                    "READ_WORKINGCAPITALLOAN"));

    private ApiAuthorizationRules() {}

    public static void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        for (Rule rule : RULES) {
            auth.requestMatchers(rule.matcher()).hasAnyAuthority(rule.authorities().toArray(String[]::new));
        }
    }

    private static Rule rule(HttpMethod method, String pattern, String... authorities) {
        return new Rule(ApiRequestMatcher.of(method, pattern), List.of(authorities));
    }

    record Rule(ApiRequestMatcher matcher, List<String> authorities) {
    }
}
