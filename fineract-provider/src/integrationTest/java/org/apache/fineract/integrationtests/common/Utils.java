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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util for RestAssured tests. This class here in src/integrationTest is
 * copy/pasted to src/test; please keep them in sync.
 */
@SuppressWarnings("unchecked")
public class Utils {

   private final static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final String TENANT_PARAM_NAME = "tenantIdentifier";
    public static final String DEFAULT_TENANT = "default";
    public static final String TENANT_IDENTIFIER = TENANT_PARAM_NAME + '=' + DEFAULT_TENANT;

    public static final String TENANT_TIME_ZONE = "Asia/Kolkata";

    private static final String HEALTH_URL = "/fineract-provider/actuator/health";
    private static final String LOGIN_URL  = "/fineract-provider/api/v1/authentication?username=mifos&password=password&" + TENANT_IDENTIFIER;

    public static void initializeRESTAssured() {
        RestAssured.baseURI = "https://localhost";
        RestAssured.port = 8443;
        RestAssured.keystore("src/main/resources/keystore.jks", "openmf");
    }

    private static void awaitSpringBootActuatorHealthyUp() {
        int attempt = 0;
        final int max_attempts = 10;
        Response response = null;
        Exception lastException = null;
        do {
            try {
                response = RestAssured.get(HEALTH_URL);
                int healthHttpStatus = response.statusCode();
                if (healthHttpStatus == 200) {
                    logger.info("{} return HTTP 200, application is now ready for integration testing!", HEALTH_URL);
                    return;
                } else {
                    logger.info("{} returned HTTP {}, going to wait and retry (attempt {})", new Object[] { HEALTH_URL, healthHttpStatus, attempt++ });
                    sleep(3);
                }
            } catch (Exception e) {
                logger.info("{} caused {}, going to wait and retry (attempt {})", HEALTH_URL, new Object[] {  e.getMessage(), attempt++ });
                lastException = e;
                sleep(3);
            }
        } while (attempt < max_attempts);

        if (lastException != null) {
            logger.error("{} still not reachable, giving up", HEALTH_URL, lastException);
            throw new AssertionError(HEALTH_URL + " not reachable", lastException);
        } else {
            logger.error("{} still has not returned HTTP 200, giving up; (last) body: ", HEALTH_URL, response.prettyPrint());
            fail(HEALTH_URL + " returned " + response.prettyPrint());
        }
    }

    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            logger.warn("Unexpected InterruptedException", e);
            throw new IllegalStateException("Unexpected InterruptedException", e);
        }
    }

    public static String loginIntoServerAndGetBase64EncodedAuthenticationKey() {
        awaitSpringBootActuatorHealthyUp();
        try {
            logger.info("Logging in, for integration test...");
            System.out.println("-----------------------------------LOGIN-----------------------------------------");
            final String json = RestAssured.post(LOGIN_URL).asString();
            assertThat("Failed to login into fineract platform", StringUtils.isBlank(json), is(false));
            return JsonPath.with(json).get("base64EncodedAuthenticationKey");
        } catch (final Exception e) {
            if (e instanceof HttpHostConnectException) {
                final HttpHostConnectException hh = (HttpHostConnectException) e;
                fail("Failed to connect to fineract platform:" + hh.getMessage());
            }

            throw new RuntimeException(e);
        }
    }

    public static <T> T performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asString();
        if (jsonAttributeToGetBack == null) { return (T) json; }
        return (T) from(json).get(jsonAttributeToGetBack);
    }

    public static String performGetTextResponse(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                                final String getURL){
        return given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asString();
    }

    public static byte[] performGetBinaryResponse(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                                final String getURL){
        return given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asByteArray();
    }

    public static <T> T performServerPost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String postURL, final String jsonBodyToSend, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).body(jsonBodyToSend).expect().spec(responseSpec).log().ifError().when().post(postURL)
                .andReturn().asString();
        if (jsonAttributeToGetBack == null) { return (T) json; }
        return (T) from(json).get(jsonAttributeToGetBack);
    }

    public static <T> T performServerPut(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String putURL, final String jsonBodyToSend, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).body(jsonBodyToSend).expect().spec(responseSpec).log().ifError().when().put(putURL)
                .andReturn().asString();
        return (T) from(json).get(jsonAttributeToGetBack);
    }

    public static <T> T performServerDelete(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String deleteURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().delete(deleteURL).andReturn()
                .asString();
        return (T) from(json).get(jsonAttributeToGetBack);
    }

    public static <T> T performServerDelete(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String deleteURL, final String jsonBodyToSend, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).body(jsonBodyToSend).expect().spec(responseSpec).log().ifError().when()
                .delete(deleteURL).andReturn().asString();
        return (T) (jsonAttributeToGetBack == null ? json : from(json).get(jsonAttributeToGetBack));
    }

    public static String convertDateToURLFormat(final String dateToBeConvert) {
        final SimpleDateFormat oldFormat = new SimpleDateFormat("dd MMMMMM yyyy", Locale.US);
        final SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        String reformattedStr = "";
        try {
            reformattedStr = newFormat.format(oldFormat.parse(dateToBeConvert));
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return reformattedStr;
    }

    public static String randomStringGenerator(final String prefix, final int len, final String sourceSetString) {
        final int lengthOfSource = sourceSetString.length();
        final Random rnd = new Random();
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append((sourceSetString).charAt(rnd.nextInt(lengthOfSource)));
        }
        return (prefix + (sb.toString()));
    }

    public static String randomStringGenerator(final String prefix, final int len) {
        return randomStringGenerator(prefix, len, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return randomStringGenerator(prefix, lenOfRandomSuffix);
    }
    public static Long randomNumberGenerator(final int expectedLength){
       final String source="1234567890";
       final int lengthofSource=source.length();
       final Random random=new Random();
       StringBuilder stringBuilder=new StringBuilder(expectedLength);
        for (int i = 0; i < expectedLength; i++) {
            stringBuilder.append(source.charAt(random.nextInt(lengthofSource)));
        }
        return Long.parseLong(stringBuilder.toString());
    }

    public static String convertDateToURLFormat(final Calendar dateToBeConvert) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        return dateFormat.format(dateToBeConvert.getTime());
    }

    public static LocalDate getLocalDateOfTenant() {
        LocalDate today = new LocalDate();
        final DateTimeZone zone = DateTimeZone.forID(TENANT_TIME_ZONE);
        if (zone != null) {
            today = new LocalDate(zone);
        }
        return today;
    }

    public static TimeZone getTimeZoneOfTenant() {
        return TimeZone.getTimeZone(TENANT_TIME_ZONE);
    }

    public static String performServerTemplatePost(final RequestSpecification requestSpec,final ResponseSpecification responseSpec,
                                                   final String postURL,final String legalFormType,final File file,final String locale,final String dateFormat) {

        final String importDocumentId=given().spec(requestSpec)
                .queryParam("legalFormType",legalFormType)
                .multiPart("file",file)
                .formParam("locale",locale)
                .formParam("dateFormat",dateFormat)
                .expect().spec(responseSpec).
                log().ifError().when().post(postURL)
                .andReturn().asString();
        return importDocumentId;
    }

    public static String performServerOutputTemplateLocationGet(final RequestSpecification requestSpec,final ResponseSpecification responseSpec,
                                                                final String getURL,final String importDocumentId){
        final String templateLocation=given().spec(requestSpec).
                queryParam("importDocumentId",importDocumentId)
                .expect().spec(responseSpec)
                .log().ifError().when().get(getURL)
                .andReturn().asString();
        return templateLocation.substring(1,templateLocation.length()-1);
    }
}

