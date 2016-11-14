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
package org.apache.fineract.infrastructure.campaigns.sms.data;

public class SmsProviderData {

    private Long id;

    private String tenantId;
    
    private String phoneNo;
    
    private String providerAppKey;

    private String providerName;

    private String providerDescription;

    public SmsProviderData(final Long id, final String providerAppKey, final String providerName, final String providerDescription, final String tenantId, 
            final String phoneNo) {
        this.id = id;
        this.providerAppKey = providerAppKey;
        this.providerName = providerName;
        this.providerDescription = providerDescription;
        this.tenantId = tenantId;
        this.phoneNo = phoneNo;
    }
    
    public SmsProviderData() {
        
    }

    public Long getId() {
        return this.id;
    }

    public String getProviderAppKey() {
        return this.providerAppKey;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public String getProviderDescription() {
        return this.providerDescription;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public String getPhoneNo() {
        return this.phoneNo;
    }
}
