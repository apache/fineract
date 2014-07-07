/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.integrationtests.common;

import static org.junit.Assert.assertEquals;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class LoanRescheduleRequestHelper {
	private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;
    
    private static final String LOAN_RESCHEDULE_REQUEST_URL = "/mifosng-provider/api/v1/rescheduleloans";
    
    public LoanRescheduleRequestHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }
    
    public Integer createLoanRescheduleRequest(final String requestJSON) {
    	final String URL = LOAN_RESCHEDULE_REQUEST_URL + "?" + Utils.TENANT_IDENTIFIER; 
    	return Utils.performServerPost(this.requestSpec, this.responseSpec, URL, requestJSON, "resourceId");
    }
    
    public Integer rejectLoanRescheduleRequest(final Integer requestId, final String requestJSON) {
    	final String URL = LOAN_RESCHEDULE_REQUEST_URL + "/" + requestId + "?" + Utils.TENANT_IDENTIFIER + "&command=reject";
    	
    	return Utils.performServerPost(this.requestSpec, this.responseSpec, URL, requestJSON, "resourceId");
    }
    
    public Integer approveLoanRescheduleRequest(final Integer requestId, final String requestJSON) {
    	final String URL = LOAN_RESCHEDULE_REQUEST_URL + "/" + requestId + "?" + Utils.TENANT_IDENTIFIER + "&command=approve";
    	
    	return Utils.performServerPost(this.requestSpec, this.responseSpec, URL, requestJSON, "resourceId");
    }
    
    public Object getLoanRescheduleRequest(final Integer requestId, final String param) {
    	final String URL = LOAN_RESCHEDULE_REQUEST_URL + "/" + requestId + "?" + Utils.TENANT_IDENTIFIER;
    	
    	return Utils.performServerGet(requestSpec, responseSpec, URL, param);
    }
    
    public void verifyCreationOfLoanRescheduleRequest(final Integer requestId) {
    	final String URL = LOAN_RESCHEDULE_REQUEST_URL + "/" + requestId + "?" + Utils.TENANT_IDENTIFIER;
    	
    	final Integer id = Utils.performServerGet(requestSpec, responseSpec, URL, "id");
    	assertEquals("ERROR IN CREATING LOAN RESCHEDULE REQUEST", requestId, id);
    }
}
