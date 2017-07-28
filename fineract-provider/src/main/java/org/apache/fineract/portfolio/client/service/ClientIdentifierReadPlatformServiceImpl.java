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
package org.apache.fineract.portfolio.client.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientIdentifierData;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierStatus;
import org.apache.fineract.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientIdentifierReadPlatformServiceImpl implements ClientIdentifierReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public ClientIdentifierReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<ClientIdentifierData> retrieveClientIdentifiers(final Long clientId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final ClientIdentityMapper rm = new ClientIdentityMapper();

        String sql = "select " + rm.schema();

        sql += " order by ci.id";

        return this.jdbcTemplate.query(sql, rm, new Object[] { clientId, hierarchySearchString });
    }

    @Override
    public ClientIdentifierData retrieveClientIdentifier(final Long clientId, final Long clientIdentifierId) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            final ClientIdentityMapper rm = new ClientIdentityMapper();

            String sql = "select " + rm.schema();

            sql += " and ci.id = ?";

            final ClientIdentifierData clientIdentifierData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { clientId,
                    hierarchySearchString, clientIdentifierId });

            return clientIdentifierData;
        } catch (final EmptyResultDataAccessException e) {
            throw new ClientIdentifierNotFoundException(clientIdentifierId);
        }

    }

    private static final class ClientIdentityMapper implements RowMapper<ClientIdentifierData> {

        public ClientIdentityMapper() {}

        public String schema() {
            return "ci.id as id, ci.client_id as clientId, ci.document_type_id as documentTypeId, ci.status as status, ci.document_key as documentKey,"
                    + " ci.description as description, cv.code_value as documentType "
                    + " from m_client_identifier ci, m_client c, m_office o, m_code_value cv"
                    + " where ci.client_id=c.id and c.office_id=o.id" + " and ci.document_type_id=cv.id"
                    + " and ci.client_id = ? and o.hierarchy like ? ";
        }

        @Override
        public ClientIdentifierData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long documentTypeId = JdbcSupport.getLong(rs, "documentTypeId");
            final String documentKey = rs.getString("documentKey");
            final String description = rs.getString("description");
            final String documentTypeName = rs.getString("documentType");
            final CodeValueData documentType = CodeValueData.instance(documentTypeId, documentTypeName);
            final String status = ClientIdentifierStatus.fromInt(rs.getInt("status")).getCode();
            return ClientIdentifierData.singleItem(id, clientId, documentType, documentKey, status, description);
        }

    }

}