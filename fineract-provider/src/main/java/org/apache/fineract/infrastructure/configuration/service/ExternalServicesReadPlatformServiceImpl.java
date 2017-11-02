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
package org.apache.fineract.infrastructure.configuration.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.fineract.infrastructure.configuration.data.ExternalServicesData;
import org.apache.fineract.infrastructure.configuration.exception.ExternalServiceConfigurationNotFoundException;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
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

            case "SMS":
                serviceNameToUse = ExternalServicesConstants.SMS_SERVICE_NAME;
            break;

            case "NOTIFICATION":
                serviceNameToUse = ExternalServicesConstants.NOTIFICATION_SERVICE_NAME;
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
