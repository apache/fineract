package org.mifosplatform.infrastructure.core.service;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.springframework.util.Assert;

/**
 *
 */
public class ThreadLocalContextUtil {

    private static final ThreadLocal<MifosPlatformTenant> contextHolder = new ThreadLocal<MifosPlatformTenant>();

    public static void setTenant(final MifosPlatformTenant tenant) {
        Assert.notNull(tenant, "tenant cannot be null");
        contextHolder.set(tenant);
    }

    public static MifosPlatformTenant getTenant() {
        return contextHolder.get();
    }

    public static void clearTenant() {
        contextHolder.remove();
    }
}