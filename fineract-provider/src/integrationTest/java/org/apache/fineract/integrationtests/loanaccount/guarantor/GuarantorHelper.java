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
package org.apache.fineract.integrationtests.loanaccount.guarantor;

import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class GuarantorHelper {

    private static final String LOAN_URL = "/fineract-provider/api/v1/loans/";
    private static final String GUARANTOR_API_URL = "/guarantors/";
    private static final String TENANT = "?" + Utils.TENANT_IDENTIFIER;
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public GuarantorHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Integer createGuarantor(final Integer loanId, final String guarantorJSON) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, LOAN_URL + loanId + GUARANTOR_API_URL + TENANT, guarantorJSON,
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public HashMap updateGuarantor(final Integer guarantorId, final Integer loanId, final String guarantorJSON) {
        return Utils.performServerPut(this.requestSpec, this.responseSpec, LOAN_URL + loanId + GUARANTOR_API_URL + guarantorId + TENANT,
                guarantorJSON, CommonConstants.RESPONSE_CHANGES);
    }

    public HashMap deleteGuarantor(final Integer guarantorId, final Integer fundId, final Integer loanId) {
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, LOAN_URL + loanId + GUARANTOR_API_URL + guarantorId + TENANT
                + "&guarantorFundingId=" + fundId, "");
    }

    public HashMap deleteGuarantor(final Integer guarantorId, final Integer loanId) {
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, LOAN_URL + loanId + GUARANTOR_API_URL + guarantorId + TENANT,
                "");
    }

    public Object getGuarantor(final Integer guarantorId, final Integer loanId, final String jsonToGetBack) {
        return Utils.performServerGet(this.requestSpec, this.responseSpec, LOAN_URL + loanId + GUARANTOR_API_URL + guarantorId + TENANT,
                jsonToGetBack);
    }

    public List getAllGuarantor(final Integer loanId) {
        return Utils.performServerGet(this.requestSpec, this.responseSpec, LOAN_URL + loanId + GUARANTOR_API_URL + TENANT, "");
    }

}
