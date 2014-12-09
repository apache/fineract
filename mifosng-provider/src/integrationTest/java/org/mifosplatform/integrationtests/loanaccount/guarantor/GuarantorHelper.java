/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.loanaccount.guarantor;

import java.util.HashMap;
import java.util.List;

import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class GuarantorHelper {

    private static final String LOAN_URL = "/mifosng-provider/api/v1/loans/";
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
