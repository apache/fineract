/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search;

import java.util.HashSet;
import java.util.Set;

public class SearchConstants {

    public static enum SEARCH_RESPONSE_PARAMETERS {
        ENTITY_ID("entityId"), ENTITY_ACCOUNT_NO("entityAccountNo"), ENTITY_EXTERNAL_ID("entityExternalId"), ENTITY_NAME("entityName"), ENTITY_TYPE(
                "entityType"), PARENT_ID("parentId"), PARENT_NAME("parentName"), ENTITY_STATUS("entityStatus");

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
        QUERY("query"), RESOURCE("resource");

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
        CLIENTS("clients"), GROUPS("groups"), LOANS("loans"), SAVINGS("savings"), CLIENTIDENTIFIERS("clientIdentifiers");

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
