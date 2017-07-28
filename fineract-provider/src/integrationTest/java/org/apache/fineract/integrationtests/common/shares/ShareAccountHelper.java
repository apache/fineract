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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;


public class ShareAccountHelper {

    private static final String LOCALE = "en_GB";
    
    private String clientId ;
    
    private String productId ;
    
    private String submittedDate ;
    
    private String externalId ;
    
    private String savingsAccountId ;
    
    private String requestedShares ;
    
    private String applicationDate ;
    
    private String allowDividendCalculationForInactiveClients ;
    
    private String minimumActivePeriod ;
    
    private String minimumActivePeriodFrequencyType ;
    
    private String lockinPeriodFrequency ;
    
    private String lockinPeriodFrequencyType ;
    
    private List<Map<String, Object>> charges = null ;
            //chargeId , amount
    
    public String build() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", LOCALE);
        if(this.clientId != null) {
            map.put("clientId", this.clientId) ;    
        }
        if(this.productId != null) {
            map.put("productId", this.productId) ;
        }
        map.put("dateFormat", "dd MMMM yyyy");
        
        if(this.savingsAccountId != null) {
            map.put("savingsAccountId", savingsAccountId) ;
        }
 
        if(externalId != null) {
            map.put("externalId", this.externalId) ;
        }
        
        if(submittedDate != null) {
            map.put("submittedDate", this.submittedDate) ;
        }
        
        if(applicationDate != null) {
            map.put("applicationDate", this.applicationDate) ;
        }
        
        if(this.requestedShares != null) {
            map.put("requestedShares", this.requestedShares) ;
        }
        
        if(this.allowDividendCalculationForInactiveClients != null) {
            map.put("allowDividendCalculationForInactiveClients", this.allowDividendCalculationForInactiveClients) ;
        }
        
        if(this.charges != null) {
           map.put("charges", this.charges) ;
        }
        
        String shareAccountCreateJson = new Gson().toJson(map);
        System.out.println(shareAccountCreateJson);
        return shareAccountCreateJson;
    }
    
    public ShareAccountHelper withClientId(final String clientId) {
        this.clientId = clientId ;
        return this ;
    }
    
    public ShareAccountHelper withProductId(final String productId) {
        this.productId = productId ;
        return this ;
    }
    
    public ShareAccountHelper withSavingsAccountId(final String savingsAccountId) {
        this.savingsAccountId = savingsAccountId ;
        return this ;
    }
    
    public ShareAccountHelper withSubmittedDate(final String submittedDate) {
        this.submittedDate = submittedDate ;
        return this ;
    }
    
    public ShareAccountHelper withRequestedShares(final String requestedShares) {
        this.requestedShares = requestedShares ;
        return this ;
    }
    
    public ShareAccountHelper withApplicationDate(final String applicationDate) {
        this.applicationDate = applicationDate ;
        return this ;
    }
    
    public ShareAccountHelper withExternalId(final String externalId) {
        this.externalId = externalId ;
        return this ;
    }
    
    public ShareAccountHelper withCharges(final List<Map<String,Object>> charges) {
        this.charges = new ArrayList<>() ;
        this.charges.addAll(charges) ;
        return this ;
    }
}
