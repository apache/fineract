package org.mifosplatform.infrastructure.core.service;

import javax.sql.DataSource;

/**
 * A service for getting hold of the appropriate {@link DataSource} connection
 * pool for the given tenant.
 */
public interface DataSourcePerTenantService {

    DataSource retrieveTenantAwareDataSource();
}