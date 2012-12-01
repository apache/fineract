package org.mifosplatform.infrastructure.security.service;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;

public interface TenantDetailsService {

    MifosPlatformTenant loadTenantById(String tenantId);

}
