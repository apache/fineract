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
package org.apache.fineract.adhocquery.data;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.adhocquery.domain.ReportRunFrequency;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object represent note or case information AdHocData
 *
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AdHocData {

    private Long id;
    private String name;
    private String query;
    private String tableName;
    private String tableFields;
    private String email;
    private boolean isActive;
    private ZonedDateTime createdOn;
    private Long createdById;
    private Long updatedById;
    private ZonedDateTime updatedOn;
    private String createdBy;
    private List<EnumOptionData> reportRunFrequencies;
    private Long reportRunFrequency;
    private Long reportRunEvery;
    private ZonedDateTime lastRun;

    public static AdHocData template() {
        List<EnumOptionData> reportRunFrequencies = Arrays.stream(ReportRunFrequency.values())
                .map(rrf -> new EnumOptionData(rrf.getValue(), rrf.getCode(), rrf.getCode())).collect(Collectors.toList());

        return new AdHocData().setReportRunFrequencies(reportRunFrequencies);
    }
}
