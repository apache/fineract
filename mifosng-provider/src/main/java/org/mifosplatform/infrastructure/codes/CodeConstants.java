/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes;

import java.util.HashSet;
import java.util.Set;

public class CodeConstants {

    /***
     * Enum of all parameters passed in while creating/updating a code and code
     * value
     ***/
    public static enum CODEVALUE_JSON_INPUT_PARAMS {
        CODEVALUE_ID("id"), NAME("name"), POSITION("position"), DESCRIPTION("description");

        private final String value;

        private CODEVALUE_JSON_INPUT_PARAMS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();
        static {
            for (final CODEVALUE_JSON_INPUT_PARAMS type : CODEVALUE_JSON_INPUT_PARAMS.values()) {
                values.add(type.value);
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
