/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mifosplatform.infrastructure.configuration.data.S3CredentialsData;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

@Service
public class ExternalServicesReadPlatformServiceImpl implements ExternalServicesReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ExternalServicesReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class S3CredentialsDataExtractor implements ResultSetExtractor<S3CredentialsData> {

        @Override
        public S3CredentialsData extractData(final ResultSet rs) throws SQLException, DataAccessException {
            String accessKey = null;
            String bucketName = null;
            String secretKey = null;
            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.S3_ACCESS_KEY)) {
                    accessKey = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.S3_BUCKET_NAME)) {
                    bucketName = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.S3_SECRET_KEY)) {
                    secretKey = rs.getString("value");
                }
            }
            return new S3CredentialsData(bucketName, accessKey, secretKey);
        }
    }

    @Override
    public S3CredentialsData getS3Credentials() {
        final ResultSetExtractor<S3CredentialsData> resultSetExtractor = new S3CredentialsDataExtractor();
        final String sql = "SELECT es.name, es.value FROM c_external_service es where es.name in('s3_bucket_name','s3_access_key','s3_secret_key')";
        final S3CredentialsData s3CredentialsData = this.jdbcTemplate.query(sql, resultSetExtractor, new Object[] {});
        return s3CredentialsData;
    }
}
