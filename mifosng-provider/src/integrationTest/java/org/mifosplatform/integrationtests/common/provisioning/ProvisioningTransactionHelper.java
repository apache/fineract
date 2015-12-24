/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.provisioning;

import java.util.ArrayList;
import java.util.Map;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;


public class ProvisioningTransactionHelper {

    private static final String PROVISIONING_CATEGORY_URL = "/mifosng-provider/api/v1/provisioningcategory?" + Utils.TENANT_IDENTIFIER ;
    
    private static final String CREATE_PROVISIONING_CRITERIA_URL = "/mifosng-provider/api/v1/provisioningcriteria?" + Utils.TENANT_IDENTIFIER;
    private static final String CREATE_PROVISIONING_ENTRY_URL = "/mifosng-provider/api/v1/provisioningentries?" + Utils.TENANT_IDENTIFIER;
    
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;
    
    public ProvisioningTransactionHelper(RequestSpecification requestSpec, ResponseSpecification responeSpec) {
        this.requestSpec = requestSpec ;
        this.responseSpec = responeSpec ;
    }
    
    public ArrayList retrieveAllProvisioningCategories() {
        return Utils.performServerGet(requestSpec, responseSpec, PROVISIONING_CATEGORY_URL, "");
    }
    
    public Integer createProvisioningCriteria(final String provsioningCriteriaJson) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_PROVISIONING_CRITERIA_URL, provsioningCriteriaJson, "resourceId");
    }
    
    public Map retrieveProvisioningCriteria(final Integer criteriaId) {
        String url = "/mifosng-provider/api/v1/provisioningcriteria/"+criteriaId+"?"+Utils.TENANT_IDENTIFIER ;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }
    
    public Integer updateProvisioningCriteria(final Integer criteriaId, final String provsioningCriteriaJson) {
        String url = "/mifosng-provider/api/v1/provisioningcriteria/"+criteriaId+"?"+Utils.TENANT_IDENTIFIER ;
        return Utils.performServerPut(this.requestSpec, this.responseSpec, url, provsioningCriteriaJson, "resourceId");
    }
    
    public Integer deleteProvisioningCriteria(final Integer criteriaId) {
        String url = "/mifosng-provider/api/v1/provisioningcriteria/"+criteriaId+"?"+Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, url, "resourceId");
    }
    
    public Integer createProvisioningEntries(final String provsioningCriteriaJson) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_PROVISIONING_ENTRY_URL, provsioningCriteriaJson, "resourceId");
    }
    
    public Integer updateProvisioningEntry(final String command, final Integer entryId, String jsonBody) {
        String url = "/mifosng-provider/api/v1/provisioningentries/" + entryId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, jsonBody, "resourceId") ;
    }
    
    public Map retrieveProvisioningEntry(final Integer provisioningEntry) {
        String url = "/mifosng-provider/api/v1/provisioningentries/"+provisioningEntry+"?"+Utils.TENANT_IDENTIFIER; ;
        return Utils.performServerGet(requestSpec, responseSpec, url, "") ;
    }
    
    public Map retrieveProvisioningEntries(final Integer provisioningEntry) {
        String url = "/mifosng-provider/api/v1/provisioningentries/entries?entryId="+provisioningEntry+"&"+Utils.TENANT_IDENTIFIER; ;
        return Utils.performServerGet(requestSpec, responseSpec, url, "") ;
    }
    
    public Map retrieveAllProvisioningEntries() {
        String url = "/mifosng-provider/api/v1/provisioningentries?dateFormat=dd MMMM yyyy"+"&"+"locale=en"+"&"+Utils.TENANT_IDENTIFIER; ;
        return Utils.performServerGet(requestSpec, responseSpec, url, "") ;
    }
    
}
