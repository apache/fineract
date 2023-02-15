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
package org.apache.fineract.infrastructure.security.service;

import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.tenant.TenantMapper;
import org.apache.fineract.infrastructure.security.exception.InvalidTenantIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * A JDBC implementation of {@link BasicAuthTenantDetailsService} for loading a tenants details by a
 * <code>tenantIdentifier</code>.
 */
@Service
public class BasicAuthTenantDetailsServiceJdbc implements BasicAuthTenantDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BasicAuthTenantDetailsServiceJdbc(@Qualifier("hikariTenantDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @Cacheable(value = "tenantsById")
    public FineractPlatformTenant loadTenantById(final String tenantIdentifier, final boolean isReport) {

        try {
            final TenantMapper rm = new TenantMapper(isReport);
            final String sql = "select  " + rm.schema() + " where t.identifier = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { tenantIdentifier }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new InvalidTenantIdentifierException("The tenant identifier: " + tenantIdentifier + " is not valid.", e);
        }
    }
}
