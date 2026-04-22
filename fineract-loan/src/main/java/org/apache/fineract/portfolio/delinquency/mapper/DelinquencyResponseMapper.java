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
package org.apache.fineract.portfolio.delinquency.mapper;

import java.util.List;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.portfolio.delinquency.api.data.DelinquencyBucketResponse;
import org.apache.fineract.portfolio.delinquency.api.data.DelinquencyRangeResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import org.mapstruct.Mapper;

@Mapper(config = MapstructMapperConfig.class)
public interface DelinquencyResponseMapper {

    DelinquencyBucketResponse mapBucket(DelinquencyBucketData delinquencyBucketData);

    List<DelinquencyBucketResponse> mapBucket(List<DelinquencyBucketData> delinquencyBucketData);

    DelinquencyRangeResponse mapRange(DelinquencyRangeData delinquencyRangeData);

    List<DelinquencyRangeResponse> mapRange(List<DelinquencyRangeData> delinquencyRangeData);

    default StringEnumOptionData mapEnum(DelinquencyBucketType delinquencyBucketType) {
        return delinquencyBucketType.toData();
    }

    default StringEnumOptionData mapEnum(DelinquencyFrequencyType delinquencyFrequencyType) {
        return delinquencyFrequencyType.toData();
    }

    default StringEnumOptionData mapEnum(DelinquencyMinimumPaymentType delinquencyMinimumPaymentType) {
        return delinquencyMinimumPaymentType.toData();
    }
}
