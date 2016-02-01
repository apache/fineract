/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor;

import java.util.HashSet;
import java.util.Set;

public class GuarantorConstants {

    public static final String GUARANTOR_RELATIONSHIP_CODE_NAME = "GuarantorRelationship";

    /***
     * Enum of all parameters passed in while creating/updating a loan product
     ***/
    public static enum GUARANTOR_JSON_INPUT_PARAMS {
        LOAN_ID("loanId"), CLIENT_RELATIONSHIP_TYPE_ID("clientRelationshipTypeId"), GUARANTOR_TYPE_ID("guarantorTypeId"), ENTITY_ID(
                "entityId"), FIRSTNAME("firstname"), LASTNAME("lastname"), ADDRESS_LINE_1("addressLine1"), ADDRESS_LINE_2("addressLine2"), CITY(
                "city"), STATE("state"), ZIP("zip"), COUNTRY("country"), MOBILE_NUMBER("mobileNumber"), PHONE_NUMBER("housePhoneNumber"), COMMENT(
                "comment"), DATE_OF_BIRTH("dob"), AMOUNT("amount"), SAVINGS_ID("savingsId");

        private final String value;

        private GUARANTOR_JSON_INPUT_PARAMS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();
        static {
            for (final GUARANTOR_JSON_INPUT_PARAMS type : GUARANTOR_JSON_INPUT_PARAMS.values()) {
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
    
    public static final String GUARANTOR_SELF_GUARANTEE_ERROR = "min.self.guarantee.required";
    public static final String GUARANTOR_EXTERNAL_GUARANTEE_ERROR = "min.external.guarantee.required";
    public static final String GUARANTOR_MANDATORY_GUARANTEE_ERROR = "mandated.guarantee.required";
    public static final String GUARANTOR_INSUFFICIENT_BALANCE_ERROR = "insufficient.balance";
    public static final String GUARANTOR_NOT_ACTIVE_ERROR = "not.active";
    
}
