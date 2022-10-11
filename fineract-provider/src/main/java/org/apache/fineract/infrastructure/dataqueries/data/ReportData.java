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
package org.apache.fineract.infrastructure.dataqueries.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ReportData {

    @SuppressWarnings("unused")
    private Long id;
    @SuppressWarnings("unused")
    private String reportName;
    @SuppressWarnings("unused")
    private String reportType;
    @SuppressWarnings("unused")
    private String reportSubType;
    @SuppressWarnings("unused")
    private String reportCategory;
    @SuppressWarnings("unused")
    private String description;
    @SuppressWarnings("unused")
    private String reportSql;
    @SuppressWarnings("unused")
    private Boolean coreReport;
    @SuppressWarnings("unused")
    private Boolean useReport;
    @SuppressWarnings("unused")
    private Collection<ReportParameterData> reportParameters;

    @SuppressWarnings("unused")
    private List<String> allowedReportTypes;
    @SuppressWarnings("unused")
    private List<String> allowedReportSubTypes;
    @SuppressWarnings("unused")
    private Collection<ReportParameterData> allowedParameters;

    public void appendedTemplate(final Collection<ReportParameterData> allowedParameters, final Collection<String> allowedReportTypes) {

        final List<String> reportTypes = new ArrayList<>();
        reportTypes.addAll(allowedReportTypes);
        this.allowedReportTypes = reportTypes;

        final List<String> reportSubTypes = new ArrayList<>();
        reportSubTypes.add("Bar");
        reportSubTypes.add("Pie");
        this.allowedReportSubTypes = reportSubTypes;

        this.allowedParameters = allowedParameters;

    }

}
