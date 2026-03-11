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
package org.apache.fineract.portfolio.delinquency.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

@ToString
@AllArgsConstructor
@Getter
@Setter
public class DelinquencyBucketData implements Serializable {

    private Long id;
    private String name;
    private List<DelinquencyRangeData> ranges;
    private Long bucketType;
    private DelinquencyMinimumPaymentPeriodAndRuleData minimumPaymentPeriodAndRule;

    public static DelinquencyBucketData getDataInstance(Long id, String name, List<DelinquencyRangeData> ranges, Long bucketType,
            DelinquencyMinimumPaymentPeriodAndRuleData minimumPaymentPeriodAndRule) {
        return new DelinquencyBucketData(id, name, ranges, bucketType, minimumPaymentPeriodAndRule, null, null, null, null);
    }

    private List<DelinquencyRangeData> rangesOptions = new ArrayList<>();
    private List<CodeValueData> bucketTypeOptions = new ArrayList<>();
    private List<CodeValueData> frequencyTypeOptions = new ArrayList<>();
    private List<CodeValueData> minimumPaymentOptions = new ArrayList<>();

}
