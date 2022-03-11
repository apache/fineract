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
package org.apache.fineract.portfolio.search;

import java.util.HashSet;
import java.util.Set;

public class SearchConstants {

    public enum SearchResponseParameters {

        ENTITY_ID("entityId"), ENTITY_ACCOUNT_NO("entityAccountNo"), ENTITY_EXTERNAL_ID("entityExternalId"), ENTITY_NAME(
                "entityName"), ENTITY_TYPE("entityType"), PARENT_ID(
                        "parentId"), PARENT_NAME("parentName"), ENTITY_MOBILE_NO("entityMobileNo"), ENTITY_STATUS("entityStatus");

        private final String value;

        SearchResponseParameters(String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final SearchResponseParameters param : SearchResponseParameters.values()) {
                values.add(param.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum SearchSupportedParameters {

        QUERY("query"), RESOURCE("resource"), EXACTMATCH("exactMatch");

        private final String value;

        SearchSupportedParameters(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final SearchSupportedParameters param : SearchSupportedParameters.values()) {
                values.add(param.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum SearchSupportedResources {

        CLIENTS("clients"), GROUPS("groups"), LOANS("loans"), SAVINGS("savings"), SHARES("shares"), CLIENTIDENTIFIERS("clientIdentifiers");

        private final String value;

        SearchSupportedResources(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final SearchSupportedResources param : SearchSupportedResources.values()) {
                values.add(param.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum SearchLoanDate {

        APPROVAL_DATE("approvalDate"), CREATED_DATE("createdDate"), DISBURSAL_DATE("disbursalDate");

        private final String value;

        SearchLoanDate(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final SearchLoanDate param : SearchLoanDate.values()) {
                values.add(param.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }
}
