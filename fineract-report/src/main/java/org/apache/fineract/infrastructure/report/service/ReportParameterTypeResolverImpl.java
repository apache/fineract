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
package org.apache.fineract.infrastructure.report.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Service
public final class ReportParameterTypeResolverImpl implements ReportParameterTypeResolver {

    private final JdbcTemplate jdbcTemplate;
    private final boolean isPostgres;

    private static final String PARAM_TYPE_SQL_PREFIX = "SELECT sp.parameter_variable, sp.";
    private static final String PARAM_TYPE_SQL_SUFFIX = """
             AS format_type
            FROM stretchy_report_parameter srp
            JOIN stretchy_parameter sp ON sp.id = srp.parameter_id
            WHERE srp.report_id = (SELECT id FROM stretchy_report WHERE report_name = ?)
            """;

    public ReportParameterTypeResolverImpl(JdbcTemplate jdbcTemplate, @Value("${spring.datasource.hikari.jdbcUrl:}") String jdbcUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.isPostgres = jdbcUrl.toLowerCase().startsWith("jdbc:postgresql");
    }

    private String getQuotedColumnName(String columnName) {
        return isPostgres ? "\"" + columnName + "\"" : columnName;
    }

    @Override
    public Map<String, String> loadParamFormatTypes(String reportName) {
        final Map<String, String> formatTypes = new HashMap<>();
        final String quotedColumnName = getQuotedColumnName("parameter_FormatType");
        final String sql = PARAM_TYPE_SQL_PREFIX + quotedColumnName + PARAM_TYPE_SQL_SUFFIX;
        final SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, reportName);
        while (rs.next()) {
            formatTypes.put(rs.getString("parameter_variable"), rs.getString("format_type"));
        }
        return formatTypes;
    }
}
