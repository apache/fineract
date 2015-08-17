/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mifosplatform.infrastructure.configuration.data.ExternalServicesData;
import org.mifosplatform.infrastructure.configuration.exception.ExternalServiceConfigurationNotFoundException;
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

    @Override
    public ExternalServicesData getExternalServiceDetailsByServiceName(String serviceName) {
        // TODO Auto-generated method stub
        final ResultSetExtractor<ExternalServicesData> resultSetExtractor = new ExternalServicesDetailsDataExtractor();
        String serviceNameToUse = null;
        switch (serviceName) {
            case "S3":
                serviceNameToUse = ExternalServicesConstants.S3_SERVICE_NAME;
            break;

            case "SMTP":
                serviceNameToUse = ExternalServicesConstants.SMTP_SERVICE_NAME;
            break;

            default:
                throw new ExternalServiceConfigurationNotFoundException(serviceName);
        }
        final String sql = "SELECT es.name as name, es.id as id FROM c_external_service es where es.name='" + serviceNameToUse + "'";
        final ExternalServicesData externalServicesData = this.jdbcTemplate.query(sql, resultSetExtractor, new Object[] {});
        return externalServicesData;
    }

    private static final class ExternalServicesDetailsDataExtractor implements ResultSetExtractor<ExternalServicesData> {

        @Override
        public ExternalServicesData extractData(ResultSet rs) throws SQLException, DataAccessException {
            // TODO Auto-generated method stub
            Long id = (long) 0;
            String name = null;
            while (rs.next()) {
                name = rs.getString("name");
                id = rs.getLong("id");
            }

            return new ExternalServicesData(id, name);
        }

    }

}
