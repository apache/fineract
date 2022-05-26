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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.adhocquery.domain.ReportRunFrequency;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object represent note or case information AdHocData
 *
 */
@Getter
@RequiredArgsConstructor
public class AdHocData {

    private final Long id;
    private final String name;
    private final String query;
    private final String tableName;
    private final String tableFields;
    private final String email;
    private final boolean isActive;
    private final ZonedDateTime createdOn;
    private final Long createdById;
    private final Long updatedById;
    private final ZonedDateTime updatedOn;
    private final String createdBy;
    private final List<EnumOptionData> reportRunFrequencies;
    private final Long reportRunFrequency;
    private final Long reportRunEvery;
    private final ZonedDateTime lastRun;

    public static AdHocData template() {
        List<EnumOptionData> reportRunFrequencies = Arrays.stream(ReportRunFrequency.values())
                .map(rrf -> new EnumOptionData(rrf.getValue(), rrf.getCode(), rrf.getCode())).collect(Collectors.toList());

        return new AdHocData(null, null, null, null, null, null, false, null, null, null, null, null, reportRunFrequencies, null, null,
                null);
    }
}
