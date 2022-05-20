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
package org.apache.fineract.integrationtest.stretchyreports;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.LinkedHashMap;
import org.apache.fineract.integrationtests.common.Utils;

public class StretchyReportHelper {

    private static final String STRETCHY_REPORT_URL = "/fineract-provider/api/v1/runreports";
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    private String nameOfReport = "report_test";
    private String reportType = "Table";

    public StretchyReportHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public LinkedHashMap getStretchyReportDetail(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String jsonReturn) {
        final String url = STRETCHY_REPORT_URL + "/" + "Client Listing" + "?" + Utils.TENANT_IDENTIFIER + "&R_officeId=1";
        final LinkedHashMap response = Utils.performServerGet(requestSpec, responseSpec, url, jsonReturn);
        return response;
    }

    public LinkedHashMap getStretchyReportDetailWithPagination(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String jsonReturn, final boolean isPaginationAllowed, final int pageNo) {
        int orderBy = 1;
        String url = STRETCHY_REPORT_URL + "/" + "Client Listing" + "?" + Utils.TENANT_IDENTIFIER + "&offset=" + pageNo + "&orderby="
                + orderBy + "&isPaginationAllowed=" + isPaginationAllowed + "&R_officeId=1";
        final LinkedHashMap response = Utils.performServerGet(requestSpec, responseSpec, url, jsonReturn);
        return response;
    }

    public Object getStretchyReportDetailWithPaginationWithoutPageNo(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String jsonReturn, final boolean isPaginationAllowed) {
        int orderBy = 1;
        String url = STRETCHY_REPORT_URL + "/" + "Client Listing" + "?" + Utils.TENANT_IDENTIFIER + "&orderby=" + orderBy
                + "&isPaginationAllowed=" + isPaginationAllowed + "&R_officeId=1";
        final Object response = Utils.performServerGet(requestSpec, responseSpec, url, jsonReturn);
        return response;
    }

    public Object getStretchyReportDataDetail(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String url, final String jsonReturn) {

        final Object response = Utils.performServerGet(requestSpec, responseSpec, url, jsonReturn);
        return response;
    }

}
