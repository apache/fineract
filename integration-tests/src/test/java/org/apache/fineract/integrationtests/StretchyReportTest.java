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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtest.stretchyreports.StretchyReportHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class StretchyReportTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private StretchyReportHelper stretchyReportHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testReportWithPagination() {
        this.stretchyReportHelper = new StretchyReportHelper(this.requestSpec, this.responseSpec);

        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        StretchyReportHelper validationErrorHelper = new StretchyReportHelper(this.requestSpec, errorResponse);
        for (int i = 0; i < 20; i++) {
            final int clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
            ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        }
        LinkedHashMap reportData = this.stretchyReportHelper.getStretchyReportDetail(this.requestSpec, this.responseSpec, "");
        ArrayList<String> rdata = (ArrayList<String>) reportData.get("data");
        int reportDataSize = rdata.size();
        Assertions.assertNotNull(reportDataSize);

        boolean isPaginationAllowed = true;
        int pageNo = 0;
        int pageCount = 0;
        int pageContent = Math.toIntExact(GlobalConfigurationHelper
                .getGlobalConfigurationByName(requestSpec, responseSpec, "reports-pagination-number-of-items-per-page").getValue());
        pageCount = reportDataSize / pageContent;
        if (reportDataSize % pageContent != 0) {
            pageCount++;
        }
        int resultantReportWithLimitSize = 0;
        while (pageCount > pageNo) {
            LinkedHashMap reportDataSlice = this.stretchyReportHelper.getStretchyReportDetailWithPagination(this.requestSpec,
                    this.responseSpec, "", isPaginationAllowed, pageNo);
            ArrayList<String> data = (ArrayList<String>) reportDataSlice.get("data");
            resultantReportWithLimitSize += data.size();
            pageNo++;
        }
        assertEquals(resultantReportWithLimitSize, reportDataSize);

        log.info("--------------------------Report with no pageNo--------------------------");
        ArrayList<HashMap> error = (ArrayList<HashMap>) validationErrorHelper.getStretchyReportDetailWithPaginationWithoutPageNo(
                this.requestSpec, errorResponse, CommonConstants.RESPONSE_ERROR, isPaginationAllowed);
        assertEquals("validation.msg.null.offset.cannot.be.blank", error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }
}
