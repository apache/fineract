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

    public static enum SEARCH_RESPONSE_PARAMETERS {
        ENTITY_ID("entityId"), ENTITY_ACCOUNT_NO("entityAccountNo"), ENTITY_EXTERNAL_ID("entityExternalId"), ENTITY_NAME("entityName"), ENTITY_TYPE(
                "entityType"), PARENT_ID("parentId"), PARENT_NAME("parentName"),ENTITY_MOBILE_NO("entityMobileNo"), ENTITY_STATUS("entityStatus");

        private final String value;

        private SEARCH_RESPONSE_PARAMETERS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();
        static {
            for (final SEARCH_RESPONSE_PARAMETERS param : SEARCH_RESPONSE_PARAMETERS.values()) {
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

    public static enum SEARCH_SUPPORTED_PARAMETERS {
        QUERY("query"), RESOURCE("resource"),EXACTMATCH("exactMatch");

        private final String value;

        private SEARCH_SUPPORTED_PARAMETERS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();
        static {
            for (final SEARCH_SUPPORTED_PARAMETERS param : SEARCH_SUPPORTED_PARAMETERS.values()) {
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

    public static enum SEARCH_SUPPORTED_RESOURCES {
        CLIENTS("clients"), GROUPS("groups"), LOANS("loans"), SAVINGS("savings"), SHARES("shares"), CLIENTIDENTIFIERS("clientIdentifiers");

        private final String value;

        private SEARCH_SUPPORTED_RESOURCES(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();
        static {
            for (final SEARCH_SUPPORTED_RESOURCES param : SEARCH_SUPPORTED_RESOURCES.values()) {
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

    public static enum SEARCH_LOAN_DATE {
        APPROVAL_DATE("approvalDate"), CREATED_DATE("createdDate"), DISBURSAL_DATE("disbursalDate");

        private final String value;

        private SEARCH_LOAN_DATE(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();
        static {
            for (final SEARCH_LOAN_DATE param : SEARCH_LOAN_DATE.values()) {
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
