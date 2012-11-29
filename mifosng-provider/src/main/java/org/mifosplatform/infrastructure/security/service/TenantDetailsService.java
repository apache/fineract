package org.mifosplatform.infrastructure.security.service;

import org.mifosng.platform.infrastructure.MifosPlatformTenant;

public interface TenantDetailsService {

    MifosPlatformTenant loadTenantById(String tenantId);

}
