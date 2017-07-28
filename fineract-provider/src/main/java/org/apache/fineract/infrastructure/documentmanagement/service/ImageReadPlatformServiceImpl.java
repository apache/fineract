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
package org.apache.fineract.infrastructure.documentmanagement.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.documentmanagement.api.ImagesApiResource.ENTITY_TYPE_FOR_IMAGES;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepository;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryFactory;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageData;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ImageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ImageReadPlatformServiceImpl implements ImageReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ContentRepositoryFactory contentRepositoryFactory;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final StaffRepositoryWrapper staffRepositoryWrapper;

    @Autowired
    public ImageReadPlatformServiceImpl(final RoutingDataSource dataSource, final ContentRepositoryFactory documentStoreFactory,
            final ClientRepositoryWrapper clientRepositoryWrapper, StaffRepositoryWrapper staffRepositoryWrapper) {
        this.staffRepositoryWrapper = staffRepositoryWrapper;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.contentRepositoryFactory = documentStoreFactory;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
    }

    private static final class ImageMapper implements RowMapper<ImageData> {

        private final String entityDisplayName;

        public ImageMapper(final String entityDisplayName) {
            this.entityDisplayName = entityDisplayName;
        }

        public String schema(String entityType) {
            StringBuilder builder = new StringBuilder("image.id as id, image.location as location, image.storage_type_enum as storageType ");
            if (ENTITY_TYPE_FOR_IMAGES.CLIENTS.toString().equalsIgnoreCase(entityType)) {
                builder.append(" from m_image image , m_client client " + " where client.image_id = image.id and client.id=?");
            } else if (ENTITY_TYPE_FOR_IMAGES.STAFF.toString().equalsIgnoreCase(entityType)) {
                builder.append("from m_image image , m_staff staff " + " where staff.image_id = image.id and staff.id=?");
            }
            return builder.toString();
        }

        @Override
        public ImageData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String location = rs.getString("location");
            final Integer storageType = JdbcSupport.getInteger(rs, "storageType");
            return new ImageData(id, location, storageType, this.entityDisplayName);
        }
    }

    @Override
    public ImageData retrieveImage(String entityType, final Long entityId) {
        try {
            Object owner;
            String displayName = null;
            if (ENTITY_TYPE_FOR_IMAGES.CLIENTS.toString().equalsIgnoreCase(entityType)) {
                owner = this.clientRepositoryWrapper.findOneWithNotFoundDetection(entityId);
                displayName = ((Client) owner).getDisplayName();
            } else if (ENTITY_TYPE_FOR_IMAGES.STAFF.toString().equalsIgnoreCase(entityType)) {
                owner = this.staffRepositoryWrapper.findOneWithNotFoundDetection(entityId);
                displayName = ((Staff) owner).displayName();
            }
            final ImageMapper imageMapper = new ImageMapper(displayName);

            final String sql = "select " + imageMapper.schema(entityType);

            final ImageData imageData = this.jdbcTemplate.queryForObject(sql, imageMapper, new Object[] { entityId });
            final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository(imageData.storageType());
            final ImageData result = contentRepository.fetchImage(imageData);

          //Once we read content EofSensorInputStream, the wrappedStream object is becoming null. So further image source is becoming null 
            //For Amazon S3. If file is not present, already S3ContentRepository would have thrown this exception.
            if (!result.available()) { throw new ImageNotFoundException(entityType, entityId); }

            return result;
        } catch (final EmptyResultDataAccessException e) {
            throw new ImageNotFoundException("clients", entityId);
        }
    }
}