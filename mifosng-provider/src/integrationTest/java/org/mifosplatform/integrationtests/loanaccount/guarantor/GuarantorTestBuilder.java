/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.loanaccount.guarantor;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;

public class GuarantorTestBuilder {

    private static final String GUARANTOR_TYPE_CUSTOMER = "1";
    @SuppressWarnings("unused")
    private static final String GUARANTOR_TYPE_STAFF = "2";
    private static final String GUARANTOR_TYPE_EXTERNAL = "3";

    private String guarantorTypeId = "1";
    private String entityId = null;
    private String addressLine1 = "addressLine1";
    private String addressLine2 = "addressLine2";
    private String city = "city";
    private String state = "state";
    private String zip = "123456";
    private String guaranteeAmount = "500";
    private String savingsId = null;

    public String build() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("guarantorTypeId", guarantorTypeId);
        map.put("locale", "en");
        if (GUARANTOR_TYPE_EXTERNAL.equals(guarantorTypeId)) {
            map.put("firstname", Utils.randomNameGenerator("guarantor_FirstName_", 5));
            map.put("lastname", Utils.randomNameGenerator("guarantor_LastName_", 4));
            map.put("addressLine1", addressLine1);
            map.put("addressLine2", addressLine2);
            map.put("city", city);
            map.put("state", state);
            map.put("zip", zip);

        } else if (GUARANTOR_TYPE_CUSTOMER.equals(guarantorTypeId)) {
            map.put("entityId", entityId);
            map.put("amount", guaranteeAmount);
            map.put("savingsId", savingsId);
        }
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public GuarantorTestBuilder existingCustomerWithGuaranteeAmount(final String entityId, final String savingsId,
            final String guaranteeAmount) {
        this.entityId = entityId;
        this.savingsId = savingsId;
        this.guaranteeAmount = guaranteeAmount;
        this.guarantorTypeId = GUARANTOR_TYPE_CUSTOMER;
        return this;
    }

    public GuarantorTestBuilder existingCustomerWithoutGuaranteeAmount(final String entityId) {
        this.entityId = entityId;
        this.savingsId = null;
        this.guaranteeAmount = null;
        this.guarantorTypeId = GUARANTOR_TYPE_CUSTOMER;
        return this;
    }
    
    public GuarantorTestBuilder externalCustomer() {
        this.guarantorTypeId = GUARANTOR_TYPE_EXTERNAL;
        return this;
    }
}
