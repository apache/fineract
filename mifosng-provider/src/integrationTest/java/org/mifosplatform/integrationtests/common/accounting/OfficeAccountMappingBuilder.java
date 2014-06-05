package org.mifosplatform.integrationtests.common.accounting;

import java.util.HashMap;

import com.google.gson.Gson;

public class OfficeAccountMappingBuilder {

    public static String build(Integer officeId, Integer glAccountId) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("officeId", officeId);
        map.put("liabilityTransferInSuspenseAccountId", glAccountId);
        return new Gson().toJson(map);
    }
}
