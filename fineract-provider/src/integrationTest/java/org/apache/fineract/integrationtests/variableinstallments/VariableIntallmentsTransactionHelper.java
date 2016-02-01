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
package org.apache.fineract.integrationtests.variableinstallments;

import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;


@SuppressWarnings("rawtypes")
public class VariableIntallmentsTransactionHelper {

    private final String URL = "https://localhost:8443/fineract-provider/api/v1/loans/" ;
    
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;
    
    public VariableIntallmentsTransactionHelper(final RequestSpecification requestSpec, 
            final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec ;
        this.responseSpec = responseSpec ;
    }
    
    
    public Map retrieveSchedule(Integer loanId) {
        String url = URL+loanId+"?associations=repaymentSchedule&exclude=guarantors&"+Utils.TENANT_IDENTIFIER ;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }
    
    public HashMap validateVariations(final String exceptions, Integer loanId) {
        String url = URL+loanId+"/schedule?command=calculateLoanSchedule&"+Utils.TENANT_IDENTIFIER ;
        return Utils.performServerPost(this.requestSpec, this.responseSpec, url, exceptions, "");
    }
    
    public HashMap submitVariations(final String exceptions, Integer loanId) {
        String url = URL+loanId+"/schedule?command=addVariations&"+Utils.TENANT_IDENTIFIER ;
        return Utils.performServerPost(this.requestSpec, this.responseSpec, url, exceptions, "");
    }
}
