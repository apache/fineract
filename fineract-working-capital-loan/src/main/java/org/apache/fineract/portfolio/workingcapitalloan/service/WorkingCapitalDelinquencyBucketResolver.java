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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketType;
import org.apache.fineract.portfolio.delinquency.exception.DelinquencyBucketNotFoundException;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.springframework.stereotype.Component;

/**
 * Resolves and validates delinquency buckets for Working Capital products and loans. Only
 * {@link DelinquencyBucketType#WORKING_CAPITAL} buckets are allowed.
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalDelinquencyBucketResolver {

    private final DelinquencyBucketRepository delinquencyBucketRepository;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;

    public Collection<DelinquencyBucketData> retrieveWorkingCapitalDelinquencyBucketOptions() {
        final List<DelinquencyBucketData> workingCapitalBuckets = this.delinquencyReadPlatformService
                .retrieveDelinquencyBucketsByType(DelinquencyBucketType.WORKING_CAPITAL);
        return workingCapitalBuckets.isEmpty() ? null : workingCapitalBuckets;
    }

    public DelinquencyBucket findWorkingCapitalBucketByIdIfProvided(final Long delinquencyBucketId) {
        if (delinquencyBucketId == null) {
            return null;
        }
        final DelinquencyBucket bucket = this.delinquencyBucketRepository.findById(delinquencyBucketId)
                .orElseThrow(() -> DelinquencyBucketNotFoundException.notFound(delinquencyBucketId));
        validateIsWorkingCapitalType(bucket);
        return bucket;
    }

    public void validateIsWorkingCapitalType(final DelinquencyBucket bucket) {
        if (bucket != null && !DelinquencyBucketType.WORKING_CAPITAL.equals(bucket.getBucketType())) {
            throw new PlatformApiDataValidationException(
                    List.of(ApiParameterError.parameterError("validation.msg.delinquencyBucketId.must.be.working.capital.type",
                            "The parameter `delinquencyBucketId` must reference a WORKING_CAPITAL delinquency bucket.",
                            WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, bucket.getId())));
        }
    }
}
