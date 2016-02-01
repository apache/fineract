/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.variableinstallments;

import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;


@SuppressWarnings("rawtypes")
public class VariableIntallmentsTransactionHelper {

    private final String URL = "https://localhost:8443/mifosng-provider/api/v1/loans/" ;
    
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
