/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import java.util.HashMap;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class OfficeHelper {

	private static final String OFFICE_URL = "/mifosng-provider/api/v1/offices";
	private final RequestSpecification requestSpec;
	private final ResponseSpecification responseSpec;

	public OfficeHelper(final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec) {
		this.requestSpec = requestSpec;
		this.responseSpec = responseSpec;
	}

	public OfficeDomain retrieveOfficeByID(int id) {
		final String json = new Gson().toJson(Utils.performServerGet(
				requestSpec, responseSpec, OFFICE_URL + "/" + id + "?"
						+ Utils.TENANT_IDENTIFIER, ""));
		return new Gson().fromJson(json, new TypeToken<OfficeDomain>() {
		}.getType());
	}

	public Integer createOffice(final String openingDate) {
		String json = getAsJSON(openingDate);
		return Utils.performServerPost(this.requestSpec, this.responseSpec,
				OFFICE_URL + "?" + Utils.TENANT_IDENTIFIER, json,
				CommonConstants.RESPONSE_RESOURCE_ID);
	}

	public Integer updateOffice(int id, String name, String openingDate) {
		final HashMap map = new HashMap<>();
		map.put("name", name);
		map.put("dateFormat", "dd MMMM yyyy");
		map.put("locale", "en");
		map.put("openingDate", openingDate);

		System.out.println("map : " + map);

		return Utils.performServerPut(requestSpec, responseSpec, OFFICE_URL
				+ "/" + id + "?" + Utils.TENANT_IDENTIFIER,
				new Gson().toJson(map), "resourceId");
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
