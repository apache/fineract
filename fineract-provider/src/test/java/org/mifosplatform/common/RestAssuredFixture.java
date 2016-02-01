/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.common;

import com.google.common.base.Preconditions;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * REST integration tests helper. Intended to encapsulate the current
 * RestAssured-based implementation as private. May be REST integration tests
 * should instead be written using e.g. Spring's TestRestTemplate or Square's
 * Retrofit (already used in the Mifos X Android client).
 */
public class RestAssuredFixture {

	private final int httpPort;

	private ResponseSpecification responseSpec;
	private RequestSpecification requestSpec;

	public RestAssuredFixture(int httpPort) {
		super();
		this.httpPort = httpPort;
		Utils.initializeRESTAssured();
		RestAssured.port = httpPort;
		this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
		this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
		this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
	}

	public <T> T httpGet(String apiPath, String jsonAttributeToGetBack) {
		return Utils.performServerGet(this.requestSpec, this.responseSpec, getApiPath(apiPath), jsonAttributeToGetBack);
	}

	public <T> T httpGet(String apiPath) {
		return httpGet(apiPath, "");
	}

	private String getApiPath(String apiPath) {
        Preconditions.checkArgument(apiPath.startsWith("/"), "trailingApiUrl must start with slash: " + apiPath);
        return "/mifosng-provider/api/v1" + apiPath + "?tenantIdentifier=default";
	}

	protected String getApiUrl(String apiPath) {
		return "http://localhost:" + httpPort + getApiPath(apiPath);
    }

}
