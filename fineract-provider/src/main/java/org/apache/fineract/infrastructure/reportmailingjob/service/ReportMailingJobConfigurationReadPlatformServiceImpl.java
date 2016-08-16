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
package org.apache.fineract.infrastructure.reportmailingjob.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobConfigurationData;
import org.apache.fineract.infrastructure.reportmailingjob.exception.ReportMailingJobConfigurationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ReportMailingJobConfigurationReadPlatformServiceImpl implements ReportMailingJobConfigurationReadPlatformService {
    private final JdbcTemplate jdbcTemplate;
    
    /** 
     * ReportMailingJobConfigurationReadPlatformServiceImpl constructor
     **/
    @Autowired
    public ReportMailingJobConfigurationReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    @Override
    public Collection<ReportMailingJobConfigurationData> retrieveAllReportMailingJobConfigurations() {
        final ReportMailingJobConfigurationMapper mapper = new ReportMailingJobConfigurationMapper();
        final String sql = "select " + mapper.ReportMailingJobConfigurationSchema();
        
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public ReportMailingJobConfigurationData retrieveReportMailingJobConfiguration(String name) {
        try {
            final ReportMailingJobConfigurationMapper mapper = new ReportMailingJobConfigurationMapper();
            final String sql = "select " + mapper.ReportMailingJobConfigurationSchema() + " where rmjc.name = ?";
            
            return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { name });
        }
        
        catch (final EmptyResultDataAccessException ex) {
            throw new ReportMailingJobConfigurationNotFoundException(name);
        }
    }
    
    private static final class ReportMailingJobConfigurationMapper implements RowMapper<ReportMailingJobConfigurationData> {
        public String ReportMailingJobConfigurationSchema() {
            return "rmjc.id, rmjc.name, rmjc.value "
                    + "from m_report_mailing_job_configuration rmjc";
        }
        
        @Override
        public ReportMailingJobConfigurationData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Integer id = JdbcSupport.getInteger(rs, "id");
            final String name = rs.getString("name");
            final String value = rs.getString("value");
            
            return ReportMailingJobConfigurationData.newInstance(id, name, value);
        }
    }
}
