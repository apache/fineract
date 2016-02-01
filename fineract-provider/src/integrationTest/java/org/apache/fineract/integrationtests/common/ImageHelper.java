/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.integrationtests.common;

import org.apache.http.HttpHeaders;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class ImageHelper {

    private static final String STAFF_IMAGE_URL = "/fineract-provider/api/v1/staff/";
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
