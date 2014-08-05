/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.accounting;

import java.util.HashMap;

import com.google.gson.Gson;

public class FinancialActivityAccountsMappingBuilder {

    public static String build(Integer financialActivityId, Integer glAccountId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("financialActivityId", financialActivityId);
        map.put("glAccountId", glAccountId);
        return new Gson().toJson(map);
    }
}
