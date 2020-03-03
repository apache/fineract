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
package org.apache.fineract.integrationtests;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.integrationtests.common.ImageHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
