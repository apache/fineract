/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.service;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.DocumentStore;
import org.mifosplatform.infrastructure.core.service.DocumentStoreFactory;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Service
public class DocumentReadPlatformServiceImpl implements DocumentReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final DocumentStoreFactory documentStoreFactory;

    @Autowired
    public DocumentReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
                                           final DocumentStoreFactory documentStoreFactory) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.documentStoreFactory = documentStoreFactory;
    }

    @Override
    public Collection<DocumentData> retrieveAllDocuments(final String entityType, final Long entityId) {

        this.context.authenticatedUser();

        // TODO verify if the entities are valid and a user
        // has data
        // scope for the particular entities
        final DocumentMapper mapper = new DocumentMapper(true);
        final String sql = "select " + mapper.schema() + " order by d.id";
        return this.jdbcTemplate.query(sql, mapper, new Object[] { entityType, entityId });
    }

    @Override
    public FileData retrieveDocumentAsFile(final String entityType, final Long entityId, final Long documentId) {
        try {
            DocumentData documentData = getDocumentData(entityType, entityId, documentId);
            DocumentStore instanceForRead = documentStoreFactory.getInstanceFromStorageType(documentData.storeType());
            return instanceForRead.retrieveDocument(documentData);
        } catch (final EmptyResultDataAccessException e) {
            throw new DocumentNotFoundException(entityType, entityId, documentId);
        } catch (final DocumentNotFoundException d) {
            throw d;
        }
    }

    @Override
    public DocumentData retrieveDocument(String entityType, Long entityId, Long documentId) {
        try {
            return getDocumentData(entityType, entityId, documentId);
        } catch (final EmptyResultDataAccessException e) {
            throw new DocumentNotFoundException(entityType, entityId, documentId);
        }
    }

    private static final class DocumentMapper implements RowMapper<DocumentData> {

        private final boolean hideLocation;

        public DocumentMapper(final boolean hideLocation) {
            this.hideLocation = hideLocation;
        }

        public String schema() {
            return "d.id as id, d.parent_entity_type as parentEntityType, d.parent_entity_id as parentEntityId, d.name as name, "
                    + " d.file_name as fileName, d.size as fileSize, d.type as fileType, "
                    + " d.description as description, d.location as location,"
                    + " d.storage_type as storageType"
                    + " from m_document d where d.parent_entity_type=? and d.parent_entity_id=?";
        }

        @Override
        public DocumentData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Long parentEntityId = JdbcSupport.getLong(rs, "parentEntityId");
            final Long fileSize = JdbcSupport.getLong(rs, "fileSize");
            final String parentEntityType = rs.getString("parentEntityType");
            final String name = rs.getString("name");
            final String fileName = rs.getString("fileName");
            final String fileType = rs.getString("fileType");
            final String description = rs.getString("description");
            String location = null;
            if (!this.hideLocation) {
                location = rs.getString("location");
            }
            final String storageType = rs.getString("storageType");

            return new DocumentData(id, parentEntityType, parentEntityId, name, fileName, fileSize, fileType, description, location, storageType);
        }
    }


    private DocumentData getDocumentData(String entityType, Long entityId, Long documentId) {
        this.context.authenticatedUser();

        // TODO verify if the entities are valid and a
        // user has data
        // scope for the particular entities
        final DocumentMapper mapper = new DocumentMapper(false);
        final String sql = "select " + mapper.schema() + " and d.id=? ";
        return this.jdbcTemplate.queryForObject(sql, mapper, new Object[]{entityType, entityId, documentId});
    }
}