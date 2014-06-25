package org.mifosplatform.integrationtests.common;

import java.util.HashMap;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;


public class OfficeHelper {

    private static final String OFFICE_URL = "/mifosng-provider/api/v1/offices";
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public OfficeHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Integer createOffice(final String openingDate) {
        String json = getAsJSON(openingDate);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, OFFICE_URL + "?" + Utils.TENANT_IDENTIFIER, json,
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

      
    public static String getAsJSON(final String openingDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("parentId", "1");
        map.put("name", Utils.randomNameGenerator("Office_", 4));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("openingDate", openingDate);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }
}
