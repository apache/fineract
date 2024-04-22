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
package org.apache.fineract.adhocquery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.adhocquery.api.AdHocJsonInputParams;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "m_adhoc")
public class AdHoc extends AbstractAuditableCustom {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "query", length = 2000)
    private String query;

    @Column(name = "table_name", length = 100)
    private String tableName;

    @Column(name = "table_fields", length = 2000)
    private String tableFields;

    @Column(name = "email", length = 500)
    private String email;

    @Column(name = "report_run_frequency_code")
    private Long reportRunFrequency;

    @Column(name = "report_run_every")
    private Long reportRunEvery;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public static AdHoc fromJson(final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(AdHocJsonInputParams.NAME.getValue());

        final String query = command.stringValueOfParameterNamed(AdHocJsonInputParams.QUERY.getValue());
        final String tableName = command.stringValueOfParameterNamed(AdHocJsonInputParams.TABLENAME.getValue());
        final String tableFields = command.stringValueOfParameterNamed(AdHocJsonInputParams.TABLEFIELDS.getValue());
        final String email = command.stringValueOfParameterNamed(AdHocJsonInputParams.EMAIL.getValue());
        final Long reportRunFrequency = command.longValueOfParameterNamed(AdHocJsonInputParams.REPORT_RUN_FREQUENCY.getValue());
        final Long reportRunEvery = command.longValueOfParameterNamed(AdHocJsonInputParams.REPORT_RUN_EVERY.getValue());
        final boolean active = command.booleanPrimitiveValueOfParameterNamed(AdHocJsonInputParams.ACTIVE.getValue());
        return new AdHoc().setName(StringUtils.defaultIfEmpty(name, null)).setQuery(StringUtils.defaultIfEmpty(query, null))
                .setTableName(StringUtils.defaultIfEmpty(tableName, null)).setTableFields(StringUtils.defaultIfEmpty(tableFields, null))
                .setEmail(StringUtils.defaultIfEmpty(email, null)).setReportRunFrequency(reportRunFrequency)
                .setReportRunEvery(reportRunEvery).setActive(false);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        final String descriptionParamName = "query";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.query)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.query = newValue;
        }
        final String tableName = "tableName";
        if (command.isChangeInStringParameterNamed(tableName, this.tableName)) {
            final String newValue = command.stringValueOfParameterNamed(tableName);
            actualChanges.put(tableName, newValue);
            this.tableName = newValue;
        }
        final String tableField = "tableFields";
        if (command.isChangeInStringParameterNamed(tableField, this.tableFields)) {
            final String newValue = command.stringValueOfParameterNamed(tableField);
            actualChanges.put(tableField, newValue);
            this.tableFields = newValue;
        }
        final String email = "email";
        if (command.isChangeInStringParameterNamed(email, this.email)) {
            final String newValue = command.stringValueOfParameterNamed(email);
            actualChanges.put(email, newValue);
            this.email = newValue;
        }
        final String reportRunFrequency = "reportRunFrequency";
        if (command.isChangeInLongParameterNamed(reportRunFrequency, this.getReportRunFrequency())) {
            final Long newValue = command.longValueOfParameterNamed(reportRunFrequency);
            actualChanges.put(reportRunFrequency, newValue);
            this.reportRunFrequency = newValue;
        }
        final String reportRunEvery = "reportRunEvery";
        if (command.isChangeInLongParameterNamed(reportRunEvery, this.getReportRunEvery())) {
            final Long newValue = command.longValueOfParameterNamed(reportRunEvery);
            actualChanges.put(reportRunEvery, newValue);
            this.reportRunEvery = newValue;
        }
        final String paramisActive = "active";
        if (command.isChangeInBooleanParameterNamed(paramisActive, this.active)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(paramisActive);
            actualChanges.put(paramisActive, newValue);
            this.active = newValue;
        }
        return actualChanges;
    }

}
