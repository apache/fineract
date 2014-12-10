package org.mifosplatform.integrationtests.common.office;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.mifosplatform.integrationtests.common.Utils;

import java.util.HashMap;
import java.util.List;

public class OfficeResourceHandler {

    private static final String OFFICES_URL = "/mifosng-provider/api/v1/offices";
    private static final String CREATE_OFFICES_URL = OFFICES_URL + "?" + Utils.TENANT_IDENTIFIER;

    public static Integer createOffice(final String officeJSON,
                                     final RequestSpecification requestSpec,
                                     final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_OFFICES_URL, officeJSON, "resourceId");
    }

    public Integer createOffice(final String openingDate) {
        String json = getAsJSON(openingDate);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, OFFICE_URL + "?" + Utils.TENANT_IDENTIFIER, json,
                CommonConstants.RESPONSE_RESOURCE_ID);
    }
}
