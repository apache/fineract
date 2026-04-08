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

package org.apache.fineract.portfolio.delinquency.domain;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;

@Getter
@RequiredArgsConstructor
public enum DelinquencyFrequencyType {

    DAYS(0L, "delinquencyFrequencyType.days", "Days frequency"), //
    WEEKS(1L, "delinquencyFrequencyType.weeks", "Week frequency"), //
    MONTHS(2L, "delinquencyFrequencyType.months", "Month frequency"), //
    YEARS(3L, "delinquencyFrequencyType.years", "Year frequency");

    private final Long id;
    private final String code;
    private final String description;

    public static List<StringEnumOptionData> toStringEnumOptions() {
        return Arrays.stream(values()).map(DelinquencyFrequencyType::toData).toList();
    }

    public StringEnumOptionData toData() {
        return new StringEnumOptionData(name(), getCode(), getDescription());
    }
}
