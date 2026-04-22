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
package org.apache.fineract.infrastructure.campaigns.sms.mapper;

import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsBusinessRulesData;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessRuleMapper implements ResultSetExtractor<List<SmsBusinessRulesData>> {

    private String schema;
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    @PostConstruct
    public void init() {
        final StringBuilder sql = new StringBuilder(300);
        sql.append("sr.id as id, ");
        sql.append("sr.report_name as reportName, ");
        sql.append("sr.report_type as reportType, ");
        sql.append("sr.report_subtype as reportSubType, ");
        sql.append("sr.description as description, ");
        sql.append("sp.parameter_variable as params, ");
        sql.append("sp." + sqlGenerator.escape("parameter_FormatType") + " as paramType, ");
        sql.append("sp.parameter_label as paramLabel, ");
        sql.append("sp.parameter_name as paramName ");
        sql.append("from stretchy_report sr ");
        sql.append("left join stretchy_report_parameter as srp on srp.report_id = sr.id ");
        sql.append("left join stretchy_parameter as sp on sp.id = srp.parameter_id ");

        this.schema = sql.toString();
    }

    public String schema() {
        return this.schema;
    }

    @Override
    public List<SmsBusinessRulesData> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<SmsBusinessRulesData> smsBusinessRulesDataList = new ArrayList<>();

        SmsBusinessRulesData smsBusinessRulesData = null;

        Map<Long, SmsBusinessRulesData> mapOfSameObjects = new HashMap<>();

        while (rs.next()) {
            final Long id = rs.getLong("id");
            smsBusinessRulesData = mapOfSameObjects.get(id);
            if (smsBusinessRulesData == null) {
                final String reportName = rs.getString("reportName");
                final String reportType = rs.getString("reportType");
                final String reportSubType = rs.getString("reportSubType");
                final String paramName = rs.getString("paramName");
                final String paramLabel = rs.getString("paramLabel");
                final String description = rs.getString("description");

                Map<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put(paramLabel, paramName);
                smsBusinessRulesData = SmsBusinessRulesData.instance(id, reportName, reportType, reportSubType, hashMap, description);
                mapOfSameObjects.put(id, smsBusinessRulesData);
                // add to the list
                smsBusinessRulesDataList.add(smsBusinessRulesData);
            }
            // add new paramType to the existing object
            Map<String, Object> hashMap = new HashMap<String, Object>();
            final String paramName = rs.getString("paramName");
            final String paramLabel = rs.getString("paramLabel");
            hashMap.put(paramLabel, paramName);

            // get existing map and add new items to it
            smsBusinessRulesData.reportParamName().putAll(hashMap);
        }

        return smsBusinessRulesDataList;
    }
}
