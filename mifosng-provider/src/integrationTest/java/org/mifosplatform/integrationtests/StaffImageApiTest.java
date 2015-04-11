/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import com.sun.jersey.core.util.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ImageHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.accounting.AccountHelper;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;
import org.mifosplatform.integrationtests.common.organisation.StaffHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import java.io.IOException;

public class StaffImageApiTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);

    }

    @Test
    public void createStaffImage() {

        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Integer imageId = ImageHelper.createImageForStaff(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image id should not be null", imageId);

    }

    @Test
    public void getStaffImage(){
        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Integer imageId = ImageHelper.createImageForStaff(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image id should not be null", imageId);
        String imageAsText = ImageHelper.getStaffImageAsText(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image id should not be null", imageAsText);
    }

    @Test
    public void getStaffImageAsBinary(){
        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Integer imageId = ImageHelper.createImageForStaff(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image id should not be null", imageId);
        byte[] imageAsBytes = ImageHelper.getStaffImageAsBinary(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image content should not be null", imageAsBytes);
    }

    @Test
    public void updateImage() {

        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Integer imageId = ImageHelper.createImageForStaff(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image id should not be null", imageId);
        imageId = ImageHelper.updateImageForStaff(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image id should not be null", imageId);

    }

    @Test
    public void deleteStaffImage() {

        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Integer imageId = ImageHelper.createImageForStaff(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image id should not be null", imageId);
        imageId = ImageHelper.deleteStaffImage(this.requestSpec, this.responseSpec, staffId);
        Assert.assertNotNull("Image id should not be null", imageId);

    }



}
