package org.mifosng.platform.infrastructure;

public interface TenantDetailsService {

	MifosPlatformTenant loadTenantById(String tenantId);

}
