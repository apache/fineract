package org.mifosplatform.integrationtests.common;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static com.jayway.restassured.path.json.JsonPath.from;


public class Utils {
    private static final String LOGIN_URL = "/mifosng-provider/api/v1/authentication?username=mifos&password=password&tenantIdentifier=default";

    public static <T> T performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                         final String getURL, final String jsonAttributeToGetBack){
        String json = given().spec(requestSpec)
                .expect().spec(responseSpec).log().ifError()
                .when().get(getURL)
                .andReturn().asString();
        return (T) from(json).get(jsonAttributeToGetBack);
    }

    public static <T> T performServerPost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                          final String postURL, final String jsonBodyToSend, final String jsonAttributeToGetBack){
        String json = given().spec(requestSpec).body(jsonBodyToSend)
                .expect().spec(responseSpec).log().ifError()
                .when().post(postURL)
                .andReturn().asString();
        return (T) from(json).get(jsonAttributeToGetBack);
    }

    public static String loginIntoServerAndGetBase64EncodedAuthenticationKey() {
        System.out.println("-----------------------------------LOGIN-----------------------------------------");
        String json = RestAssured.post(LOGIN_URL).asString();
        return JsonPath.with(json).get("base64EncodedAuthenticationKey");
    }
}
