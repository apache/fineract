/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.provisioning.data.ProvisioningCategoryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ProvisioningCategoryReadPlatformServiceImpl implements ProvisioningCategoryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ProvisioningCategoryRowMapper provisionCategoryRowMapper;

    @Autowired
    public ProvisioningCategoryReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.provisionCategoryRowMapper = new ProvisioningCategoryRowMapper();
    }

    @Override
    public Collection<ProvisioningCategoryData> retrieveAllProvisionCategories() {
        //User is already authenticated by API. So we no need to check again here
        final String sql = "select " + this.provisionCategoryRowMapper.schema() + " from m_provision_category pc order by pc.id";
        return this.jdbcTemplate.query(sql, this.provisionCategoryRowMapper, new Object[] {});
    }

    private static final class ProvisioningCategoryRowMapper implements RowMapper<ProvisioningCategoryData> {
        @Override
        public ProvisioningCategoryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String categoryName = rs.getString("category_name");
            final String description = rs.getString("description");
            return new ProvisioningCategoryData(id, categoryName, description);
        }

        public String schema() {
            return " pc.id as id, pc.category_name as category_name, pc.description as description";
        }
    }
}