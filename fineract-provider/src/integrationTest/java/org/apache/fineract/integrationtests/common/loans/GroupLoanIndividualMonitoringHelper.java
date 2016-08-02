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
package org.apache.fineract.integrationtests.common.loans;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GroupLoanIndividualMonitoringHelper {
	private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String GROUP_LOAN_INDIVIDUAL_MONITORING_URL = "/fineract-provider/api/v1/grouploanindividualmonitoring/";

	public GroupLoanIndividualMonitoringHelper(
			RequestSpecification requestSpec, ResponseSpecification responseSpec) {
		this.requestSpec = requestSpec;
		this.responseSpec = responseSpec;
	}
    
	public String getClientMembersByLoanId(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
		final String URL = GROUP_LOAN_INDIVIDUAL_MONITORING_URL+ loanID + "?"+ Utils.TENANT_IDENTIFIER;
        final String response = Utils.performGetTextResponse(requestSpec, responseSpec, URL);
        return response;
    }
}
