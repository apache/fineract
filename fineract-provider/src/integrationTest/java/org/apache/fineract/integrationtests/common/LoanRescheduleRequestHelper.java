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
package org.apache.fineract.integrationtests.common;

import static org.junit.Assert.assertEquals;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class LoanRescheduleRequestHelper {
	private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;
    
    private static final String LOAN_RESCHEDULE_REQUEST_URL = "/fineract-provider/api/v1/rescheduleloans";
    
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
