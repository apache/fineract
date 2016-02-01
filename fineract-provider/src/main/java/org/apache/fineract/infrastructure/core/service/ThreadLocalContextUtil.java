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
package org.apache.fineract.infrastructure.core.service;

import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.springframework.util.Assert;

/**
 *
 */
public class ThreadLocalContextUtil {

    public static final String CONTEXT_TENANTS = "tenants";

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    private static final ThreadLocal<FineractPlatformTenant> tenantcontext = new ThreadLocal<>();
    
    private static final ThreadLocal<String> authTokenContext = new ThreadLocal<>();
    
    public static void setTenant(final FineractPlatformTenant tenant) {
        Assert.notNull(tenant, "tenant cannot be null");
        tenantcontext.set(tenant);
    }

    public static FineractPlatformTenant getTenant() {
        return tenantcontext.get();
    }

    public static void clearTenant() {
        tenantcontext.remove();
    }

    public static String getDataSourceContext() {
        return contextHolder.get();
    }

    public static void setDataSourceContext(final String dataSourceContext) {
        contextHolder.set(dataSourceContext);
    }

    public static void clearDataSourceContext() {
        contextHolder.remove();
    }
    
    public static void setAuthToken(final String authToken) {
    	authTokenContext.set(authToken);
    }

    public static String getAuthToken() {
        return authTokenContext.get();
    }

}