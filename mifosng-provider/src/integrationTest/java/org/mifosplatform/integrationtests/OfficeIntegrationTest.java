/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.OfficeDomain;
import org.mifosplatform.integrationtests.common.OfficeHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class OfficeIntegrationTest {

	private ResponseSpecification responseSpec;
	private RequestSpecification requestSpec;

	@Before
	public void setup() {
		Utils.initializeRESTAssured();
		this.requestSpec = new RequestSpecBuilder().setContentType(
				ContentType.JSON).build();
		this.requestSpec
				.header("Authorization",
						"Basic "
								+ Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
		this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200)
				.build();
	}

	@Test
	public void testOfficeModification() {
		OfficeHelper oh = new OfficeHelper(requestSpec, responseSpec);
		int officeId = oh.createOffice("01 July 2007");
		String name = Utils.randomNameGenerator("New_Office_", 4);
		String date = "02 July 2007";
		String[] dateArr = { "2007", "7", "2" };

		oh.updateOffice(officeId, name, date);
		OfficeDomain newOffice = oh.retrieveOfficeByID(officeId);

		Assert.assertTrue(name.equals(newOffice.getName()));
		Assert.assertArrayEquals(dateArr, newOffice.getOpeningDate());
	}
}
