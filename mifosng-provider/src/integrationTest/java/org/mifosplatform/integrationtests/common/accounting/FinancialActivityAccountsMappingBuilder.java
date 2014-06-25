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
