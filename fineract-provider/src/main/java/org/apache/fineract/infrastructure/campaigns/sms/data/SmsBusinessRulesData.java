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
package org.apache.fineract.infrastructure.campaigns.sms.data;

import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SmsBusinessRulesData {

    private Long reportId;

    private String reportName;

    private String reportType;

    private String reportSubType;

    private String reportDescription;

    private Map<String, Object> reportParamName;

    public static SmsBusinessRulesData instance(final Long reportId, final String reportName, final String reportType,
            final String reportSubType, final Map<String, Object> reportParamName, final String reportDescription) {
        return new SmsBusinessRulesData().setReportId(reportId).setReportName(reportName).setReportType(reportType)
                .setReportSubType(reportSubType).setReportParamName(reportParamName).setReportDescription(reportDescription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof SmsBusinessRulesData)) {
            return false;
        }

        SmsBusinessRulesData that = (SmsBusinessRulesData) o;

        if (reportId != null ? !reportId.equals(that.reportId) : that.reportId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return reportId != null ? reportId.hashCode() : 0;
    }
}
