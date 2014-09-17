/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.springframework.util.Assert;

/**
 *
 */
public class ThreadLocalContextUtil {

    public static final String CONTEXT_TENANTS = "tenants";

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    private static final ThreadLocal<MifosPlatformTenant> tenantcontext = new ThreadLocal<>();
    
    private static final ThreadLocal<String> authTokenContext = new ThreadLocal<>();

    public static void setTenant(final MifosPlatformTenant tenant) {
        Assert.notNull(tenant, "tenant cannot be null");
        tenantcontext.set(tenant);
    }

    public static MifosPlatformTenant getTenant() {
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