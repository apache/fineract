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
package org.apache.fineract.integrationtests.common.shares;

import java.util.Map;

import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;



public class ShareDividendsTransactionHelper {

    private static final String SHARE_PRODUCT_URL = "/fineract-provider/api/v1/shareproduct";
    private static final String DIVIDEND = "dividend" ;
    
    public static Integer createShareProductDividends(final Integer productId, final String dividendJson, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String url = SHARE_PRODUCT_URL + "/" + productId + "/" + DIVIDEND + "?" + Utils.TENANT_IDENTIFIER ;
        return Utils.performServerPost(requestSpec, responseSpec, url, dividendJson, "subResourceId");
    }
    
    public static Integer postCommand(final String command, final Integer productId, final Integer dividendId, String jsonBody, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String url = SHARE_PRODUCT_URL + "/" + productId + "/"+DIVIDEND + "/"+ dividendId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, url, jsonBody, "resourceId");
    }
    
    public static Map<String, Object> retrieveDividendDetails(final Integer productId, final Integer dividendId, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String url = SHARE_PRODUCT_URL + "/" + productId + "/"+DIVIDEND + "/"+ dividendId +"?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }
    
    public static Map<String, Object> retrieveAllDividends(final Integer productId, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String url = SHARE_PRODUCT_URL + "/" + productId + "/"+DIVIDEND +"?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }
}
