/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.common;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.HttpHostConnectException;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Util for RestAssured tests. This class here in src/test is copy/pasted :(
 * from and identical to the one in src/integrationTest; please keep it in sync.
 * The cunning plan is that, now that we have Spring Boot + MariaDB4j,
 * eventually do completely away with src/integrationTest and have only
 * src/test.. can you help? ;)
 */
@SuppressWarnings("unchecked")
public class Utils {

    public static final String TENANT_IDENTIFIER = "tenantIdentifier=default";

    private static final String LOGIN_URL = "/mifosng-provider/api/v1/authentication?username=mifos&password=password&" + TENANT_IDENTIFIER;

    public static void initializeRESTAssured() {
        RestAssured.baseURI = "https://localhost";
        RestAssured.port = 8443;
        RestAssured.keystore("src/main/resources/keystore.jks", "openmf");
    }

    public static String loginIntoServerAndGetBase64EncodedAuthenticationKey() {
        try {
            System.out.println("-----------------------------------LOGIN-----------------------------------------");
            final String json = RestAssured.post(LOGIN_URL).asString();
            assertThat("Failed to login into mifosx platform", StringUtils.isBlank(json), is(false));
            return JsonPath.with(json).get("base64EncodedAuthenticationKey");
        } catch (final Exception e) {
            if (e instanceof HttpHostConnectException) {
                final HttpHostConnectException hh = (HttpHostConnectException) e;
                fail("Failed to connect to mifosx platform:" + hh.getMessage());
            }

            throw new RuntimeException(e);
        }
    }

    public static <T> T performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asString();
        return (T) from(json).get(jsonAttributeToGetBack);
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

    public static String convertDateToURLFormat(String dateToBeConvert) {
        SimpleDateFormat oldFormat = new SimpleDateFormat("dd MMMMMM yyyy", Locale.US);
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
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

}
