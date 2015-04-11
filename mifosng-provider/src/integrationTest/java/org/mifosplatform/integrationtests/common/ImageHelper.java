/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import org.apache.http.HttpHeaders;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class ImageHelper {

    private static final String STAFF_IMAGE_URL = "/mifosng-provider/api/v1/staff/";
    private static final String IMAGES_URI = "/images";

    public static Integer createImageForStaff(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            Integer staffId) {
        System.out.println("---------------------------------CREATING AN IMAGE FOR STAFF---------------------------------------------");
        String URL = STAFF_IMAGE_URL + staffId + IMAGES_URI + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, URL, generateImageAsText(), "resourceId");
    }

    public static Integer updateImageForStaff(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            Integer staffId) {
        System.out.println("---------------------------------UPDATING AN IMAGE FOR STAFF---------------------------------------------");
        String URL = STAFF_IMAGE_URL + staffId + IMAGES_URI + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, URL, generateImageAsText(), "resourceId");
    }

    public static String getStaffImageAsText(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            Integer staffId) {
        System.out.println("---------------------------------RETRIEVING STAFF IMAGE---------------------------------------------");
        String URL = STAFF_IMAGE_URL + staffId + IMAGES_URI + "?" + Utils.TENANT_IDENTIFIER;
        requestSpec.header(HttpHeaders.ACCEPT, "text/plain");
        return Utils.performGetTextResponse(requestSpec, responseSpec, URL);
    }

    public static byte[] getStaffImageAsBinary(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            Integer staffId) {
        System.out.println("---------------------------------RETRIEVING STAFF IMAGE---------------------------------------------");
        String URL = STAFF_IMAGE_URL + staffId + IMAGES_URI + "?" + Utils.TENANT_IDENTIFIER;
        requestSpec.header(HttpHeaders.ACCEPT, "application/octet-stream");
        return Utils.performGetBinaryResponse(requestSpec, responseSpec, URL);
    }

    public static Integer deleteStaffImage(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, Integer staffId) {
        System.out.println("---------------------------------RETRIEVING STAFF IMAGE---------------------------------------------");
        String URL = STAFF_IMAGE_URL + staffId + IMAGES_URI + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(requestSpec, responseSpec, URL, "resourceId");
    }

    private static String generateImageAsText() {
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJ\n"
                + "bWFnZVJlYWR5ccllPAAAAJ1JREFUeNpi+P//PwMIA4E9EG8E4idQDGLbw+WhiiqA+D8OXAFVAzbp\n"
                + "DxBvB2JLIGaGYkuoGEjOhhFIHAbij0BdPgxYACMj42ogJQpifwBiXSDeC8JIbt4LxSC5DyxQjTeB\n"
                + "+BeaYb+Q5EBOAVutCzMJHUNNPADzzDokiYdAfAmJvwLkGeTgWQfyKZICS6hYBTwc0QL8ORSjBDhA\n" + "gAEAOg13B6R/SAgAAAAASUVORK5CYII=";
    }
}
